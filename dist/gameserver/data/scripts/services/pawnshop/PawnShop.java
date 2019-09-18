package services.pawnshop;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.taskmanager.DelayedItemsManager;
import ru.j2dev.gameserver.templates.OptionDataTemplate;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PawnShop extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PawnShop.class);
    private static final String HTML_BASE = "mods/pawnshop";
    private static final String BYPASS_PREFIX = "-h scripts_services.pawnshop.PawnShop:";
    private static final AtomicInteger LAST_ID = new AtomicInteger(0);
    private static final Comparator<PawnShopItem> PAWN_SHOP_ITEM_COMPARATOR = (o1, o2) -> {
        if (o1.getCurrencyItemId() == o2.getCurrencyItemId()) {
            return o1.getPrice() - o2.getPrice();
        }
        return o2.getCurrencyItemId() - o1.getCurrencyItemId();
    };
    private static CopyOnWriteArrayList<PawnShopItem> PAWN_SHOP_ITEMS = new CopyOnWriteArrayList<>();

    private static void loadItems() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        final List<PawnShopItem> items = new LinkedList<>();
        int maxId = 0;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.createStatement();
            rset = stmt.executeQuery("{CALL `lip_ex_PawnShopLoadItems`()}");
            while (rset.next()) {
                final PawnShopItem item = new PawnShopItem(rset.getInt("id"), rset.getInt("ownerId"), rset.getInt("itemType"), rset.getInt("amount"), rset.getInt("enchantLevel"), rset.getInt("currency"), rset.getInt("price"), rset.getInt("varOpt1"), rset.getInt("varOpt2"));
                items.add(0, item);
                if (item.getId() > maxId) {
                    maxId = item.getId();
                }
            }
        } catch (SQLException se) {
            LOGGER.error("PawnShop: Can't load items", se);
        } finally {
            DbUtils.closeQuietly(con, stmt, rset);
        }
        final int lastId = LAST_ID.get();
        if (maxId > lastId) {
            LAST_ID.compareAndSet(lastId, maxId);
        }
        PAWN_SHOP_ITEMS = new CopyOnWriteArrayList<>(items);
    }

    public static void showStartPage(final Player player, final NpcInstance npc) {
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        doBuyList(player, npc, 0, "");
    }

    private static boolean isAllowedNpc(final Player player, final NpcInstance npc) {
        return player != null && npc != null && !player.isActionsDisabled() && (Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting()) && npc.isInActingRange(player);
    }

    private static void parseArgs(final String[] args, final int[] ints) {
        parseArgs(args, ints, null);
    }

    private static void parseArgs(final String[] args, final int[] ints, final StringBuilder remind) {
        int argIdx = 0;
        if (ints != null) {
            for (int intIdx = 0; argIdx < args.length && intIdx < ints.length; ++argIdx, ++intIdx) {
                final String arg = args[argIdx];
                try {
                    final long longVal = Long.parseLong(arg);
                    ints[intIdx] = (int) (longVal & 0xFFFFFFFFL);
                } catch (Exception ex) {
                    LOGGER.warn("Can't parse int arg \"" + arg + "\"");
                }
            }
        }
        if (remind != null && argIdx < args.length) {
            remind.append(args[argIdx]);
            ++argIdx;
            while (argIdx < args.length) {
                remind.append(' ').append(args[argIdx]);
                ++argIdx;
            }
        }
    }

    private static void doBuyList(final Player player, final NpcInstance npc, final int page, final String queryStr) {
        final String query = stripString(queryStr);
        final PawnShopItem[] pawnShopItems = searchQuery(player, query);
        final StringBuilder itemsHtmlBuilder = new StringBuilder();
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("mods/pawnshop/buy_list.htm");
        for (int psiIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page, psiLastIdx = psiIdx + Config.PAWNSHOP_ITEMS_PER_PAGE; psiIdx < psiLastIdx && psiIdx < pawnShopItems.length; ++psiIdx) {
            final PawnShopItem pawnShopItem = pawnShopItems[psiIdx];
            itemsHtmlBuilder.append(itemHtml(player, "pawnshop.buy_item_element", String.format("-h scripts_services.pawnshop.PawnShop:buyItem %d %d %s", pawnShopItem.getId(), page, query), pawnShopItem.getItemTemplate(), pawnShopItem.getEnchantLevel(), pawnShopItem.getAmount(), ItemTemplateHolder.getInstance().getTemplate(pawnShopItem.getCurrencyItemId()), pawnShopItem.getPrice(), pawnShopItem.getOwnerName(), pawnShopItem.getVariationSkill()));
        }
        html.replace("%list%", itemsHtmlBuilder.toString());
        html.replace("%paging%", pagingHtml(player, page, pawnShopItems, "buyList", query));
        player.sendPacket(html);
    }

    private static PawnShopItem[] searchQuery(final Player player, final String query) {
        final List<PawnShopItem> result = new LinkedList<>();
        boolean fallback = true;
        if (!query.isEmpty()) {
            if (query.charAt(0) == '+') {
                final String enchLvlStr = query.substring(1);
                if (StringUtils.isNumeric(enchLvlStr)) {
                    try {
                        final int enchLvl = Integer.parseInt(enchLvlStr);
                        for (final PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS) {
                            if (isAllowedItem(pawnShopItem) && pawnShopItem.getEnchantLevel() == enchLvl) {
                                result.add(pawnShopItem);
                            }
                        }
                        fallback = false;
                    } catch (Exception ex) {
                        LOGGER.warn("PawnShop: Can't process item enchant level query \"" + query + "\"", ex);
                    }
                }
            }
            if (fallback && StringUtils.isNumeric(query)) {
                try {
                    final int itemTypeId = Integer.parseInt(query);
                    for (final PawnShopItem pawnShopItem2 : PAWN_SHOP_ITEMS) {
                        if (isAllowedItem(pawnShopItem2) && pawnShopItem2.getItemTypeId() == itemTypeId) {
                            result.add(pawnShopItem2);
                        }
                    }
                    fallback = false;
                } catch (Exception ex2) {
                    LOGGER.warn("PawnShop: Can't process item id query \"" + query + "\"", ex2);
                }
            }
            if (fallback) {
                if (query.length() >= Config.PAWNSHOP_MIN_QUERY_LENGTH) {
                    for (final PawnShopItem pawnShopItem3 : PAWN_SHOP_ITEMS) {
                        if (isAllowedItem(pawnShopItem3) && StringUtils.containsIgnoreCase(pawnShopItem3.getNameForQuery(), query)) {
                            result.add(pawnShopItem3);
                        }
                    }
                    fallback = false;
                } else {
                    player.sendMessage(new CustomMessage("pawnshop.query_to_short", player));
                }
            }
            if (!fallback && Config.PAWNSHOP_PRICE_SORT) {
                result.sort(PAWN_SHOP_ITEM_COMPARATOR);
            }
        }
        if (fallback) {
            for (final PawnShopItem pawnShopItem3 : PAWN_SHOP_ITEMS) {
                if (isAllowedItem(pawnShopItem3)) {
                    result.add(pawnShopItem3);
                }
            }
        }
        return result.toArray(new PawnShopItem[result.size()]);
    }

    private static void doBuyItem(final Player player, final int pawnShopItemId) {
        if (!player.getPlayerAccess().UseTrade) {
            player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
            return;
        }
        if (pawnShopItemId <= 0) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        if (player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10) {
            player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
            return;
        }
        PawnShopItem pawnShopItem = null;
        for (final PawnShopItem psi : PAWN_SHOP_ITEMS) {
            if (psi.getId() == pawnShopItemId) {
                pawnShopItem = psi;
            }
        }
        if (pawnShopItem == null || !isAllowedItem(pawnShopItem)) {
            return;
        }
        final int currency = pawnShopItem.getCurrencyItemId();
        final long price = pawnShopItem.getPrice();
        final int ownerId = pawnShopItem.getOwnerId();
        if (ownerId == player.getObjectId()) {
            player.sendMessage(new CustomMessage("pawnshop.owner_cant_buy_own_item", player));
            return;
        }
        if (currency <= 0 || price <= 0L || player.getInventory().getCountOf(currency) < price) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(price).addItemName(currency));
            return;
        }
        if (pawnShopItem.getDeleted().compareAndSet(false, true)) {
            try {
                if (currency <= 0 || price <= 0L || ItemFunctions.removeItem(player, currency, price, true) < price) {
                    pawnShopItem.getDeleted().set(false);
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(price).addItemName(currency));
                    return;
                }
            } catch (Exception ex) {
                LOGGER.error("PawnShop: Can't buy pawnshop item " + pawnShopItemId, ex);
                pawnShopItem.getDeleted().set(false);
                return;
            }
            PAWN_SHOP_ITEMS.remove(pawnShopItem);
            pawnShopItem.delete();
            final ItemInstance item = ItemFunctions.createItem(pawnShopItem.getItemTypeId());
            item.setCount((long) pawnShopItem.getAmount());
            item.setEnchantLevel(pawnShopItem.getEnchantLevel());
            item.setVariationStat1(pawnShopItem.getVarOpt1());
            item.setVariationStat2(pawnShopItem.getVarOpt2());
            Log.LogItem(player, ItemLog.TradeBuy, item);
            player.getInventory().addItem(item);
            player.sendPacket(SystemMessage2.obtainItems(item.getItemId(), item.getCount(), item.getEnchantLevel()));
            final Player owner = World.getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                ItemFunctions.addItem(owner, currency, price, true);
                Log.LogItem(player, ItemLog.TradeSell, item);
            } else {
                DelayedItemsManager.getInstance().addDelayed(ownerId, currency, (int) price, 0, "Reward for pawnshop item " + pawnShopItemId + " bought by " + player);
            }
        }
    }

    private static void doRefundList(final Player player, final NpcInstance npc, final int page) {
        final PawnShopItem[] pawnShopItems = getOwnerItems(player);
        final StringBuilder itemsHtmlBuilder = new StringBuilder();
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("mods/pawnshop/refund_list.htm");
        for (int psiIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page, psiLastIdx = psiIdx + Config.PAWNSHOP_ITEMS_PER_PAGE; psiIdx < psiLastIdx && psiIdx < pawnShopItems.length; ++psiIdx) {
            final PawnShopItem pawnShopItem = pawnShopItems[psiIdx];
            itemsHtmlBuilder.append(itemHtml(player, "pawnshop.refund_item_element", String.format("-h scripts_services.pawnshop.PawnShop:refundItem %d %d", pawnShopItem.getId(), page), pawnShopItem.getItemTemplate(), pawnShopItem.getEnchantLevel(), pawnShopItem.getAmount(), ItemTemplateHolder.getInstance().getTemplate(pawnShopItem.getCurrencyItemId()), pawnShopItem.getPrice(), pawnShopItem.getOwnerName(), pawnShopItem.getVariationSkill()));
        }
        html.replace("%paging%", pagingHtml(player, page, pawnShopItems, "refundList"));
        html.replace("%list%", itemsHtmlBuilder.toString());
        player.sendPacket(html);
    }

    private static void doRefundItem(final Player player, final int pawnShopItemId) {
        if (!player.getPlayerAccess().UseTrade) {
            player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
            return;
        }
        if (pawnShopItemId <= 0) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        if (player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10) {
            player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
            return;
        }
        PawnShopItem pawnShopItem = null;
        for (final PawnShopItem psi : PAWN_SHOP_ITEMS) {
            if (psi.getId() == pawnShopItemId) {
                pawnShopItem = psi;
            }
        }
        if (pawnShopItem == null || !isAllowedItem(pawnShopItem)) {
            return;
        }
        if (Config.PAWNSHOP_REFUND_ITEM_ID > 0 && Config.PAWNSHOP_REFUND_ITEM_COUNT > 0L && player.getInventory().getCountOf(Config.PAWNSHOP_REFUND_ITEM_ID) < Config.PAWNSHOP_REFUND_ITEM_COUNT) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_REFUND_ITEM_COUNT).addItemName(Config.PAWNSHOP_REFUND_ITEM_ID));
            return;
        }
        if (pawnShopItem.getDeleted().compareAndSet(false, true)) {
            try {
                if (Config.PAWNSHOP_REFUND_ITEM_ID > 0 && Config.PAWNSHOP_REFUND_ITEM_COUNT > 0L && ItemFunctions.removeItem(player, Config.PAWNSHOP_REFUND_ITEM_ID, Config.PAWNSHOP_REFUND_ITEM_COUNT, true) < Config.PAWNSHOP_REFUND_ITEM_COUNT) {
                    pawnShopItem.getDeleted().set(false);
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_REFUND_ITEM_COUNT).addItemName(Config.PAWNSHOP_REFUND_ITEM_ID));
                    return;
                }
            } catch (Exception ex) {
                LOGGER.error("PawnShop: Can't refund pawnshop item " + pawnShopItemId, ex);
                pawnShopItem.getDeleted().set(false);
                return;
            }
            PAWN_SHOP_ITEMS.remove(pawnShopItem);
            pawnShopItem.delete();
            final ItemInstance item = ItemFunctions.createItem(pawnShopItem.getItemTypeId());
            item.setCount((long) pawnShopItem.getAmount());
            item.setEnchantLevel(pawnShopItem.getEnchantLevel());
            item.setVariationStat1(pawnShopItem.getVarOpt1());
            item.setVariationStat2(pawnShopItem.getVarOpt2());
            Log.LogItem(player, ItemLog.RefundReturn, item);
            player.getInventory().addItem(item);
            player.sendPacket(SystemMessage2.obtainItems(item.getItemId(), item.getCount(), item.getEnchantLevel()));
        }
    }

    private static PawnShopItem[] getOwnerItems(final Player owner) {
        return PAWN_SHOP_ITEMS.stream().filter(pawnShopItem -> pawnShopItem.getOwnerId() == owner.getObjectId() && isAllowedItem(pawnShopItem)).toArray(PawnShopItem[]::new);
    }

    private static void doSellList(final Player player, final NpcInstance npc, final int page) {
        final ItemInstance[] items = getSellableItems(player);
        final StringBuilder itemsHtmlBuilder = new StringBuilder();
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("mods/pawnshop/sell_list.htm");
        for (int itemIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page, lastItemIdx = itemIdx + Config.PAWNSHOP_ITEMS_PER_PAGE; itemIdx < lastItemIdx && itemIdx < items.length; ++itemIdx) {
            final ItemInstance item = items[itemIdx];
            itemsHtmlBuilder.append(itemHtml(player, "pawnshop.sell_item_element", String.format("-h scripts_services.pawnshop.PawnShop:sellItem %d %d", item.getObjectId(), page), item.getTemplate(), item.getEnchantLevel(), (int) item.getCount(), getVarOptSkill(item.getVariationStat1(), item.getVariationStat2())));
        }
        html.replace("%paging%", pagingHtml(player, page, items, "sellList"));
        html.replace("%list%", itemsHtmlBuilder.toString());
        player.sendPacket(html);
    }

    private static void doSellItem(final Player player, final NpcInstance npc, final int itemObjId, final int page) {
        if (!player.getPlayerAccess().UseTrade) {
            player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
            return;
        }
        if (itemObjId <= 0) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        final ItemInstance[] items = getSellableItems(player);
        ItemInstance item = null;
        for (final ItemInstance $item : items) {
            if ($item.getObjectId() == itemObjId) {
                item = $item;
            }
        }
        if (item == null || !isAllowedItem(item)) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        if (Config.PAWNSHOP_TAX_ITEM_ID > 0 && Config.PAWNSHOP_TAX_ITEM_COUNT > 0L && player.getInventory().getCountOf(Config.PAWNSHOP_TAX_ITEM_ID) < Config.PAWNSHOP_TAX_ITEM_COUNT) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_TAX_ITEM_COUNT).addItemName(Config.PAWNSHOP_TAX_ITEM_ID));
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("mods/pawnshop/sell_item.htm");
        html.replace("%back_page%", String.valueOf(page));
        html.replace("%sell_item_name%", item.getName());
        html.replace("%currency_list%", currencysHtmlList());
        html.replace("%item%", itemHtml(player, "pawnshop.sell_item", null, item.getTemplate(), item.getEnchantLevel(), (int) item.getCount(), getVarOptSkill(item.getVariationStat1(), item.getVariationStat2())));
        html.replace("%item_obj_id%", String.valueOf(item.getObjectId()));
        player.sendPacket(html);
    }

    private static void doSellApply(final Player player, final NpcInstance npc, final int itemObjId, final ItemTemplate currency, final int price) {
        if (!player.getPlayerAccess().UseTrade) {
            player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
            return;
        }
        if (itemObjId <= 0 || currency == null) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        final ItemInstance[] items = getSellableItems(player);
        ItemInstance item = null;
        for (final ItemInstance $item : items) {
            if ($item.getObjectId() == itemObjId) {
                item = $item;
            }
        }
        if (item == null || !isAllowedItem(item) || SafeMath.mulAndCheck((long) price, item.getCount()) <= 0L) {
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
            return;
        }
        if (Config.PAWNSHOP_TAX_ITEM_ID > 0 && Config.PAWNSHOP_TAX_ITEM_COUNT > 0L && ItemFunctions.removeItem(player, Config.PAWNSHOP_TAX_ITEM_ID, Config.PAWNSHOP_TAX_ITEM_COUNT, true) < Config.PAWNSHOP_TAX_ITEM_COUNT) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_TAX_ITEM_COUNT).addItemName(Config.PAWNSHOP_TAX_ITEM_ID));
            return;
        }
        final int amount = (int) item.getCount();
        final PawnShopItem psi = new PawnShopItem(LAST_ID.incrementAndGet(), player.getObjectId(), item.getItemId(), amount, item.getEnchantLevel(), currency.getItemId(), price, item.getVariationStat1(), item.getVariationStat2());
        Log.LogItem(player, ItemLog.RefundSell, item);
        if (!player.getInventory().destroyItem(item)) {
            return;
        }
        player.sendPacket(SystemMessage2.removeItems(psi.getItemTypeId(), (long) amount));
        PAWN_SHOP_ITEMS.add(0, psi);
        psi.store();
    }

    private static ItemInstance[] getSellableItems(final Player player) {
        return player.getInventory().getItems().stream().filter(item -> isAllowedItem(item) && item.getOwnerId() == player.getObjectId()).toArray(ItemInstance[]::new);
    }

    private static String stripString(final String str) {
        return str.replaceAll("[!\"#\\$%&'\\(\\)\\*,\\-\\./:;<=>\\?@\\[\\\\\\]\\^_`{\\|}~]+", "").replaceAll("[^\\w\\d+]+", " ").trim();
    }

    private static boolean isAllowedItem(final ItemInstance item) {
        return item != null && item.getLocation() == ItemLocation.INVENTORY && (!item.isAugmented() || Config.PAWNSHOP_ALLOW_SELL_AUGMENTED_ITEMS) && (!item.isWeapon() || item.getEnchantLevel() >= Config.PAWNSHOP_MIN_ENCHANT_LEVEL) && isAllowedItem(item.getTemplate());
    }

    private static boolean isAllowedItem(final PawnShopItem pawnShopItem) {
        return pawnShopItem != null && !pawnShopItem.getDeleted().get() && ArrayUtils.contains(Config.PAWNSHOP_CURRENCY_ITEM_IDS, pawnShopItem.getCurrencyItemId()) && isAllowedItem(pawnShopItem.getItemTemplate());
    }

    private static boolean isAllowedItem(final ItemTemplate item) {
        return item != null && !item.isTemporal() && !item.isShadowItem() && ArrayUtils.contains(Config.PAWNSHOP_ITEMS_CLASSES, item.getItemClass()) && item.getItemGrade().gradeOrd() >= Config.PAWNSHOP_MIN_GRADE.gradeOrd() && !ArrayUtils.contains(Config.PAWNSHOP_PROHIBITED_ITEM_IDS, item.getItemId());
    }

    private static String formatOptSkillText(final Skill optSkill) {
        if (optSkill == null) {
            return "";
        }
        if (optSkill.isActive()) {
            return "Active " + optSkill.getName();
        }
        if (!optSkill.getTriggerList().isEmpty()) {
            return "Chance " + optSkill.getName();
        }
        if (optSkill.isPassive()) {
            return "Passive " + optSkill.getName();
        }
        return optSkill.getName();
    }

    private static String itemHtml(final Player player, final String templateAddr, final String bypass, final ItemTemplate item, final int enchant, final int amount, final Skill optSkill) {
        String html = StringHolder.getInstance().getNotNull(player, templateAddr);
        html = html.replace("%bypass%", (bypass != null) ? bypass : "");
        html = html.replace("%item_id%", String.format("%d", item.getItemId()));
        html = html.replace("%item_name%", item.getName());
        html = html.replace("%item_add_name%", item.getAdditionalName());
        html = html.replace("%item_icon%", item.getIcon());
        html = html.replace("%item_enchant%", (enchant > 0) ? String.format("(+%d)", enchant) : "");
        html = html.replace("%item_amount%", String.valueOf(amount));
        html = html.replace("%option%", formatOptSkillText(optSkill));
        return html;
    }

    private static String itemHtml(final Player player, final String templateAddr, final String bypass, final ItemTemplate item, final int enchant, final int amount, final ItemTemplate currency, final long price, final String ownerName, final Skill optSkill) {
        String html = StringHolder.getInstance().getNotNull(player, templateAddr);
        html = html.replace("%bypass%", (bypass != null) ? bypass : "");
        html = html.replace("%item_id%", String.valueOf(item.getItemId()));
        html = html.replace("%item_name%", item.getName());
        html = html.replace("%item_add_name%", item.getAdditionalName());
        html = html.replace("%item_icon%", item.getIcon());
        html = html.replace("%item_enchant%", (enchant > 0) ? String.format("(+%d)", enchant) : "");
        html = html.replace("%item_amount%", String.valueOf(amount));
        html = html.replace("%option%", formatOptSkillText(optSkill));
        if (currency != null) {
            html = html.replace("%currency_id%", String.valueOf(currency.getItemId()));
            html = html.replace("%currency_name%", currency.getName());
            html = html.replace("%price%", String.valueOf(price));
        } else {
            html = html.replace("%currency_id%", "");
            html = html.replace("%currency_name%", "");
            html = html.replace("%price%", "");
        }
        html = html.replace("%owner%", (ownerName != null) ? ownerName : "");
        return html;
    }

    private static String currencysHtmlList() {
        return Arrays.stream(Config.PAWNSHOP_CURRENCY_ITEM_IDS).mapToObj(pawnshop_currency_item_id -> ItemTemplateHolder.getInstance().getTemplate(pawnshop_currency_item_id)).filter(Objects::nonNull).map(ItemTemplate::getName).collect(Collectors.joining(";"));
    }

    private static ItemTemplate getCurrencyItem(String s) {
        s = stripString(s);
        for (int cIdx = 0; cIdx < Config.PAWNSHOP_CURRENCY_ITEM_IDS.length; ++cIdx) {
            final ItemTemplate currencyItem = ItemTemplateHolder.getInstance().getTemplate(Config.PAWNSHOP_CURRENCY_ITEM_IDS[cIdx]);
            if (currencyItem != null) {
                if (s.equalsIgnoreCase(stripString(currencyItem.getName()))) {
                    return currencyItem;
                }
            }
        }
        return null;
    }

    private static boolean hasNextPage(final int length, final int page) {
        return (page + 1) * Config.PAWNSHOP_ITEMS_PER_PAGE < length;
    }

    private static String pagingHtml(final Player player, final int page, final Object[] items, final String method) {
        return pagingHtml(player, page, items, method, null);
    }

    private static String pagingHtml(final Player player, final int page, final Object[] items, final String method, final String args) {
        final String bypassFmt = "-h scripts_services.pawnshop.PawnShop:" + method + ((args != null) ? (" %d " + args) : " %d");
        return pagingHtml(player, (page > 0) ? String.format(bypassFmt, page - 1).trim() : null, page, hasNextPage(items.length, page) ? String.format(bypassFmt, page + 1).trim() : null);
    }

    private static String pagingHtml(final Player player, final String prevBypass, final int currPage, final String nextBypass) {
        String html = StringHolder.getInstance().getNotNull(player, "pawnshop.paging");
        html = html.replace("%prev_button%", (prevBypass != null) ? "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", prevBypass) : "");
        html = html.replace("%curr_page%", Integer.toString(currPage + 1));
        html = html.replace("%next_button%", (nextBypass != null) ? "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", nextBypass) : "");
        return html;
    }

    private static Skill getVarOptSkill(final int varOpt1, final int varOpt2) {
        Skill varOptSkill = null;
        if (varOpt1 > 0 || varOpt2 > 0) {
            final OptionDataTemplate odt1 = OptionDataHolder.getInstance().getTemplate(varOpt1);
            final OptionDataTemplate odt2 = OptionDataHolder.getInstance().getTemplate(varOpt2);
            if (odt2 != null && !odt2.getSkills().isEmpty()) {
                varOptSkill = odt2.getSkills().get(0);
            }
            if (odt1 != null && !odt1.getSkills().isEmpty()) {
                varOptSkill = odt1.getSkills().get(0);
            }
        }
        return varOptSkill;
    }

    @Override
    public void onInit() {
        if (Config.PAWNSHOP_ENABLED) {
            loadItems();
        }
    }

    public void buyList() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        doBuyList(player, npc, 0, "");
    }

    public void buyList(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final StringBuilder queryStrBuilder = new StringBuilder();
        final int[] ints = {0};
        parseArgs(args, ints, queryStrBuilder);
        doBuyList(player, npc, ints[0], queryStrBuilder.toString());
    }

    public void buyItem(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final StringBuilder queryStrBuilder = new StringBuilder();
        final int[] ints = {-1, 0};
        parseArgs(args, ints, queryStrBuilder);
        doBuyItem(player, ints[0]);
        doBuyList(player, npc, ints[1], queryStrBuilder.toString());
    }

    public void refundList() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        doRefundList(player, npc, 0);
    }

    public void refundList(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final int[] ints = {0};
        parseArgs(args, ints);
        doRefundList(player, npc, ints[0]);
    }

    public void refundItem(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final int[] ints = {-1, 0};
        parseArgs(args, ints);
        doRefundItem(player, ints[0]);
        doRefundList(player, npc, ints[1]);
    }

    public void sellList() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        doSellList(player, npc, 0);
    }

    public void sellList(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final int[] ints = {0};
        parseArgs(args, ints);
        doSellList(player, npc, ints[0]);
    }

    public void sellItem(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final int[] ints = {-1, 0};
        parseArgs(args, ints);
        doSellItem(player, npc, ints[0], ints[1]);
    }

    public void sellApply(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc)) {
            return;
        }
        final StringBuilder currSb = new StringBuilder();
        final int[] ints = {0, 0, -1};
        parseArgs(args, ints, currSb);
        doSellApply(player, npc, ints[1], getCurrencyItem(currSb.toString()), ints[2]);
        doSellList(player, npc, ints[0]);
    }

    private static class PawnShopItem {
        private final int _id;
        private final int _ownerId;
        private final int _itemTypeId;
        private final int _amount;
        private final int _enchantLevel;
        private final int _currencyItemId;
        private final int _price;
        private final int _varOpt1;
        private final int _varOpt2;
        private final AtomicBoolean _deleted;
        private final Skill _optSkill;
        private String _ownerName;
        private String _nameForQuery;

        private PawnShopItem(final int id, final int ownerId, final int itemTypeId, final int amount, final int enchantLevel, final int currencyItemId, final int price, final int varOpt1, final int varOpt2) {
            _id = id;
            _ownerId = ownerId;
            _itemTypeId = itemTypeId;
            _amount = amount;
            _enchantLevel = enchantLevel;
            _currencyItemId = currencyItemId;
            _price = price;
            _varOpt1 = varOpt1;
            _varOpt2 = varOpt2;
            _optSkill = getVarOptSkill(varOpt1, varOpt2);
            _deleted = new AtomicBoolean(false);
        }

        public AtomicBoolean getDeleted() {
            return _deleted;
        }

        public String getOwnerName() {
            if (_ownerName == null) {
                final Player player = World.getPlayer(getOwnerId());
                if (player != null) {
                    _ownerName = player.getName();
                } else if ((_ownerName = CharacterDAO.getInstance().getNameByObjectId(getOwnerId())) == null) {
                    _ownerName = "";
                }
            }
            return _ownerName;
        }

        public int getId() {
            return _id;
        }

        public int getOwnerId() {
            return _ownerId;
        }

        public int getItemTypeId() {
            return _itemTypeId;
        }

        public int getAmount() {
            return _amount;
        }

        public int getEnchantLevel() {
            return _enchantLevel;
        }

        public int getCurrencyItemId() {
            return _currencyItemId;
        }

        public int getPrice() {
            return _price;
        }

        public int getVarOpt1() {
            return _varOpt1;
        }

        public int getVarOpt2() {
            return _varOpt2;
        }

        public Skill getVariationSkill() {
            return _optSkill;
        }

        public ItemTemplate getItemTemplate() {
            return ItemTemplateHolder.getInstance().getTemplate(getItemTypeId());
        }

        public String getNameForQuery() {
            if (_nameForQuery == null) {
                final ItemTemplate item = getItemTemplate();
                if (item != null) {
                    _nameForQuery = stripString(item.getName());
                } else {
                    _nameForQuery = "";
                }
            }
            return _nameForQuery;
        }

        public void store() {
            Connection con = null;
            CallableStatement cstmt = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                cstmt = con.prepareCall("{CALL `lip_ex_PawnShopStoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
                cstmt.setInt(1, _id);
                cstmt.setInt(2, _ownerId);
                cstmt.setInt(3, _itemTypeId);
                cstmt.setInt(4, _amount);
                cstmt.setInt(5, _enchantLevel);
                cstmt.setInt(6, _currencyItemId);
                cstmt.setInt(7, _price);
                cstmt.setInt(8, _varOpt1);
                cstmt.setInt(9, _varOpt2);
                cstmt.execute();
            } catch (SQLException se) {
                LOGGER.error("", se);
            } finally {
                DbUtils.closeQuietly(con, cstmt);
            }
        }

        public void delete() {
            Connection con = null;
            CallableStatement cstmt = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                cstmt = con.prepareCall("{CALL `lip_ex_PawnShopDeleteItem`(?)}");
                cstmt.setInt(1, _id);
                cstmt.execute();
            } catch (SQLException se) {
                LOGGER.error("", se);
            } finally {
                DbUtils.closeQuietly(con, cstmt);
            }
            _deleted.set(true);
        }
    }
}
