package services;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.ItemTemplate.ItemClass;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Util;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBroker extends Functions implements INpcDialogAppender {
    private static final int MAX_ITEMS_PER_PAGE = 10;
    private static final int MAX_PAGES_PER_LIST = 9;
    private static final Map<Integer, NpcInfo> _npcInfos = new ConcurrentHashMap<>();

    public int[] RARE_ITEMS;

    public ItemBroker() {
        RARE_ITEMS = new int[]{16205, 16206, 16207, 16208, 16209, 16210, 16211, 16212, 16213, 16214, 16215, 16216, 16217, 16218, 16219, 16220, 16304, 16321, 16338, 16355};
    }

    private TreeMap<String, TreeMap<Long, Item>> getItems(final int type) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return null;
        }
        updateInfo(player, npc);
        final NpcInfo info = _npcInfos.get(getNpc().getObjectId());
        if (info == null) {
            return null;
        }
        switch (type) {
            case 1: {
                return info.bestSellItems;
            }
            case 3: {
                return info.bestBuyItems;
            }
            case 5: {
                return info.bestCraftItems;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (!Config.ITEM_BROKER_ITEM_SEARCH) {
            return "";
        }
        final StringBuilder append = new StringBuilder();
        int type = 0;
        String typeNameRu = "";
        String typeNameEn = "";
        switch (val) {
            case 0: {
                if (player.isLangRus()) {
                    append.append("<br><font color=\"LEVEL\">\u041f\u043e\u0438\u0441\u043a \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0435\u0432:</font><br1>");
                    append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">\u0421\u043f\u0438\u0441\u043e\u043a \u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0445 \u0442\u043e\u0432\u0430\u0440\u043e\u0432</font>]<br1>");
                    append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">\u0421\u043f\u0438\u0441\u043e\u043a \u043f\u043e\u043a\u0443\u043f\u0430\u0435\u043c\u044b\u0445 \u0442\u043e\u0432\u0430\u0440\u043e\u0432</font>]<br1>");
                    append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0445 \u0442\u043e\u0432\u0430\u0440\u043e\u0432</font>]<br1>");
                    break;
                }
                append.append("<br><font color=\"LEVEL\">Search for dealers:</font><br1>");
                append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">The list of goods for sale</font>]<br1>");
                append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">The list of goods to buy</font>]<br1>");
                append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">The list of goods to craft</font>]<br1>");
                break;
            }
            case 11: {
                type = 1;
                typeNameRu = "\u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0445";
                typeNameEn = "sell";
                break;
            }
            case 13: {
                type = 3;
                typeNameRu = "\u043f\u043e\u043a\u0443\u043f\u0430\u0435\u043c\u044b\u0445";
                typeNameEn = "buy";
                break;
            }
            case 15: {
                type = 5;
                typeNameRu = "\u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0435\u043c\u044b\u0445";
                typeNameEn = "craft";
                break;
            }
            case 21:
            case 23:
            case 25: {
                type = val - 20;
                if (player.isLangRus()) {
                    append.append("!\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u044f:<br>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 0 0|<font color=\"FF9900\">\u041e\u0440\u0443\u0436\u0438\u0435</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 0 0|<font color=\"FF9900\">\u0411\u0440\u043e\u043d\u044f</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 0 0|<font color=\"FF9900\">\u0411\u0438\u0436\u0443\u0442\u0435\u0440\u0438\u044f</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 0 0|<font color=\"FF9900\">\u0423\u043a\u0440\u0430\u0448\u0435\u043d\u0438\u044f</font>]<br1>");
                    append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">\u041d\u0430\u0437\u0430\u0434</font>]");
                } else {
                    append.append("!The list of equipment:<br>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 0 0|<font color=\"FF9900\">Weapons</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 0 0|<font color=\"FF9900\">Armors</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 0 0|<font color=\"FF9900\">Jewels</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 0 0|<font color=\"FF9900\">Accessories</font>]<br1>");
                    append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Back</font>]");
                }
                return append.toString();
            }
            case 31:
            case 33:
            case 35: {
                type = val - 30;
                if (player.isLangRus()) {
                    append.append("!\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u044f, \u0437\u0430\u0442\u043e\u0447\u0435\u043d\u043d\u043e\u0433\u043e \u043d\u0430 +4 \u0438 \u0432\u044b\u0448\u0435:<br>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 4 0|<font color=\"FF9900\">\u041e\u0440\u0443\u0436\u0438\u0435</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 4 0|<font color=\"FF9900\">\u0411\u0440\u043e\u043d\u044f</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 4 0|<font color=\"FF9900\">\u0411\u0438\u0436\u0443\u0442\u0435\u0440\u0438\u044f</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 4 0|<font color=\"FF9900\">\u0423\u043a\u0440\u0430\u0448\u0435\u043d\u0438\u044f</font>]<br1>");
                    append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">\u041d\u0430\u0437\u0430\u0434</font>]");
                } else {
                    append.append("!The list of equipment, enchanted to +4 and more:<br>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 4 0|<font color=\"FF9900\">Weapons+</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 4 0|<font color=\"FF9900\">Armors+</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 4 0|<font color=\"FF9900\">Jewels+</font>]<br1>");
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 4 0|<font color=\"FF9900\">Accessories+</font>]<br1>");
                    append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Back</font>]");
                }
                return append.toString();
            }
        }
        if (type > 0) {
            if (player.isLangRus()) {
                append.append("!\u0421\u043f\u0438\u0441\u043e\u043a ").append(typeNameRu).append(" \u0442\u043e\u0432\u0430\u0440\u043e\u0432:<br>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 0|<font color=\"FF9900\">\u0412\u0435\u0441\u044c \u0441\u043f\u0438\u0441\u043e\u043a</font>]<br1>");
                append.append("[npc_%objectId%_Chat ").append(type + 20).append("|<font color=\"FF9900\">\u0421\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u0435</font>]<br1>");
                if (type == 1) {
                    append.append("[npc_%objectId%_Chat ").append(type + 30).append("|<font color=\"FF9900\">\u0421\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u0435 +4 \u0438 \u0432\u044b\u0448\u0435</font>]<br1>");
                }
                if (type != 5) {
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 1|<font color=\"FF9900\">\u0420\u0435\u0434\u043a\u043e\u0435 \u0441\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u0435</font>]<br1>");
                }
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 0 0|<font color=\"FF9900\">\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u044b\u0435 \u043c\u0430\u0442\u0435\u0440\u0438\u0430\u043b\u044b</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 0 0|<font color=\"FF9900\">\u0418\u043d\u0433\u0440\u0435\u0434\u0438\u0435\u043d\u0442\u044b</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 0 0|<font color=\"FF9900\">\u041a\u043b\u044e\u0447\u0435\u0432\u044b\u0435 \u0438\u043d\u0433\u0440\u0435\u0434\u0438\u0435\u043d\u0442\u044b</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 0 0|<font color=\"FF9900\">\u0420\u0435\u0446\u0435\u043f\u0442\u044b</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 0 0|<font color=\"FF9900\">\u041a\u043d\u0438\u0433\u0438 \u0438 \u0430\u043c\u0443\u043b\u0435\u0442\u044b</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 10 1 0 0|<font color=\"FF9900\">\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0434\u043b\u044f \u0443\u043b\u0443\u0447\u0448\u0435\u043d\u0438\u044f</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 11 1 0 0|<font color=\"FF9900\">\u0420\u0430\u0437\u043d\u043e\u0435</font>]<br1>");
                if (type != 5) {
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 90 1 0 0|<font color=\"FF9900\">\u0421\u0442\u0430\u043d\u0434\u0430\u0440\u0442\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b</font>]<br1>");
                }
                append.append("<edit var=\"tofind\" width=100><br1>");
                append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 $tofind|<font color=\"FF9900\">\u041d\u0430\u0439\u0442\u0438</font>]<br1>");
                append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">\u041d\u0430\u0437\u0430\u0434</font>]");
            } else {
                append.append("!The list of goods to ").append(typeNameEn).append(":<br>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 0|<font color=\"FF9900\">List all</font>]<br1>");
                append.append("[npc_%objectId%_Chat ").append(type + 20).append("|<font color=\"FF9900\">Equipment</font>]<br1>");
                if (type == 1) {
                    append.append("[npc_%objectId%_Chat ").append(type + 30).append("|<font color=\"FF9900\">Equipment +4 and more</font>]<br1>");
                }
                if (type != 5) {
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 1|<font color=\"FF9900\">Rare equipment</font>]<br1>");
                }
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 0 0|<font color=\"FF9900\">Consumable</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 0 0|<font color=\"FF9900\">Matherials</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 0 0|<font color=\"FF9900\">Key matherials</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 0 0|<font color=\"FF9900\">Recipies</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 0 0|<font color=\"FF9900\">Books and amulets</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 10 1 0 0|<font color=\"FF9900\">Enchant items</font>]<br1>");
                append.append("[scripts_services.ItemBroker:list ").append(type).append(" 11 1 0 0|<font color=\"FF9900\">Other</font>]<br1>");
                if (type != 5) {
                    append.append("[scripts_services.ItemBroker:list ").append(type).append(" 90 1 0 0|<font color=\"FF9900\">Commons</font>]<br1>");
                }
                append.append("<edit var=\"tofind\" width=100><br1>");
                append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 $tofind|<font color=\"FF9900\">Find</font>]<br1>");
                append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Back</font>]");
            }
        }
        return append.toString();
    }

    public void list(final String[] var) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (var.length != 5) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u0434\u0430\u043d\u043d\u044b\u0445", player, npc);
            return;
        }
        int type;
        int itemType;
        int currentPage;
        int minEnchant;
        int rare;
        try {
            type = Integer.valueOf(var[0]);
            itemType = Integer.valueOf(var[1]);
            currentPage = Integer.valueOf(var[2]);
            minEnchant = Integer.valueOf(var[3]);
            rare = Integer.valueOf(var[4]);
        } catch (Exception e) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435", player, npc);
            return;
        }
        final ItemClass itemClass = (itemType >= ItemClass.values().length) ? null : ItemClass.values()[itemType];
        final TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
        if (allItems == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0442\u0430\u043a\u043e\u0433\u043e \u0442\u0438\u043f\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e", player, npc);
            return;
        }
        final List<Item> items = new ArrayList<>(allItems.size() * 10);
        for (final TreeMap<Long, Item> tempItems : allItems.values()) {
            final TreeMap<Long, Item> tempItems2 = new TreeMap<>();
            for (final Entry<Long, Item> entry : tempItems.entrySet()) {
                final Item tempItem = entry.getValue();
                if (tempItem == null) {
                    continue;
                }
                if (tempItem.enchant < minEnchant) {
                    continue;
                }
                final ItemTemplate temp = (tempItem.item != null) ? tempItem.item.getItem() : ItemTemplateHolder.getInstance().getTemplate(tempItem.itemId);
                if (temp == null) {
                    continue;
                }
                if (rare > 0 && !tempItem.rare) {
                    continue;
                }
                if (itemClass != null && itemClass != ItemClass.ALL && temp.getItemClass() != itemClass) {
                    continue;
                }
                tempItems2.put(entry.getKey(), tempItem);
            }
            if (tempItems2.isEmpty()) {
                continue;
            }
            final Item item = (type == 3) ? tempItems2.lastEntry().getValue() : tempItems2.firstEntry().getValue();
            if (item == null) {
                continue;
            }
            items.add(item);
        }
        final StringBuilder out = new StringBuilder(200);
        out.append("[npc_%objectId%_Chat 1");
        out.append(type);
        out.append("|««]&nbsp;&nbsp;");
        int totalPages = items.size();
        totalPages = totalPages / 10 + ((totalPages % 10 > 0) ? 1 : 0);
        totalPages = Math.max(1, totalPages);
        currentPage = Math.min(totalPages, Math.max(1, currentPage));
        if (totalPages > 1) {
            int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
            if (page > 1) {
                listPageNum(out, type, itemType, 1, minEnchant, rare, "1");
            }
            if (currentPage > 11) {
                listPageNum(out, type, itemType, currentPage - 10, minEnchant, rare, String.valueOf(currentPage - 10));
            }
            if (currentPage > 1) {
                listPageNum(out, type, itemType, currentPage - 1, minEnchant, rare, "<");
            }
            for (int count = 0; count < 9 && page <= totalPages; ++count, ++page) {
                if (page == currentPage) {
                    out.append(page).append("&nbsp;");
                } else {
                    listPageNum(out, type, itemType, page, minEnchant, rare, String.valueOf(page));
                }
            }
            if (currentPage < totalPages) {
                listPageNum(out, type, itemType, currentPage + 1, minEnchant, rare, ">");
            }
            if (currentPage < totalPages - 10) {
                listPageNum(out, type, itemType, currentPage + 10, minEnchant, rare, String.valueOf(currentPage + 10));
            }
            if (page <= totalPages) {
                listPageNum(out, type, itemType, totalPages, minEnchant, rare, String.valueOf(totalPages));
            }
        }
        out.append("<table width=100%>");
        if (items.size() > 0) {
            int count2 = 0;
            final ListIterator<Item> iter = items.listIterator((currentPage - 1) * 10);
            while (iter.hasNext() && count2 < 10) {
                final Item item2 = iter.next();
                final ItemTemplate temp2 = (item2.item != null) ? item2.item.getItem() : ItemTemplateHolder.getInstance().getTemplate(item2.itemId);
                if (temp2 == null) {
                    continue;
                }
                out.append("<tr><td>");
                out.append(temp2.getIcon32());
                out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ");
                out.append(type);
                out.append(" ");
                out.append(item2.itemId);
                out.append(" ");
                out.append(minEnchant);
                out.append(" ");
                out.append(rare);
                out.append(" ");
                out.append(itemType);
                out.append(" 1 ");
                out.append(currentPage);
                out.append("|");
                out.append(item2.name);
                out.append("</td></tr><tr><td>price: ");
                out.append(Util.formatAdena(item2.price));
                if (temp2.isStackable()) {
                    out.append(", count: ").append(Util.formatAdena(item2.count));
                }
                out.append("</td></tr></table></td></tr>");
                ++count2;
            }
        } else if (player.isLangRus()) {
            out.append("<tr><td colspan=2>\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.</td></tr>");
        } else {
            out.append("<tr><td colspan=2>Nothing found.</td></tr>");
        }
        out.append("</table><br>&nbsp;");
        show(out.toString(), player, npc);
    }

    private void listPageNum(final StringBuilder out, final int type, final int itemType, final int page, final int minEnchant, final int rare, final String letter) {
        out.append("[scripts_services.ItemBroker:list ");
        out.append(type);
        out.append(" ");
        out.append(itemType);
        out.append(" ");
        out.append(page);
        out.append(" ");
        out.append(minEnchant);
        out.append(" ");
        out.append(rare);
        out.append("|");
        out.append(letter);
        out.append("]&nbsp;");
    }

    public void listForItem(final String[] var) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (var.length < 7 || var.length > 12) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u0434\u0430\u043d\u043d\u044b\u0445", player, npc);
            return;
        }
        String[] search = null;
        int type;
        int itemId;
        int minEnchant;
        int rare;
        int itemType;
        int currentPage;
        int returnPage;
        try {
            type = Integer.valueOf(var[0]);
            itemId = Integer.valueOf(var[1]);
            minEnchant = Integer.valueOf(var[2]);
            rare = Integer.valueOf(var[3]);
            itemType = Integer.valueOf(var[4]);
            currentPage = Integer.valueOf(var[5]);
            returnPage = Integer.valueOf(var[6]);
            if (var.length > 7) {
                search = new String[var.length - 7];
                System.arraycopy(var, 7, search, 0, search.length);
            }
        } catch (Exception e) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435", player, npc);
            return;
        }
        final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
        if (template == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - itemId \u043d\u0435 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d.", player, npc);
            return;
        }
        final TreeMap<String, TreeMap<Long, Item>> tmpItems = getItems(type);
        if (tmpItems == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u0442\u0430\u043a\u043e\u0439 \u0442\u0438\u043f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442.", player, npc);
            return;
        }
        final TreeMap<Long, Item> allItems = tmpItems.get(template.getName());
        if (allItems == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0441 \u0442\u0430\u043a\u0438\u043c \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.", player, npc);
            return;
        }
        final StringBuilder out = new StringBuilder(200);
        if (search == null) {
            listPageNum(out, type, itemType, returnPage, minEnchant, rare, "««");
        } else {
            findPageNum(out, type, returnPage, search, "««");
        }
        out.append("&nbsp;&nbsp;");
        final NavigableMap<Long, Item> sortedItems = (type == 3) ? allItems.descendingMap() : allItems;
        if (sortedItems == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.", player, npc);
            return;
        }
        final List<Item> items = new ArrayList<>(sortedItems.size());
        for (final Item item : sortedItems.values()) {
            if (item != null && item.enchant >= minEnchant) {
                if (rare > 0 && !item.rare) {
                    continue;
                }
                items.add(item);
            }
        }
        int totalPages = items.size();
        totalPages = totalPages / 10 + ((totalPages % 10 > 0) ? 1 : 0);
        totalPages = Math.max(1, totalPages);
        currentPage = Math.min(totalPages, Math.max(1, currentPage));
        if (totalPages > 1) {
            int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
            if (page > 1) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, 1, returnPage, search, "1");
            }
            if (currentPage > 11) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage - 10, returnPage, search, String.valueOf(currentPage - 10));
            }
            if (currentPage > 1) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage - 1, returnPage, search, "<");
            }
            for (int count = 0; count < 9 && page <= totalPages; ++count, ++page) {
                if (page == currentPage) {
                    out.append(page).append("&nbsp;");
                } else {
                    listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, page, returnPage, search, String.valueOf(page));
                }
            }
            if (currentPage < totalPages) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage + 1, returnPage, search, ">");
            }
            if (currentPage < totalPages - 10) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage + 10, returnPage, search, String.valueOf(currentPage + 10));
            }
            if (page <= totalPages) {
                listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, totalPages, returnPage, search, String.valueOf(totalPages));
            }
        }
        out.append("<table width=100%>");
        if (items.size() > 0) {
            int count2 = 0;
            final ListIterator<Item> iter = items.listIterator((currentPage - 1) * 10);
            while (iter.hasNext() && count2 < 10) {
                final Item item2 = iter.next();
                final ItemTemplate temp = (item2.item != null) ? item2.item.getItem() : ItemTemplateHolder.getInstance().getTemplate(item2.itemId);
                if (temp == null) {
                    continue;
                }
                out.append("<tr><td>");
                out.append(temp.getIcon32());
                out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:path ");
                out.append(type);
                out.append(" ");
                out.append(item2.itemId);
                out.append(" ");
                out.append(item2.itemObjId);
                out.append("|");
                out.append(item2.name);
                out.append("</td></tr><tr><td>price: ");
                out.append(Util.formatAdena(item2.price));
                if (temp.isStackable()) {
                    out.append(", count: ").append(Util.formatAdena(item2.count));
                }
                out.append(", owner: ").append(item2.merchantName);
                out.append("</td></tr></table></td></tr>");
                ++count2;
            }
        } else if (player.isLangRus()) {
            out.append("<tr><td colspan=2>\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.</td></tr>");
        } else {
            out.append("<tr><td colspan=2>Nothing found.</td></tr>");
        }
        out.append("</table><br>&nbsp;");
        show(out.toString(), player, npc);
    }

    private void listForItemPageNum(final StringBuilder out, final int type, final int itemId, final int minEnchant, final int rare, final int itemType, final int page, final int returnPage, final String[] search, final String letter) {
        out.append("[scripts_services.ItemBroker:listForItem ");
        out.append(type);
        out.append(" ");
        out.append(itemId);
        out.append(" ");
        out.append(minEnchant);
        out.append(" ");
        out.append(rare);
        out.append(" ");
        out.append(itemType);
        out.append(" ");
        out.append(page);
        out.append(" ");
        out.append(returnPage);
        if (search != null) {
            for (String aSearch : search) {
                out.append(" ");
                out.append(aSearch);
            }
        }
        out.append("|");
        out.append(letter);
        out.append("]&nbsp;");
    }

    public void path(final String[] var) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (var.length != 3) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u0434\u0430\u043d\u043d\u044b\u0445", player, npc);
            return;
        }
        int type;
        int itemId;
        int itemObjId;
        try {
            type = Integer.valueOf(var[0]);
            itemId = Integer.valueOf(var[1]);
            itemObjId = Integer.valueOf(var[2]);
        } catch (Exception e) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435", player, npc);
            return;
        }
        final ItemTemplate temp = ItemTemplateHolder.getInstance().getTemplate(itemId);
        if (temp == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - itemId \u043d\u0435 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d.", player, npc);
            return;
        }
        final TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
        if (allItems == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0442\u0430\u043a\u043e\u0433\u043e \u0442\u0438\u043f\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.", player, npc);
            return;
        }
        final TreeMap<Long, Item> items = allItems.get(temp.getName());
        if (items == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0441 \u0442\u0430\u043a\u0438\u043c \u0438\u043c\u0435\u043d\u0435\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.", player, npc);
            return;
        }
        Item item = null;
        for (final Item i : items.values()) {
            if (i.itemObjId == itemObjId) {
                item = i;
                break;
            }
        }
        if (item == null) {
            show("\u041e\u0448\u0438\u0431\u043a\u0430 - \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d.", player, npc);
            return;
        }
        boolean found = false;
        final Player trader = GameObjectsStorage.getPlayer(item.merchantStoredId);
        if (trader == null) {
            show("\u0422\u043e\u0440\u0433\u043e\u0432\u0435\u0446 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d, \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u043e\u043d \u0432\u044b\u0448\u0435\u043b \u0438\u0437 \u0438\u0433\u0440\u044b.", player, npc);
            return;
        }
        switch (type) {
            case 1: {
                if (trader.getSellList() != null) {
                    for (final TradeItem tradeItem : trader.getSellList()) {
                        if (tradeItem.getItemId() == item.itemId && tradeItem.getOwnersPrice() == item.price) {
                            found = true;
                            break;
                        }
                    }
                    break;
                }
                break;
            }
            case 3: {
                if (trader.getBuyList() != null) {
                    for (final TradeItem tradeItem : trader.getBuyList()) {
                        if (tradeItem.getItemId() == item.itemId && tradeItem.getOwnersPrice() == item.price) {
                            found = true;
                            break;
                        }
                    }
                    break;
                }
                break;
            }
            case 5: {
                found = true;
                break;
            }
        }
        if (!found) {
            if (player.isLangRus()) {
                show("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435, \u0446\u0435\u043d\u0430 \u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0438\u0437\u043c\u0435\u043d\u0438\u043b\u0438\u0441\u044c, \u0431\u0443\u0434\u044c\u0442\u0435 \u043e\u0441\u0442\u043e\u0440\u043e\u0436\u043d\u044b !", player, npc);
            } else {
                show("Caution, price or item was changed, please be careful !", player, npc);
            }
        }
        final RadarControl rc = new RadarControl(0, 1, item.player);
        player.sendPacket(rc);
        if (player.getVarB("notraders")) {
            player.sendPacket(new CharInfo(trader));
            switch (trader.getPrivateStoreType()) {
                case 3:
                    player.sendPacket(new PrivateStoreMsgBuy(trader));
                    break;
                case 1:
                case 8:
                    player.sendPacket(new PrivateStoreMsgSell(trader));
                    break;
                case 5:
                    player.sendPacket(new RecipeShopMsg(trader));
                    break;
            }
        }
        player.setTarget(trader);
    }

    public void updateInfo(final Player player, final NpcInstance npc) {
        NpcInfo info = _npcInfos.get(npc.getObjectId());
        if (info == null || info.lastUpdate < System.currentTimeMillis() - Config.ITEM_BROKER_UPDATE_TIME) {
            info = new NpcInfo();
            info.lastUpdate = System.currentTimeMillis();
            info.bestBuyItems = new TreeMap<>();
            info.bestSellItems = new TreeMap<>();
            info.bestCraftItems = new TreeMap<>();
            int itemObjId = 0;
            for (final Player pl : World.getAroundPlayers(npc, 4000, 400)) {
                final int type = pl.getPrivateStoreType();
                if (type == 1 || type == 3 || type == 5) {
                    TreeMap<String, TreeMap<Long, Item>> items;
                    List<TradeItem> tradeList;
                    switch (type) {
                        case 1: {
                            items = info.bestSellItems;
                            tradeList = pl.getSellList();
                            for (final TradeItem item : tradeList) {
                                final ItemTemplate temp = item.getItem();
                                if (temp == null) {
                                    continue;
                                }
                                TreeMap<Long, Item> oldItems = items.computeIfAbsent(temp.getName(), k -> new TreeMap<>());
                                Item newItem;
                                long key;
                                for (newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getObjectId(), pl.getName(), pl.getLoc(), item.getObjectId(), item), key = newItem.price * 100L; key < newItem.price * 100L + 100L && oldItems.containsKey(key); ++key) {
                                }
                                oldItems.put(key, newItem);
                            }
                            continue;
                        }
                        case 3: {
                            items = info.bestBuyItems;
                            tradeList = pl.getBuyList();
                            for (final TradeItem item : tradeList) {
                                final ItemTemplate temp = item.getItem();
                                if (temp == null) {
                                    continue;
                                }
                                TreeMap<Long, Item> oldItems = items.computeIfAbsent(temp.getName(), k -> new TreeMap<>());
                                Item newItem;
                                long key;
                                for (newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getObjectId(), pl.getName(), pl.getLoc(), itemObjId++, item), key = newItem.price * 100L; key < newItem.price * 100L + 100L && oldItems.containsKey(key); ++key) {
                                }
                                oldItems.put(key, newItem);
                            }
                            continue;
                        }
                        case 5: {
                            items = info.bestCraftItems;
                            final List<ManufactureItem> createList = pl.getCreateList();
                            if (createList == null) {
                                continue;
                            }
                            for (final ManufactureItem mitem : createList) {
                                final int recipeId = mitem.getRecipeId();
                                final Recipe recipe = RecipeHolder.getInstance().getRecipeById(recipeId);
                                if (recipe == null) {
                                    continue;
                                }
                                for (final Pair<ItemTemplate, Long> product : recipe.getProducts()) {
                                    final ItemTemplate temp2 = product.getKey();
                                    if (temp2 == null) {
                                        continue;
                                    }
                                    TreeMap<Long, Item> oldItems2 = items.computeIfAbsent(temp2.getName(), k -> new TreeMap<>());
                                    Item newItem2;
                                    long key2;
                                    for (newItem2 = new Item(product.getKey().getItemId(), type, mitem.getCost(), product.getValue(), 0, temp2.getName(), pl.getObjectId(), pl.getName(), pl.getLoc(), itemObjId++, null), key2 = newItem2.price * 100L; key2 < newItem2.price * 100L + 100L && oldItems2.containsKey(key2); ++key2) {
                                    }
                                    oldItems2.put(key2, newItem2);
                                }
                            }
                            continue;
                        }
                        default: {
                        }
                    }
                }
            }
            _npcInfos.put(npc.getObjectId(), info);
        }
    }

    public void find(final String[] var) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        if (var.length < 3 || var.length > 7) {
            if (player.isLangRus()) {
                show("Пожалуйста введите от 1 до 16 символов.<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Назад</font>]", player, npc);
            } else {
                show("Please enter from 1 up to 16 symbols.<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Back</font>]", player, npc);
            }
            return;
        }
        int minEnchant = 0;
        String[] search;
        int type;
        int currentPage;
        try {
            type = Integer.valueOf(var[0]);
            currentPage = Integer.valueOf(var[1]);
            search = new String[var.length - 2];
            for (int i = 0; i < search.length; ++i) {
                final String line = var[i + 2].trim().toLowerCase();
                search[i] = line;
                if (line.length() > 1 && line.startsWith("+")) {
                    minEnchant = Integer.valueOf(line.substring(1));
                }
            }
        } catch (Exception e) {
            show("Некорректные данные", player, npc);
            return;
        }
        final TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
        if (allItems == null) {
            show("Ошибка - предметов с таким типом не найдено.", player, npc);
            return;
        }
        final List<Item> items = new ArrayList<>();
        mainLoop:
        for (final Entry<String, TreeMap<Long, Item>> entry : allItems.entrySet()) {
            for (final String line2 : search) {
                if (!line2.startsWith("+")) {
                    if (!entry.getKey().toLowerCase().contains(line2)) {
                        continue mainLoop;
                    }
                }
            }
            final TreeMap<Long, Item> itemMap = entry.getValue();
            Item item = null;
            for (final Item itm : itemMap.values()) {
                if (itm != null && itm.enchant >= minEnchant) {
                    item = itm;
                    break;
                }
            }
            if (item != null) {
                items.add(item);
            }
        }
        final StringBuilder out = new StringBuilder(200);
        out.append("[npc_%objectId%_Chat 1");
        out.append(type);
        out.append("|««]&nbsp;&nbsp;");
        int totalPages = items.size();
        totalPages = totalPages / 10 + ((totalPages % 10 > 0) ? 1 : 0);
        totalPages = Math.max(1, totalPages);
        currentPage = Math.min(totalPages, Math.max(1, currentPage));
        if (totalPages > 1) {
            int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
            if (page > 1) {
                findPageNum(out, type, 1, search, "1");
            }
            if (currentPage > 11) {
                findPageNum(out, type, currentPage - 10, search, String.valueOf(currentPage - 10));
            }
            if (currentPage > 1) {
                findPageNum(out, type, currentPage - 1, search, "<");
            }
            for (int count = 0; count < 9 && page <= totalPages; ++count, ++page) {
                if (page == currentPage) {
                    out.append(page).append("&nbsp;");
                } else {
                    findPageNum(out, type, page, search, String.valueOf(page));
                }
            }
            if (currentPage < totalPages) {
                findPageNum(out, type, currentPage + 1, search, ">");
            }
            if (currentPage < totalPages - 10) {
                findPageNum(out, type, currentPage + 10, search, String.valueOf(currentPage + 10));
            }
            if (page <= totalPages) {
                findPageNum(out, type, totalPages, search, String.valueOf(totalPages));
            }
        }
        out.append("<table width=100%>");
        if (items.size() > 0) {
            int count2 = 0;
            final ListIterator<Item> iter = items.listIterator((currentPage - 1) * 10);
            while (iter.hasNext() && count2 < 10) {
                final Item item = iter.next();
                final ItemTemplate temp = (item.item != null) ? item.item.getItem() : ItemTemplateHolder.getInstance().getTemplate(item.itemId);
                if (temp == null) {
                    continue;
                }
                out.append("<tr><td>");
                out.append(temp.getIcon32());
                out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ");
                out.append(type);
                out.append(" ");
                out.append(item.itemId);
                out.append(" ");
                out.append(minEnchant);
                out.append(" 0 0 1 ");
                out.append(currentPage);
                for (String aSearch : search) {
                    out.append(" ");
                    out.append(aSearch);
                }
                out.append("|");
                out.append("<font color=\"LEVEL\">");
                out.append(temp.getName());
                out.append("</font>]");
                out.append("</td></tr>");
                out.append("</table></td></tr>");
                ++count2;
            }
        } else if (player.isLangRus()) {
            out.append("<tr><td colspan=2>Ничего не найдено.</td></tr>");
        } else {
            out.append("<tr><td colspan=2>Nothing found.</td></tr>");
        }
        out.append("</table><br>&nbsp;");
        show(out.toString(), player, npc);
    }

    private void findPageNum(final StringBuilder out, final int type, final int page, final String[] search, final String letter) {
        out.append("[scripts_services.ItemBroker:find ");
        out.append(type);
        out.append(" ");
        out.append(page);
        if (search != null) {
            for (String aSearch : search) {
                out.append(" ");
                out.append(aSearch);
            }
        }
        out.append("|");
        out.append(letter);
        out.append("]&nbsp;");
    }

    @Override
    public List<Integer> getNpcIds() {
        return Arrays.asList(31732, 31833, 31838, 31829, 31805);
    }

    public class NpcInfo {
        public long lastUpdate;
        public TreeMap<String, TreeMap<Long, Item>> bestSellItems;
        public TreeMap<String, TreeMap<Long, Item>> bestBuyItems;
        public TreeMap<String, TreeMap<Long, Item>> bestCraftItems;
    }

    public class Item {
        public final int itemId;
        public final int itemObjId;
        public final int type;
        public final long price;
        public final long count;
        public final int enchant;
        public final boolean rare;
        public final int merchantStoredId;
        public final String name;
        public final String merchantName;
        public final Location player;
        public final TradeItem item;

        public Item(final int itemId, final int type, final long price, final long count, final int enchant, final String itemName, final int storedId, final String merchantName, final Location player, final int itemObjId, final TradeItem item) {
            this.itemId = itemId;
            this.type = type;
            this.price = price;
            this.count = count;
            this.enchant = enchant;
            rare = ArrayUtils.contains(RARE_ITEMS, itemId);
            final StringBuilder out = new StringBuilder(70);
            if (enchant > 0) {
                if (rare) {
                    out.append("<font color=\"FF0000\">+");
                } else {
                    out.append("<font color=\"7CFC00\">+");
                }
                out.append(enchant);
                out.append(" ");
            } else if (rare) {
                out.append("<font color=\"0000FF\">Rare ");
            } else {
                out.append("<font color=\"LEVEL\">");
            }
            out.append(itemName);
            out.append("</font>]");
            if (item != null) {
                if (item.getAttackElement() != Element.NONE.getId()) {
                    out.append(" &nbsp;<font color=\"7CFC00\">+");
                    out.append(item.getAttackElementValue());
                    switch (item.getAttackElement()) {
                        case 0: {
                            out.append(" Fire");
                            break;
                        }
                        case 1: {
                            out.append(" Water");
                            break;
                        }
                        case 2: {
                            out.append(" Wind");
                            break;
                        }
                        case 3: {
                            out.append(" Earth");
                            break;
                        }
                        case 4: {
                            out.append(" Holy");
                            break;
                        }
                        case 5: {
                            out.append(" Unholy");
                            break;
                        }
                    }
                    out.append("</font>");
                } else {
                    final int fire = item.getDefenceFire();
                    final int water = item.getDefenceWater();
                    final int wind = item.getDefenceWind();
                    final int earth = item.getDefenceEarth();
                    final int holy = item.getDefenceHoly();
                    final int unholy = item.getDefenceUnholy();
                    if (fire + water + wind + earth + holy + unholy > 0) {
                        out.append("&nbsp;<font color=\"7CFC00\">");
                        if (fire > 0) {
                            out.append("+");
                            out.append(fire);
                            out.append(" Fire ");
                        }
                        if (water > 0) {
                            out.append("+");
                            out.append(water);
                            out.append(" Water ");
                        }
                        if (wind > 0) {
                            out.append("+");
                            out.append(wind);
                            out.append(" Wind ");
                        }
                        if (earth > 0) {
                            out.append("+");
                            out.append(earth);
                            out.append(" Earth ");
                        }
                        if (holy > 0) {
                            out.append("+");
                            out.append(holy);
                            out.append(" Holy ");
                        }
                        if (unholy > 0) {
                            out.append("+");
                            out.append(unholy);
                            out.append(" Unholy ");
                        }
                        out.append("</font>");
                    }
                }
            }
            name = out.toString();
            merchantStoredId = storedId;
            this.merchantName = merchantName;
            this.player = player;
            this.itemObjId = itemObjId;
            this.item = item;
        }
    }
}
