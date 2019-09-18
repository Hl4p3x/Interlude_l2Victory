package quests;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.utils.TimeUtils;

public class _655_AGrandPlanForTamingWildBeasts extends Quest {
    private static final int MESSENGER = 35627;
    private static final int STONE = 8084;
    private static final int TRAINER_LICENSE = 8293;

    public _655_AGrandPlanForTamingWildBeasts() {
        super(0);
        addStartNpc(35627);
        addTalkId(35627);
        addQuestItem(8084, 8293);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("farm_messenger_q0655_06.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmlText = "noquest";
        final int cond = st.getCond();
        final Player player = st.getPlayer();
        final Clan clan = player.getClan();
        final ClanHall clanhall = ResidenceHolder.getInstance().getResidence(63);
        if (clanhall.getSiegeEvent().isRegistrationOver()) {
            htmlText = null;
            showHtmlFile(player, "farm_messenger_q0655_02.htm", false, "%siege_time%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate()));
        } else if (clan == null || player.getObjectId() != clan.getLeaderId()) {
            htmlText = "farm_messenger_q0655_03.htm";
        } else if (player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4) {
            htmlText = "farm_messenger_q0655_05.htm";
        } else if (clanhall.getSiegeEvent().getSiegeClan("attackers", player.getClan()) != null) {
            htmlText = "farm_messenger_q0655_07.htm";
        } else if (clan.getHasHideout() > 0) {
            htmlText = "farm_messenger_q0655_04.htm";
        } else if (cond == 0) {
            htmlText = "farm_messenger_q0655_01.htm";
        } else if (cond == 1 && st.getQuestItemsCount(8084) < 10L) {
            htmlText = "farm_messenger_q0655_08.htm";
        } else if (cond == 1 && st.getQuestItemsCount(8084) == 10L) {
            st.setCond(-1);
            st.takeItems(8084, -1L);
            st.giveItems(8293, 1L);
            htmlText = "farm_messenger_q0655_10.htm";
        } else if (st.getQuestItemsCount(8293) == 1L) {
            htmlText = "farm_messenger_q0655_09.htm";
        }
        return htmlText;
    }

    
}
