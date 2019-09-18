package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _303_CollectArrowheads extends Quest {
    int ORCISH_ARROWHEAD;

    public _303_CollectArrowheads() {
        super(false);
        ORCISH_ARROWHEAD = 963;
        addStartNpc(30029);
        addTalkId(30029);
        addKillId(20361);
        addQuestItem(ORCISH_ARROWHEAD);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("minx_q0303_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 10) {
                htmltext = "minx_q0303_03.htm";
            } else {
                htmltext = "minx_q0303_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10L) {
            htmltext = "minx_q0303_05.htm";
        } else {
            st.takeItems(ORCISH_ARROWHEAD, -1L);
            st.giveItems(57, 1000L);
            st.addExpAndSp(2000L, 0L);
            htmltext = "minx_q0303_06.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10L) {
            st.giveItems(ORCISH_ARROWHEAD, 1L);
            if (st.getQuestItemsCount(ORCISH_ARROWHEAD) == 10L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
