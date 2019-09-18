package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.MapRegionManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.mapregion.DomainArea;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.StringTokenizer;

public class MerchantInstance extends NpcInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantInstance.class);
    private static final int NEWBIE_EXCHANGE_MULTISELL = 6001;

    public MerchantInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        if (getTemplate().getHtmRoot() != null) {
            return getTemplate().getHtmRoot() + pom + ".htm";
        }
        String temp = "merchant/" + pom + ".htm";
        if (HtmCache.getInstance().getNullable(temp, player) != null) {
            return temp;
        }
        temp = "teleporter/" + pom + ".htm";
        if (HtmCache.getInstance().getNullable(temp, player) != null) {
            return temp;
        }
        temp = "petmanager/" + pom + ".htm";
        if (HtmCache.getInstance().getNullable(temp, player) != null) {
            return temp;
        }
        return "default/" + pom + ".htm";
    }

    private void showWearWindow(final Player player, final int val) {
        if (!player.getPlayerAccess().UseShop) {
            return;
        }
        final NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);
        if (list != null) {
            final ShopPreviewList bl = new ShopPreviewList(list, player);
            player.sendPacket(bl);
        } else {
            LOGGER.warn("no buylist with id:" + val);
            player.sendActionFailed();
        }
    }

    protected void showShopWindow(final Player player, final int listId, final boolean tax) {
        if (!player.getPlayerAccess().UseShop) {
            return;
        }
        double taxRate = 0.0;
        if (tax) {
            final Castle castle = getCastle(player);
            if (castle != null) {
                taxRate = castle.getTaxRate();
            }
        }
        final NpcTradeList list = BuyListHolder.getInstance().getBuyList(listId);
        if (list == null || list.getNpcId() == getNpcId()) {
            player.sendPacket(new BuyList(list, player, taxRate));
        } else {
            LOGGER.warn("[MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
            LOGGER.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
        }
    }

    protected void showShopWindow(final Player player) {
        showShopWindow(player, 0, false);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();
        if ("Buy".equalsIgnoreCase(actualCommand)) {
            int val = 0;
            if (st.countTokens() > 0) {
                val = Integer.parseInt(st.nextToken());
            }
            showShopWindow(player, val, true);
        } else if ("Sell".equalsIgnoreCase(actualCommand)) {
            player.sendPacket(new SellRefundList(player, false));
        } else if ("Wear".equalsIgnoreCase(actualCommand)) {
            if (st.countTokens() < 1) {
                return;
            }
            final int val = Integer.parseInt(st.nextToken());
            showWearWindow(player, val);
        } else if ("Multisell".equalsIgnoreCase(actualCommand)) {
            if (st.countTokens() < 1) {
                return;
            }
            final int val = Integer.parseInt(st.nextToken());
            final Castle castle = getCastle(player);
            MultiSellHolder.getInstance().SeparateAndSend(val, player, (castle != null) ? castle.getTaxRate() : 0.0);
        } else if ("Exchange".equalsIgnoreCase(actualCommand)) {
            if (player.getLevel() < 25) {
                MultiSellHolder.getInstance().SeparateAndSend(NEWBIE_EXCHANGE_MULTISELL, player, 0.0);
            } else {
                player.sendPacket(new NpcHtmlMessage(player, this, "merchant/merchant_for_newbie001.htm", 0));
            }
        } else if ("ReceivePremium".equalsIgnoreCase(actualCommand)) {
            if (player.getPremiumItemList().isEmpty()) {
                player.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
                return;
            }
            player.sendPacket(new ExGetPremiumItemList(player));
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public Castle getCastle(final Player player) {
        if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX || (getReflection() == ReflectionManager.GIRAN_HARBOR && Config.SERVICES_GIRAN_HARBOR_NOTAX)) {
            return null;
        }
        if (getReflection() == ReflectionManager.GIRAN_HARBOR) {
            final String var = player.getVar("backCoords");
            if (var != null && !var.isEmpty()) {
                final Location loc = Location.parseLoc(var);
                final DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, loc);
                if (domain != null) {
                    return ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
                }
            }
            return super.getCastle();
        }
        return super.getCastle(player);
    }

    @Override
    public boolean isMerchantNpc() {
        return true;
    }
}
