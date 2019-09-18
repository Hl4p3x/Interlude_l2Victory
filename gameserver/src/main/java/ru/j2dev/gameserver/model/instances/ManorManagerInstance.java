package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.manor.SeedProduction;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ManorManagerInstance extends MerchantInstance {
    public ManorManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), new ValidateLocation(this));
        } else {
            final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
            if (!isInActingRange(player)) {
                if (!player.getAI().isIntendingInteract(this)) {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                }
                player.sendActionFailed();
            } else {
                if (CastleManorManager.getInstance().isDisabled()) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("npcdefault.htm");
                    html.replace("%objectId%", String.valueOf(getObjectId()));
                    html.replace("%npcname%", getName());
                    player.sendPacket(html);
                } else if (!player.isGM() && player.isClanLeader() && getCastle() != null && getCastle().getOwnerId() == player.getClanId()) {
                    showMessageWindow(player, "manager-lord.htm");
                } else {
                    showMessageWindow(player, "manager.htm");
                }
                player.sendActionFailed();
            }
        }
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("manor_menu_select")) {
            if (CastleManorManager.getInstance().isUnderMaintenance()) {
                player.sendPacket(ActionFail.STATIC, Msg.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
                return;
            }
            final String params = command.substring(command.indexOf("?") + 1);
            final StringTokenizer st = new StringTokenizer(params, "&");
            final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
            final int state = Integer.parseInt(st.nextToken().split("=")[1]);
            final int time = Integer.parseInt(st.nextToken().split("=")[1]);
            final Castle castle = getCastle();
            int castleId;
            if (state == -1) {
                castleId = castle.getId();
            } else {
                castleId = state;
            }
            switch (ask) {
                case 1: {
                    if (castleId != castle.getId()) {
                        player.sendPacket(Msg._HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
                        break;
                    }
                    final NpcTradeList tradeList = new NpcTradeList(0);
                    final List<SeedProduction> seeds = castle.getSeedProduction(0);
                    for (final SeedProduction s : seeds) {
                        final TradeItem item = new TradeItem();
                        item.setItemId(s.getId());
                        item.setOwnersPrice(s.getPrice());
                        item.setCount(s.getCanProduce());
                        if (item.getCount() > 0L && item.getOwnersPrice() > 0L) {
                            tradeList.addItem(item);
                        }
                    }
                    final BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
                    player.sendPacket(bl);
                    break;
                }
                case 2: {
                    player.sendPacket(new ExShowSellCropList(player, castleId, castle.getCropProcure(0)));
                    break;
                }
                case 3: {
                    if (time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved()) {
                        player.sendPacket(new ExShowSeedInfo(castleId, Collections.emptyList()));
                        break;
                    }
                    player.sendPacket(new ExShowSeedInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getSeedProduction(time)));
                    break;
                }
                case 4: {
                    if (time == 1 && !ResidenceHolder.getInstance().getResidence(Castle.class, castleId).isNextPeriodApproved()) {
                        player.sendPacket(new ExShowCropInfo(castleId, Collections.emptyList()));
                        break;
                    }
                    player.sendPacket(new ExShowCropInfo(castleId, ResidenceHolder.getInstance().getResidence(Castle.class, castleId).getCropProcure(time)));
                    break;
                }
                case 5: {
                    player.sendPacket(new ExShowManorDefaultInfo());
                    break;
                }
                case 6: {
                    showShopWindow(player, Integer.parseInt("3" + getNpcId()), false);
                    break;
                }
                case 9: {
                    player.sendPacket(new ExShowProcureCropDetail(state));
                    break;
                }
            }
        } else if (command.startsWith("help")) {
            final StringTokenizer st2 = new StringTokenizer(command, " ");
            st2.nextToken();
            final String filename = "manor_client_help00" + st2.nextToken() + ".htm";
            showMessageWindow(player, filename);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath() {
        return "manormanager/";
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        return "manormanager/manager.htm";
    }

    private void showMessageWindow(final Player player, final String filename) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(getHtmlPath() + filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcId%", String.valueOf(getNpcId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}
