package quests;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.utils.TimeUtils;

public class _504_CompetitionForTheBanditStronghold extends Quest {
    private static final int MESSENGER = 35437;
    private static final int TARLK_BUGBEAR = 20570;
    private static final int TARLK_BUGBEAR_WARRIOR = 20571;
    private static final int TARLK_BUGBEAR_HIGH_WARRIOR = 20572;
    private static final int TARLK_BASILISK = 20573;
    private static final int ELDER_TARLK_BASILISK = 20574;
    private static final int AMULET = 4332;
    private static final int ALIANCE_TROPHEY = 5009;
    private static final int CONTEST_CERTIFICATE = 4333;

    public _504_CompetitionForTheBanditStronghold() {
        super(2);
        addStartNpc(35437);
        addTalkId(35437);
        addKillId(20570);
        addKillId(20571);
        addKillId(20572);
        addKillId(20573);
        addKillId(20574);
        addQuestItem(4333, 4332, 5009);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("azit_messenger_q0504_02.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.giveItems(4333, 1L);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        final Player player = st.getPlayer();
        final Clan clan = player.getClan();
        final ClanHall clanhall = ResidenceHolder.getInstance().getResidence(35);
        if (clanhall.getSiegeEvent().isRegistrationOver()) {
            htmltext = null;
            showHtmlFile(player, "azit_messenger_q0504_03.htm", false, "%siege_time%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate()));
        } else if (clan == null || player.getObjectId() != clan.getLeaderId()) {
            htmltext = "azit_messenger_q0504_05.htm";
        } else if (player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4) {
            htmltext = "azit_messenger_q0504_04.htm";
        } else if (clanhall.getSiegeEvent().getSiegeClan("attackers", player.getClan()) != null) {
            htmltext = "azit_messenger_q0504_06.htm";
        } else if (clan.getHasHideout() > 0) {
            htmltext = "azit_messenger_q0504_10.htm";
        } else if (cond == 0) {
            htmltext = "azit_messenger_q0504_01.htm";
        } else if (st.getQuestItemsCount(4333) == 1L && st.getQuestItemsCount(4332) < 30L) {
            htmltext = "azit_messenger_q0504_07.htm";
        } else if (st.getQuestItemsCount(5009) >= 1L) {
            htmltext = "azit_messenger_q0504_07a.htm";
        } else if (st.getQuestItemsCount(4333) == 1L && st.getQuestItemsCount(4332) == 30L) {
            st.takeItems(4332, -1L);
            st.takeItems(4333, -1L);
            st.giveItems(5009, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setCond(-1);
            htmltext = "azit_messenger_q0504_08.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(4332) < 30L) {
            st.giveItems(4332, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
