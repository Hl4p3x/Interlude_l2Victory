package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _617_GatherTheFlames extends Quest {
    public static final int[] Recipes = {6881, 6883, 6885, 6887, 7580, 6891, 6893, 6895, 6897, 6899};
    private static final int VULCAN = 31539;
    private static final int HILDA = 31271;
    private static final int TORCH = 7264;
    private static final int[][] DROPLIST = {{21376, 48}, {21377, 48}, {21378, 48}, {21652, 48}, {21380, 48}, {21381, 51}, {21653, 51}, {21383, 51}, {21394, 51}, {21385, 51}, {21386, 51}, {21388, 53}, {21655, 53}, {21387, 53}, {21390, 56}, {21656, 56}, {21395, 56}, {21389, 56}, {21391, 56}, {21392, 56}, {21393, 58}, {21657, 58}, {21382, 60}, {21379, 60}, {21654, 64}, {21384, 64}};

    public _617_GatherTheFlames() {
        super(true);
        addStartNpc(31539);
        addStartNpc(31271);
        for (final int[] element : _617_GatherTheFlames.DROPLIST) {
            addKillId(element[0]);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("warsmith_vulcan_q0617_03.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() < 74) {
                return "warsmith_vulcan_q0617_02.htm";
            }
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
        } else if ("blacksmith_hilda_q0617_03.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() < 74) {
                return "blacksmith_hilda_q0617_02.htm";
            }
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
        } else if ("warsmith_vulcan_q0617_08.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.takeItems(7264, -1L);
            st.exitCurrentQuest(true);
        } else if ("warsmith_vulcan_q0617_07.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7264) < 1000L) {
                return "warsmith_vulcan_q0617_05.htm";
            }
            st.takeItems(7264, 1000L);
            st.giveItems(_617_GatherTheFlames.Recipes[Rnd.get(_617_GatherTheFlames.Recipes.length)], 1L);
            st.playSound("ItemSound.quest_middle");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31539) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 74) {
                    htmltext = "warsmith_vulcan_q0617_02.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "warsmith_vulcan_q0617_01.htm";
                }
            } else {
                htmltext = ((st.getQuestItemsCount(7264) < 1000L) ? "warsmith_vulcan_q0617_05.htm" : "warsmith_vulcan_q0617_04.htm");
            }
        } else if (npcId == 31271) {
            if (cond < 1) {
                htmltext = ((st.getPlayer().getLevel() < 74) ? "blacksmith_hilda_q0617_02.htm" : "blacksmith_hilda_q0617_01.htm");
            } else {
                htmltext = "blacksmith_hilda_q0617_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        for (final int[] element : _617_GatherTheFlames.DROPLIST) {
            if (npc.getNpcId() == element[0]) {
                st.rollAndGive(7264, 1, (double) element[1]);
                return null;
            }
        }
        return null;
    }
}
