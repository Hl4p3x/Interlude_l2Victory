package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _376_GiantsExploration1 extends Quest {
    private static final int DROP_RATE = 20;
    private static final int DROP_RATE_BOOK = 15;
    private static final int ANCIENT_PARCHMENT = 5944;
    private static final int DICT1 = 5891;
    private static final int DICT2 = 5892;
    private static final int MST_BK = 5890;
    private static final int[][] EXCHANGE = {{5937, 5938, 5939, 5940, 5941}, {5346, 5354}, {5932, 5933, 5934, 5935, 5936}, {5332, 5334}, {5922, 5923, 5924, 5925, 5926}, {5416, 5418}, {5927, 5928, 5929, 5930, 5931}, {5424, 5340}};
    private static final int HR_SOBLING = 31147;
    private static final int WF_CLIFF = 30182;
    private static final int[] MOBS = {20647, 20648, 20649, 20650};

    public _376_GiantsExploration1() {
        super(true);
        addStartNpc(31147);
        addTalkId(30182);
        addKillId(_376_GiantsExploration1.MOBS);
        addQuestItem(5891, 5890);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("yes".equalsIgnoreCase(event)) {
            htmltext = "Starting.htm";
            st.setState(2);
            st.setCond(1);
            st.giveItems(5891, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("no".equalsIgnoreCase(event)) {
            htmltext = "ext_msg.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("show".equalsIgnoreCase(event)) {
            htmltext = "no_items.htm";
            for (int i = 0; i < _376_GiantsExploration1.EXCHANGE.length; i += 2) {
                long count = Long.MAX_VALUE;
                for (final int j : _376_GiantsExploration1.EXCHANGE[i]) {
                    count = Math.min(count, st.getQuestItemsCount(j));
                }
                if (count >= 1L) {
                    htmltext = "tnx4items.htm";
                    for (final int j : _376_GiantsExploration1.EXCHANGE[i]) {
                        st.takeItems(j, count);
                    }
                    for (int l = 0; l < count; ++l) {
                        final int item = _376_GiantsExploration1.EXCHANGE[i + 1][Rnd.get(_376_GiantsExploration1.EXCHANGE[i + 1].length)];
                        st.giveItems(item, 1L);
                    }
                }
            }
        } else if ("myst".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(5890) > 0L) {
                if (cond == 1) {
                    st.setState(2);
                    st.setCond(2);
                    htmltext = "go_part2.htm";
                } else if (cond == 2) {
                    htmltext = "gogogo_2.htm";
                }
            } else {
                htmltext = "no_part2.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (npcId == 31147) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 51) {
                    st.exitCurrentQuest(true);
                    htmltext = "error_1.htm";
                } else {
                    htmltext = "start.htm";
                }
            } else if (id == 2) {
                if (st.getQuestItemsCount(5944) != 0L) {
                    htmltext = "checkout2.htm";
                } else {
                    htmltext = "checkout.htm";
                }
            }
        } else if (npcId == 30182 && (cond == 2 & st.getQuestItemsCount(5890) > 0L)) {
            htmltext = "ok_part2.htm";
            st.takeItems(5890, -1L);
            st.giveItems(5892, 1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond > 0) {
            st.rollAndGive(5944, 1, 1, 20.0);
            if (cond == 1) {
                st.rollAndGive(5890, 1, 1, 1, 15.0);
            }
        }
        return null;
    }
}
