package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _019_GoToThePastureland extends Quest {
    int VLADIMIR;
    int TUNATUN;
    int BEAST_MEAT;

    public _019_GoToThePastureland() {
        super(false);
        VLADIMIR = 31302;
        TUNATUN = 31537;
        BEAST_MEAT = 7547;
        addStartNpc(VLADIMIR);
        addTalkId(VLADIMIR);
        addTalkId(TUNATUN);
        addQuestItem(BEAST_MEAT);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("trader_vladimir_q0019_0104.htm".equals(event)) {
            st.giveItems(BEAST_MEAT, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        if ("beast_herder_tunatun_q0019_0201.htm".equals(event)) {
            st.takeItems(BEAST_MEAT, -1L);
            st.giveItems(57, 30000L);
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
        if (npcId == VLADIMIR) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 63) {
                    htmltext = "trader_vladimir_q0019_0101.htm";
                } else {
                    htmltext = "trader_vladimir_q0019_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "trader_vladimir_q0019_0105.htm";
            }
        } else if (npcId == TUNATUN) {
            if (st.getQuestItemsCount(BEAST_MEAT) >= 1L) {
                htmltext = "beast_herder_tunatun_q0019_0101.htm";
            } else {
                htmltext = "beast_herder_tunatun_q0019_0202.htm";
                st.exitCurrentQuest(true);
            }
        }
        return htmltext;
    }
}
