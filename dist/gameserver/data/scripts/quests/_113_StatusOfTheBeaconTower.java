package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _113_StatusOfTheBeaconTower extends Quest {
    private static final int MOIRA = 31979;
    private static final int TORRANT = 32016;
    private static final int BOX = 8086;

    public _113_StatusOfTheBeaconTower() {
        super(false);
        addStartNpc(31979);
        addTalkId(32016);
        addQuestItem(8086);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("seer_moirase_q0113_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.giveItems(8086, 1L);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("torant_q0113_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(57, 12020L);
            st.takeItems(8086, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 3) {
            htmltext = "completed";
        } else if (npcId == 31979) {
            if (id == 1) {
                if (st.getPlayer().getLevel() >= 40) {
                    htmltext = "seer_moirase_q0113_0101.htm";
                } else {
                    htmltext = "seer_moirase_q0113_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "seer_moirase_q0113_0105.htm";
            }
        } else if (npcId == 32016 && st.getQuestItemsCount(8086) == 1L) {
            htmltext = "torant_q0113_0101.htm";
        }
        return htmltext;
    }
}
