package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _016_TheComingDarkness extends Quest {
    public final int HIERARCH = 31517;
    public final int[][] ALTAR_LIST;
    public final int CRYSTAL_OF_SEAL = 7167;

    public _016_TheComingDarkness() {
        super(false);
        ALTAR_LIST = new int[][]{{31512, 1}, {31513, 2}, {31514, 3}, {31515, 4}, {31516, 5}};
        addStartNpc(31517);
        for (final int[] element : ALTAR_LIST) {
            addTalkId(element[0]);
        }
        addQuestItem(7167);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("31517-02.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.giveItems(7167, 5L);
            st.playSound("ItemSound.quest_accept");
        }
        for (final int[] element : ALTAR_LIST) {
            if (event.equalsIgnoreCase(element[0] + "-02.htm")) {
                st.takeItems(7167, 1L);
                st.setCond(element[1] + 1);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31517) {
            if (cond < 1) {
                if (st.getPlayer().getLevel() < 61) {
                    htmltext = "31517-00.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "31517-01.htm";
                }
            } else if (cond > 0 && cond < 6 && st.getQuestItemsCount(7167) > 0L) {
                htmltext = "31517-02r.htm";
            } else if (cond > 0 && cond < 6 && st.getQuestItemsCount(7167) < 1L) {
                htmltext = "31517-proeb.htm";
                st.exitCurrentQuest(false);
            } else if (cond > 5 && st.getQuestItemsCount(7167) < 1L) {
                htmltext = "31517-03.htm";
                st.addExpAndSp(221958L, 0L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            }
        }
        for (final int[] element : ALTAR_LIST) {
            if (npcId == element[0]) {
                if (cond == element[1]) {
                    if (st.getQuestItemsCount(7167) > 0L) {
                        htmltext = element[0] + "-01.htm";
                    } else {
                        htmltext = element[0] + "-03.htm";
                    }
                } else if (cond == element[1] + 1) {
                    htmltext = element[0] + "-04.htm";
                }
            }
        }
        return htmltext;
    }
}
