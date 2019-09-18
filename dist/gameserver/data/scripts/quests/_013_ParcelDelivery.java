package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _013_ParcelDelivery extends Quest {
    private static final int PACKAGE = 7263;

    public _013_ParcelDelivery() {
        super(false);
        addStartNpc(31274);
        addTalkId(31274);
        addTalkId(31539);
        addQuestItem(7263);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("mineral_trader_fundin_q0013_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.giveItems(7263, 1L);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("warsmith_vulcan_q0013_0201.htm".equalsIgnoreCase(event)) {
            st.takeItems(7263, -1L);
            st.giveItems(57, 82656L, true);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31274) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    htmltext = "mineral_trader_fundin_q0013_0101.htm";
                } else {
                    htmltext = "mineral_trader_fundin_q0013_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "mineral_trader_fundin_q0013_0105.htm";
            }
        } else if (npcId == 31539 && cond == 1 && st.getQuestItemsCount(7263) == 1L) {
            htmltext = "warsmith_vulcan_q0013_0101.htm";
        }
        return htmltext;
    }
}
