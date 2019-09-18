package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.*;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager.NobleRecord;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExHeroList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Arrays;

public class OlympiadManagerInstance extends NpcInstance {
    private static final int OLYMPIAD_MANAGER_ID = 31688;
    private static final int[] OLYMPIAD_MONUMENT_IDS;
    private static final int HERO_CIRCLE = 6842;
    private static final int[] TOP_RANK_CLOAKS = {31274, 31275, 31275};
    private static Logger LOGGER = LoggerFactory.getLogger(OlympiadManagerInstance.class);

    static {
        Arrays.sort(OLYMPIAD_MONUMENT_IDS = new int[]{31690, 31769, 31770, 31771, 31772});
    }

    public OlympiadManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    private Player[] createTeam(final Player leader) {
        final Player[] ret = new Player[3];
        if (leader.getParty() == null) {
            leader.sendMessage(new CustomMessage("THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET_TO_PARTICIPATE_IN_A_TEAM", leader));
            return null;
        }
        if (!leader.getParty().isLeader(leader)) {
            leader.sendMessage(new CustomMessage("ONLY_A_PARTY_LEADER_CAN_REQUEST_A_TEAM_MATCH", leader));
            return null;
        }
        if (!checkMatchLimit(leader, OlympiadGameType.TEAM_CLASS_FREE)) {
            return null;
        }
        ret[0] = leader;
        int i = 0;
        for (final Player pm : leader.getParty().getPartyMembers()) {
            if (!checkMatchLimit(pm, OlympiadGameType.TEAM_CLASS_FREE)) {
                return null;
            }
            if (pm == leader) {
                continue;
            }
            if (++i >= 3) {
                leader.sendMessage(new CustomMessage("THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET_TO_PARTICIPATE_IN_A_TEAM", leader));
                return null;
            }
            ret[i] = pm;
        }
        return ret;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (!Config.OLY_ENABLED) {
            return;
        }
        SystemMessage sm;
        if (command.startsWith("olympiad hwpn_")) {
            boolean already_got = false;
            final int item_id = Integer.parseInt(command.substring(9));
            int reward_id = 0;
            for (final int hid : HeroManager.HERO_WEAPONS) {
                if (player.getInventory().getItemByItemId(hid) != null || player.getWarehouse().getCountOf(hid) > 0L) {
                    already_got = true;
                }
                if (hid == item_id) {
                    reward_id = hid;
                }
            }
            if (already_got) {
                showChatWindow(player, 51);
            }
            if (player.isHero() && reward_id > 0) {
                player.getInventory().addItem(reward_id, 1L);
                player.sendPacket(SystemMessage2.obtainItems(reward_id, 1L, 0));
            }
            return;
        }
        if (command.startsWith("olympiad ")) {
            final int cmdID = Integer.parseInt(command.substring(4));
            if (getNpcId() == OLYMPIAD_MANAGER_ID) {
                switch (cmdID) {
                    case 100: {
                        if (OlympiadSystemManager.getInstance().isRegAllowed()) {
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            if (!OlympiadPlayersManager.getInstance().isRegistred(player) && !player.isOlyParticipant()) {
                                html.setFile("olympiad/olympiad_operator100.htm");
                                html.replace("%period%", String.valueOf(OlympiadSystemManager.getInstance().getCurrentPeriod()));
                                html.replace("%season%", String.valueOf(OlympiadSystemManager.getInstance().getCurrentSeason()));
                                html.replace("%particicnt%", String.valueOf(OlympiadSystemManager.getInstance().getPartCount()));
                                html.replace("%currpartcnt%", String.valueOf(OlympiadPlayersManager.getInstance().getParticipantCount()));
                            } else {
                                html.setFile("olympiad/olympiad_operator110.htm");
                            }
                            player.sendPacket(html);
                            break;
                        }
                        player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                        break;
                    }
                    case 101: {
                        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
                            player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                            break;
                        }
                        if (!checkMatchLimit(player, OlympiadGameType.CLASS_INDIVIDUAL)) {
                            break;
                        }
                        player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                        sm = OlympiadGameManager.getInstance().AddParticipationRequest(OlympiadGameType.CLASS_INDIVIDUAL, new Player[]{player});
                        if (sm != null) {
                            player.sendPacket(sm);
                            break;
                        }
                        break;
                    }
                    case 102: {
                        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
                            player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                            break;
                        }
                        if (!checkMatchLimit(player, OlympiadGameType.CLASS_FREE)) {
                            break;
                        }
                        sm = OlympiadGameManager.getInstance().AddParticipationRequest(OlympiadGameType.CLASS_FREE, new Player[]{player});
                        if (sm != null) {
                            player.sendPacket(sm);
                            break;
                        }
                        break;
                    }
                    case 103: {
                        if (OlympiadSystemManager.getInstance().isRegAllowed()) {
                            final Player[] participants = createTeam(player);
                            if (participants != null) {
                                sm = OlympiadGameManager.getInstance().AddParticipationRequest(OlympiadGameType.TEAM_CLASS_FREE, participants);
                                if (sm != null) {
                                    player.sendPacket(sm);
                                }
                            }
                            break;
                        }
                        player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                        break;
                    }
                    case 104: {
                        if (OlympiadSystemManager.getInstance().isRegAllowed()) {
                            final OlympiadGameType ctype = OlympiadPlayersManager.getInstance().getCompTypeOf(player);
                            if (ctype != null) {
                                OlympiadPlayersManager.getInstance().removeEntryByPlayer(ctype, player);
                                player.sendPacket(Msg.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
                            }
                            break;
                        }
                        player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                        break;
                    }
                    case 200: {
                        if (OlympiadSystemManager.getInstance().isRegAllowed()) {
                            OlympiadGameManager.getInstance().showCompetitionList(player);
                            break;
                        }
                        player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
                        break;
                    }
                    case 301: {
                        final NpcHtmlMessage phtml = new NpcHtmlMessage(player, this);
                        final int rcount = NoblessManager.getInstance().getNoblessePasses(player);
                        if (rcount > 0) {
                            phtml.setFile(getHtmlPath(getNpcId(), 301, player));
                            phtml.replace("%points%", String.valueOf(rcount));
                            player.getInventory().addItem(Config.OLY_VICTORY_RITEMID, rcount);
                            player.sendPacket(SystemMessage2.obtainItems(Config.OLY_VICTORY_RITEMID, rcount, 0));
                        } else {
                            phtml.setFile(getHtmlPath(getNpcId(), 302, player));
                        }
                        player.sendPacket(phtml);
                        break;
                    }
                    case 1902:
                    case 1903: {
                        MultiSellHolder.getInstance().SeparateAndSend(cmdID, player, 0.0);
                        break;
                    }
                    default: {
                        if (cmdID >= 588 && cmdID <= 634) {
                            final int class_id = cmdID - 500;
                            final NpcHtmlMessage rhtml = new NpcHtmlMessage(player, this);
                            rhtml.setFile("olympiad/olympiad_operator_rank_class.htm");
                            final String[] rlist = NoblessManager.getInstance().getClassLeaders(class_id);
                            String Name;
                            String Rank;
                            for (int i = 0; i < 15; ++i) {
                                if (i < rlist.length) {
                                    Name = rlist[i];
                                    Rank = String.valueOf(i + 1);
                                } else {
                                    Name = "";
                                    Rank = "";
                                }
                                rhtml.replace("<?Rank" + (i + 1) + "?>", Rank);
                                rhtml.replace("<?Name" + (i + 1) + "?>", Name);
                            }
                            player.sendPacket(rhtml);
                            break;
                        }
                        showChatWindow(player, cmdID);
                        break;
                    }
                }
            } else if (Arrays.binarySearch(OLYMPIAD_MONUMENT_IDS, getNpcId()) >= 0) {
                switch (cmdID) {
                    case 1000:
                        player.sendPacket(new ExHeroList());
                        break;
                    case 2000:
                        if (OlympiadSystemManager.getInstance().isCalculationPeriod()) {
                            showChatWindow(player, 11);
                        } else if (HeroManager.getInstance().isInactiveHero(player)) {
                            HeroManager.getInstance().activateHero(player);
                            showChatWindow(player, 10);
                        } else {
                            showChatWindow(player, 1);
                        }
                        break;
                    case 3:
                        if (player.isHero()) {
                            if (player.getInventory().getItemByItemId(HERO_CIRCLE) != null || player.getWarehouse().getCountOf(HERO_CIRCLE) > 0L) {
                                showChatWindow(player, 55);
                            } else {
                                player.getInventory().addItem(HERO_CIRCLE, 1L);
                                player.sendPacket(SystemMessage2.obtainItems(HERO_CIRCLE, 1L, 0));
                            }
                        } else {
                            showChatWindow(player, 3);
                        }
                        break;
                    case 4:
                        if (player.isHero()) {
                            boolean already_got2 = false;
                            for (final int hid2 : HeroManager.HERO_WEAPONS) {
                                if (player.getInventory().getItemByItemId(hid2) != null || player.getWarehouse().getCountOf(hid2) > 0L) {
                                    already_got2 = true;
                                }
                            }
                            showChatWindow(player, already_got2 ? 51 : 50);
                        } else {
                            showChatWindow(player, 4);
                        }
                        break;
                    case 5:
                        if (player.isNoble()) {
                            final int rank = NoblessManager.getInstance().getPlayerClassRank(player.getBaseClassId(), player.getObjectId());
                            if (rank < 0 || rank >= TOP_RANK_CLOAKS.length) {
                                showChatWindow(player, 5);
                            } else {
                                final int cloakItemId = TOP_RANK_CLOAKS[rank];
                                if (player.getInventory().getItemByItemId(cloakItemId) != null || player.getWarehouse().getCountOf(cloakItemId) > 0L) {
                                    showChatWindow(player, 5);
                                } else {
                                    player.getInventory().addItem(cloakItemId, 1L);
                                    player.sendPacket(SystemMessage2.obtainItems(cloakItemId, 1L, 0));
                                }
                            }
                        } else {
                            showChatWindow(player, 5);
                        }
                        break;
                    default:
                        showChatWindow(player, cmdID);
                        break;
                }
            }
            return;
        }
        super.onBypassFeedback(player, command);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... replace) {
        if ((val == 0 || val == 100) && !player.isNoble()) {
            super.showChatWindow(player, 900);
        } else {
            super.showChatWindow(player, val);
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        return String.format((getNpcId() == 31688) ? "olympiad/olympiad_operator%03d.htm" : "olympiad/olympiad_monument%03d.htm", val);
    }

    private boolean checkMatchLimit(final Player player, final OlympiadGameType type) {
        if (player == null) {
            return false;
        }
        final NobleRecord nr = NoblessManager.getInstance().getNobleRecord(player.getObjectId());
        if (nr.class_based_cnt + nr.class_free_cnt + nr.team_cnt > Config.OLY_MAX_TOTAL_MATCHES) {
            player.sendPacket(SystemMsg.THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70);
            return false;
        }
        switch (type) {
            case CLASS_FREE: {
                if (nr.class_free_cnt > Config.OLY_CF_MATCHES) {
                    player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
                    return false;
                }
                break;
            }
            case CLASS_INDIVIDUAL: {
                if (nr.class_based_cnt > Config.OLY_CB_MATCHES) {
                    player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
                    return false;
                }
                break;
            }
            case TEAM_CLASS_FREE: {
                if (nr.team_cnt > Config.OLY_TB_MATCHES) {
                    player.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
                    return false;
                }
                break;
            }
        }
        return true;
    }
}
