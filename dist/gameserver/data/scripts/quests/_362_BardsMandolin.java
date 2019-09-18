package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _362_BardsMandolin extends Quest {
    private static final int SWAN = 30957;
    private static final int NANARIN = 30956;
    private static final int GALION = 30958;
    private static final int WOODROW = 30837;
    private static final int SWANS_FLUTE = 4316;
    private static final int SWANS_LETTER = 4317;
    private static final int Musical_Score__Theme_of_Journey = 4410;

    public _362_BardsMandolin() {
        super(false);
        addStartNpc(SWAN);
        addTalkId(NANARIN);
        addTalkId(GALION);
        addTalkId(WOODROW);
        addQuestItem(SWANS_FLUTE);
        addQuestItem(SWANS_LETTER);
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        if (st.getState() == 1) {
            if (npcId != SWAN) {
                return htmltext;
            }
            st.setCond(0);
        }
        final int cond = st.getCond();
        if (npcId == SWAN) {
            if (cond == 0) {
                htmltext = "30957_1.htm";
            } else if (cond == 3 && st.getQuestItemsCount(SWANS_FLUTE) > 0L && st.getQuestItemsCount(SWANS_LETTER) == 0L) {
                htmltext = "30957_3.htm";
                st.setCond(4);
                st.giveItems(SWANS_LETTER, 1L);
            } else if (cond == 4 && st.getQuestItemsCount(SWANS_FLUTE) > 0L && st.getQuestItemsCount(SWANS_LETTER) > 0L) {
                htmltext = "30957_6.htm";
            } else if (cond == 5) {
                htmltext = "30957_4.htm";
            }
        } else if (npcId == WOODROW && cond == 1) {
            htmltext = "30837_1.htm";
            st.setCond(2);
        } else if (npcId == GALION && cond == 2) {
            htmltext = "30958_1.htm";
            st.setCond(3);
            st.giveItems(SWANS_FLUTE, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if (npcId == NANARIN && cond == 4 && st.getQuestItemsCount(SWANS_FLUTE) > 0L && st.getQuestItemsCount(SWANS_LETTER) > 0L) {
            htmltext = "30956_1.htm";
            st.takeItems(SWANS_FLUTE, 1L);
            st.takeItems(SWANS_LETTER, 1L);
            st.setCond(5);
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("30957_2.htm".equalsIgnoreCase(event) && _state == 1 && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30957_5.htm".equalsIgnoreCase(event) && _state == 2 && cond == 5) {
            st.giveItems(57, 10000L);
            st.giveItems(Musical_Score__Theme_of_Journey, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    
}
