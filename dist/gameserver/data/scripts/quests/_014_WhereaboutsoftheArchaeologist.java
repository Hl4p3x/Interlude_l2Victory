package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _014_WhereaboutsoftheArchaeologist extends Quest {
    private static final int LETTER_TO_ARCHAEOLOGIST = 7253;

    public _014_WhereaboutsoftheArchaeologist() {
        super(false);
        addStartNpc(31263);
        addTalkId(31538);
        addQuestItem(7253);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("trader_liesel_q0014_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.giveItems(7253, 1L);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("explorer_ghost_a_q0014_0201.htm".equalsIgnoreCase(event)) {
            st.takeItems(7253, -1L);
            st.giveItems(57, 113228L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
            return "explorer_ghost_a_q0014_0201.htm";
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31263) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    htmltext = "trader_liesel_q0014_0101.htm";
                } else {
                    htmltext = "trader_liesel_q0014_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "trader_liesel_q0014_0104.htm";
            }
        } else if (npcId == 31538 && cond == 1 && st.getQuestItemsCount(7253) == 1L) {
            htmltext = "explorer_ghost_a_q0014_0101.htm";
        }
        return htmltext;
    }
}
