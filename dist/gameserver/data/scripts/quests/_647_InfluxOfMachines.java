package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _647_InfluxOfMachines extends Quest {
    private static final int DROP_CHANCE = 60;
    private static final int DESTROYED_GOLEM_SHARD = 8100;
    private static final int[] RECIPES = {4963, 4964, 4965, 4966, 4967, 4968, 4969, 4970, 4971, 4972, 5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 8298, 8306, 8310, 8312, 8322, 8324};

    public _647_InfluxOfMachines() {
        super(true);
        addStartNpc(32069);
        addTalkId(32069);
        for (int i = 22052; i < 22079; ++i) {
            addKillId(i);
        }
        addQuestItem(8100);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "collecter_gutenhagen_q0647_0103.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("647_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(8100) >= 500L) {
                st.takeItems(8100, -1L);
                st.giveItems(_647_InfluxOfMachines.RECIPES[Rnd.get(_647_InfluxOfMachines.RECIPES.length)], 1L);
                htmltext = "collecter_gutenhagen_q0647_0201.htm";
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "collecter_gutenhagen_q0647_0106.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        final long count = st.getQuestItemsCount(8100);
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 46) {
                htmltext = "collecter_gutenhagen_q0647_0101.htm";
            } else {
                htmltext = "collecter_gutenhagen_q0647_0102.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 && count < 500L) {
            htmltext = "collecter_gutenhagen_q0647_0106.htm";
        } else if (cond == 2 && count >= 500L) {
            htmltext = "collecter_gutenhagen_q0647_0105.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.rollAndGive(8100, 1, 1, 500, 60.0 * npc.getTemplate().rateHp)) {
            st.setCond(2);
        }
        return null;
    }
}
