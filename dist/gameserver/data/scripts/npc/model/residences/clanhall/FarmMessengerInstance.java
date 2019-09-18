package npc.model.residences.clanhall;

import quests._655_AGrandPlanForTamingWildBeasts;
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
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.List;
import java.util.StringTokenizer;

public class FarmMessengerInstance extends NpcInstance {
    public FarmMessengerInstance(final int objectId, final NpcTemplate template) {
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
            showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_1.htm");
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
            final QuestState questState = player.getQuestState(_655_AGrandPlanForTamingWildBeasts.class);
            if (questState == null || questState.getQuestItemsCount(8293) != 1L) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_27.htm");
                return;
            }
            questState.exitCurrentQuest(true);
            register(player);
        } else if ("registerAsMember".equalsIgnoreCase(command)) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
                return;
            }
            if (siegeClan2.getClan().getLeaderId() == player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_5.htm");
                return;
            }
            if (siegeClan2.getPlayers().contains(player.getObjectId())) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm");
            } else {
                if (siegeClan2.getPlayers().size() >= 18) {
                    showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_8.htm");
                    return;
                }
                siegeClan2.getPlayers().add(player.getObjectId());
                SiegePlayerDAO.getInstance().insert(clanHall, clan.getClanId(), player.getObjectId());
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_9.htm");
            }
        } else if (command.startsWith("formAlliance")) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
                return;
            }
            if (siegeClan2.getClan().getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm");
                return;
            }
            if (siegeClan2.getParam() > 0L) {
                return;
            }
            final StringTokenizer t = new StringTokenizer(command);
            t.nextToken();
            final int npcId = Integer.parseInt(t.nextToken());
            siegeClan2.setParam(npcId);
            SiegeClanDAO.getInstance().update(clanHall, siegeClan2);
            showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_9.htm");
        } else if ("setNpc".equalsIgnoreCase(command)) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
                return;
            }
            if (siegeClan2.getClan().getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/agit_oel_mahum_messeger_10.htm");
                return;
            }
            showChatWindow(player, npcDialog(siegeClan2));
        } else if ("viewNpc".equalsIgnoreCase(command)) {
            final CTBSiegeClanObject siegeClan2 = siegeEvent.getSiegeClan("attackers", player.getClan());
            if (siegeClan2 == null) {
                showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_7.htm");
                return;
            }
            String file;
            if (siegeClan2.getParam() == 0L) {
                file = "residence2/clanhall/agit_oel_mahum_messeger_10.htm";
            } else {
                file = npcDialog(siegeClan2);
            }
            showChatWindow(player, file);
        } else if ("listClans".equalsIgnoreCase(command)) {
            final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
            msg.setFile("residence2/clanhall/farm_messenger003.htm");
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
                file = "residence2/clanhall/farm_kel_mahum_messenger_4a.htm";
                break;
            }
            case 1: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_4b.htm";
                break;
            }
            case 2: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_4c.htm";
                break;
            }
            case 3: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_4d.htm";
                break;
            }
            case 4: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_4e.htm";
                break;
            }
            default: {
                return;
            }
        }
        showChatWindow(player, file);
    }

    private String npcDialog(final SiegeClanObject siegeClanObject) {
        String file = null;
        switch ((int) siegeClanObject.getParam()) {
            case 0: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_6.htm";
                break;
            }
            case 35618: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_17.htm";
                break;
            }
            case 35619: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_18.htm";
                break;
            }
            case 35620: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_19.htm";
                break;
            }
            case 35621: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_20.htm";
                break;
            }
            case 35622: {
                file = "residence2/clanhall/farm_kel_mahum_messenger_23.htm";
                break;
            }
        }
        return file;
    }

    private boolean checkCond(final Player player, final boolean regMenu) {
        final Clan clan = player.getClan();
        final ClanHall clanHall = getClanHall();
        final ClanHallTeamBattleEvent siegeEvent = clanHall.getSiegeEvent();
        final List<CTBSiegeClanObject> siegeClans = siegeEvent.getObjects("attackers");
        final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
        if (siegeEvent.isRegistrationOver()) {
            showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_11.htm", "%siege_time%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
            return false;
        }
        if (regMenu && siegeClan != null) {
            return true;
        }
        if (clan == null || player.getObjectId() != clan.getLeaderId()) {
            showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_03.htm");
            return false;
        }
        if (player.getClan().isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return false;
        }
        if (player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4) {
            showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_05.htm");
            return false;
        }
        if (clan.getHasHideout() == clanHall.getId()) {
            showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_22.htm");
            return false;
        }
        if (clan.getHasHideout() > 0) {
            showChatWindow(player, "quests/_655_AGrandPlanForTamingWildBeasts/farm_messenger_q0655_04.htm");
            return false;
        }
        if (siegeClans.size() >= 5) {
            showChatWindow(player, "residence2/clanhall/farm_kel_mahum_messenger_21.htm");
            return false;
        }
        return true;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final Clan clan = getClanHall().getOwner();
        if (clan != null) {
            showChatWindow(player, "residence2/clanhall/farm_messenger001.htm", "%owner_name%", clan.getName());
        } else {
            showChatWindow(player, "residence2/clanhall/farm_messenger002.htm");
        }
    }
}
