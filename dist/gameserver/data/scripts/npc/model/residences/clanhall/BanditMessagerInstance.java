package npc.model.residences.clanhall;

import quests._504_CompetitionForTheBanditStronghold;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.dao.SiegePlayerDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.List;
import java.util.StringTokenizer;

public class BanditMessagerInstance extends NpcInstance {
    public BanditMessagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        final ClanHall clanHall = getClanHall();
        final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
        final Clan clan = player.getClan();
        if ("registrationMenu".equalsIgnoreCase(command)) {
            if (!checkCond(player, true)) {
                return;
            }
            showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_1.htm");
        } else if ("registerAsClan".equalsIgnoreCase(command)) {
            if (!checkCond(player, false)) {
                return;
            }
            final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
            final CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
            if (siegeClan != null) {
                showFlagInfo(player, siegeClans.indexOf(siegeClan));
                return;
            }
            final QuestState questState = player.getQuestState(_504_CompetitionForTheBanditStronghold.class);
            if (questState == null || questState.getQuestItemsCount(5009) != 1L) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_24.htm");
                return;
            }
            questState.exitCurrentQuest(true);
            register(player);
        } else if ("registerByOffer".equalsIgnoreCase(command)) {
            if (!checkCond(player, false)) {
                return;
            }
            final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
            final CTBSiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
            if (siegeClan != null) {
                showFlagInfo(player, siegeClans.indexOf(siegeClan));
                return;
            }
            if (!player.consumeItem(57, 200000L)) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_26.htm");
                return;
            }
            register(player);
        } else if ("viewNpc".equalsIgnoreCase(command)) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_7.htm");
                return;
            }
            String file;
            switch ((int) siegeClan2.getParam()) {
                case 0: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_10.htm";
                    break;
                }
                case 35428: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_11.htm";
                    break;
                }
                case 35429: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_12.htm";
                    break;
                }
                case 35430: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_13.htm";
                    break;
                }
                case 35431: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_14.htm";
                    break;
                }
                case 35432: {
                    file = "residence2/clanhall/agit_oel_mahum_messeger_15.htm";
                    break;
                }
                default: {
                    return;
                }
            }
            showChatWindow(player, file);
        } else if (command.startsWith("formAlliance")) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_7.htm");
                return;
            }
            if (siegeClan2.getClan().getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm");
                return;
            }
            final StringTokenizer t = new StringTokenizer(command);
            t.nextToken();
            final int npcId = Integer.parseInt(t.nextToken());
            siegeClan2.setParam(npcId);
            SiegeClanDAO.getInstance().update(clanHall, siegeClan2);
            showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm");
        } else if ("registerAsMember".equalsIgnoreCase(command)) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_7.htm");
                return;
            }
            if (siegeClan2.getClan().getLeaderId() == player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_5.htm");
                return;
            }
            if (siegeClan2.getPlayers().contains(player.getObjectId())) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm");
            } else {
                if (siegeClan2.getPlayers().size() >= 18) {
                    showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_8.htm");
                    return;
                }
                siegeClan2.getPlayers().add(player.getObjectId());
                SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm");
            }
        } else if ("listClans".equalsIgnoreCase(command)) {
            final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
            msg.setFile("residence2/clanhall/azit_messenger003.htm");
            final List<CTBSiegeClanObject> siegeClans2 = siegeEvent.getObjects("attackers");
            for (int i = 0; i < 5; ++i) {
                CTBSiegeClanObject siegeClan3 = null;
                try {
                    siegeClan3 = siegeClans2.get(i);
                } catch (IndexOutOfBoundsException ignored) {

                }
                if (siegeClan3 != null) {
                    msg.replace("%clan_" + i + "%", siegeClan3.getClan().getName());
                } else {
                    msg.replaceNpcString("%clan_" + i + "%", NpcString.__UNREGISTERED__);
                }
                msg.replace("%clan_count_" + i + "%", (siegeClan3 == null) ? "" : String.valueOf(siegeClan3.getPlayers().size()));
            }
            player.sendPacket(msg);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private boolean checkCond(final Player player, final boolean regMenu) {
        final Clan clan = player.getClan();
        final ClanHall clanHall = getClanHall();
        final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
        final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
        final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
        if (siegeEvent.isRegistrationOver()) {
            showChatWindow(player, "quests/_504_CompetitionForTheBanditStronghold/azit_messenger_q0504_03.htm", "%siege_time%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
            return false;
        }
        if (regMenu && siegeClan != null) {
            return true;
        }
        if (clan == null || player.getObjectId() != clan.getLeaderId()) {
            showChatWindow(player, "quests/_504_CompetitionForTheBanditStronghold/azit_messenger_q0504_05.htm");
            return false;
        }
        if (player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4) {
            showChatWindow(player, "quests/_504_CompetitionForTheBanditStronghold/azit_messenger_q0504_04.htm");
            return false;
        }
        if (clan.getHasHideout() == clanHall.getId()) {
            showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_22.htm");
            return false;
        }
        if (clan.getHasHideout() > 0) {
            showChatWindow(player, "quests/_504_CompetitionForTheBanditStronghold/azit_messenger_q0504_10.htm");
            return false;
        }
        if (siegeClans.size() >= 5) {
            showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_21.htm");
            return false;
        }
        return true;
    }

    private void register(final Player player) {
        final Clan clan = player.getClan();
        final ClanHall clanHall = getClanHall();
        final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
        final CTBSiegeClanObject siegeClan = new CTBSiegeClanObject("attackers", clan, 0L);
        siegeClan.getPlayers().add(player.getObjectId());
        siegeEvent.addObject("attackers", siegeClan);
        SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
        SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
        final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
        showFlagInfo(player, siegeClans.indexOf(siegeClan));
    }

    private void showFlagInfo(final Player player, final int index) {
        String file;
        switch (index) {
            case 0: {
                file = "residence2/clanhall/agit_oel_mahum_messeger_4a.htm";
                break;
            }
            case 1: {
                file = "residence2/clanhall/agit_oel_mahum_messeger_4b.htm";
                break;
            }
            case 2: {
                file = "residence2/clanhall/agit_oel_mahum_messeger_4c.htm";
                break;
            }
            case 3: {
                file = "residence2/clanhall/agit_oel_mahum_messeger_4d.htm";
                break;
            }
            case 4: {
                file = "residence2/clanhall/agit_oel_mahum_messeger_4e.htm";
                break;
            }
            default: {
                return;
            }
        }
        showChatWindow(player, file);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final Clan clan = getClanHall().getOwner();
        if (clan != null) {
            showChatWindow(player, "residence2/clanhall/azit_messenger001.htm", "%owner_name%", clan.getName());
        } else {
            showChatWindow(player, "residence2/clanhall/azit_messenger002.htm");
        }
    }
}
