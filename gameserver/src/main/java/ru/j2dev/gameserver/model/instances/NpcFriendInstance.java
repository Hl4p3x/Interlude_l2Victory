package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MyTargetSelected;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ValidateLocation;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.WarehouseFunctions;

import java.util.StringTokenizer;

public final class NpcFriendInstance extends MerchantInstance {
    public NpcFriendInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), new ValidateLocation(this));
            if (isAutoAttackable(player)) {
                player.sendPacket(makeStatusUpdate(9, 10));
            }
            player.sendActionFailed();
            return;
        }
        player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
        if (Events.onAction(player, this, shift)) {
            return;
        }
        if (isAutoAttackable(player)) {
            player.getAI().Attack(this, false, shift);
            return;
        }
        if (!isInActingRange(player)) {
            if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT) {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && !player.isGM()) {
            player.sendActionFailed();
            return;
        }
        if ((!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || player.isAlikeDead()) {
            return;
        }
        if (hasRandomAnimation()) {
            onRandomAnimation();
        }
        player.sendActionFailed();
        player.setLastNpcInteractionTime();
        String filename = "";
        if ((getNpcId() >= 31370 && getNpcId() <= 31376 && player.getVarka() > 0) || (getNpcId() >= 31377 && getNpcId() < 31384 && player.getKetra() > 0)) {
            filename = "npc_friend/" + getNpcId() + "-nofriend.htm";
            showChatWindow(player, filename);
            return;
        }
        switch (getNpcId()) {
            case 31370:
            case 31371:
            case 31373:
            case 31377:
            case 31378:
            case 31380:
            case 31553:
            case 31554: {
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31372: {
                if (player.getKetra() > 2) {
                    filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31379: {
                if (player.getVarka() > 2) {
                    filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31374: {
                if (player.getKetra() > 1) {
                    filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31381: {
                if (player.getVarka() > 1) {
                    filename = "npc_friend/" + getNpcId() + "-warehouse.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31375: {
                if (player.getKetra() == 3 || player.getKetra() == 4) {
                    filename = "npc_friend/" + getNpcId() + "-special1.htm";
                    break;
                }
                if (player.getKetra() == 5) {
                    filename = "npc_friend/" + getNpcId() + "-speciaru.j2dev.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31382: {
                if (player.getVarka() == 3 || player.getVarka() == 4) {
                    filename = "npc_friend/" + getNpcId() + "-special1.htm";
                    break;
                }
                if (player.getVarka() == 5) {
                    filename = "npc_friend/" + getNpcId() + "-speciaru.j2dev.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31376: {
                if (player.getKetra() == 4) {
                    filename = "npc_friend/" + getNpcId() + "-normal.htm";
                    break;
                }
                if (player.getKetra() == 5) {
                    filename = "npc_friend/" + getNpcId() + "-special.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31383: {
                if (player.getVarka() == 4) {
                    filename = "npc_friend/" + getNpcId() + "-normal.htm";
                    break;
                }
                if (player.getVarka() == 5) {
                    filename = "npc_friend/" + getNpcId() + "-special.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31555: {
                if (player.getRam() == 1) {
                    filename = "npc_friend/" + getNpcId() + "-special1.htm";
                    break;
                }
                if (player.getRam() == 2) {
                    filename = "npc_friend/" + getNpcId() + "-speciaru.j2dev.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
            case 31556: {
                if (player.getRam() == 2) {
                    filename = "npc_friend/" + getNpcId() + "-bufflist.htm";
                    break;
                }
                filename = "npc_friend/" + getNpcId() + ".htm";
                break;
            }
        }
        showChatWindow(player, filename);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();
        if ("Buff".equalsIgnoreCase(actualCommand)) {
            if (st.countTokens() < 1) {
                return;
            }
            final int val = Integer.parseInt(st.nextToken());
            int item = 0;
            switch (getNpcId()) {
                case 31372: {
                    item = 7186;
                    break;
                }
                case 31379: {
                    item = 7187;
                    break;
                }
                case 31556: {
                    item = 7251;
                    break;
                }
            }
            int skill = 0;
            int level = 0;
            long count = 0L;
            switch (val) {
                case 1: {
                    skill = 4359;
                    level = 2;
                    count = 2L;
                    break;
                }
                case 2: {
                    skill = 4360;
                    level = 2;
                    count = 2L;
                    break;
                }
                case 3: {
                    skill = 4345;
                    level = 3;
                    count = 3L;
                    break;
                }
                case 4: {
                    skill = 4355;
                    level = 2;
                    count = 3L;
                    break;
                }
                case 5: {
                    skill = 4352;
                    level = 1;
                    count = 3L;
                    break;
                }
                case 6: {
                    skill = 4354;
                    level = 3;
                    count = 3L;
                    break;
                }
                case 7: {
                    skill = 4356;
                    level = 1;
                    count = 6L;
                    break;
                }
                case 8: {
                    skill = 4357;
                    level = 2;
                    count = 6L;
                    break;
                }
            }
            if (skill != 0 && player.getInventory().destroyItemByItemId(item, count)) {
                player.doCast(SkillTable.getInstance().getInfo(skill, level), player, true);
            } else {
                showChatWindow(player, "npc_friend/" + getNpcId() + "-havenotitems.htm");
            }
        } else if (command.startsWith("Chat")) {
            final int val = Integer.parseInt(command.substring(5));
            String fname;
            fname = "npc_friend/" + getNpcId() + "-" + val + ".htm";
            if (!"".equals(fname)) {
                showChatWindow(player, fname);
            }
        } else if (command.startsWith("Buy")) {
            final int val = Integer.parseInt(command.substring(4));
            showShopWindow(player, val, false);
        } else if ("Sell".equalsIgnoreCase(actualCommand)) {
            showShopWindow(player);
        } else if (command.startsWith("WithdrawP")) {
            final int val = Integer.parseInt(command.substring(10));
            if (val == 99) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("npc-friend/personal.htm");
                html.replace("%npcname%", getName());
                player.sendPacket(html);
            } else {
                WarehouseFunctions.showRetrieveWindow(player, val);
            }
        } else if ("DepositP".equals(command)) {
            WarehouseFunctions.showDepositWindow(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
