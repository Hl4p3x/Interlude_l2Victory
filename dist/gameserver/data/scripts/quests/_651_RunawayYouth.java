package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _651_RunawayYouth extends Quest {
    private static final int IVAN = 32014;
    private static final int BATIDAE = 31989;
    private static final int SOE = 736;

    protected NpcInstance _npc;

    public _651_RunawayYouth() {
        super(false);
        addStartNpc(IVAN);
        addTalkId(BATIDAE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("runaway_boy_ivan_q0651_03.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(SOE) > 0L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.takeItems(SOE, 1L);
                htmltext = "runaway_boy_ivan_q0651_04.htm";
                st.startQuestTimer("ivan_timer", 20000L);
            }
        } else if ("runaway_boy_ivan_q0651_05.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_giveup");
        } else if ("ivan_timer".equalsIgnoreCase(event)) {
            _npc.deleteMe();
            htmltext = null;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == IVAN && cond == 0) {
            if (st.getPlayer().getLevel() >= 26) {
                htmltext = "runaway_boy_ivan_q0651_01.htm";
            } else {
                htmltext = "runaway_boy_ivan_q0651_01a.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == BATIDAE && cond == 1) {
            htmltext = "fisher_batidae_q0651_01.htm";
            st.giveItems(57, Math.round(2883.0 * st.getRateQuestsReward()));
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }
}
