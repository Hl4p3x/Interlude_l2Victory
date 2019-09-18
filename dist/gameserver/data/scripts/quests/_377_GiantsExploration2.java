package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _377_GiantsExploration2 extends Quest {
    private static final int DROP_RATE = 20;
    private static final int ANC_BOOK = 5955;
    private static final int DICT2 = 5892;
    private static final int[][] EXCHANGE = {{5945, 5946, 5947, 5948, 5949}, {5950, 5951, 5952, 5953, 5954}};
    private static final int HR_SOBLING = 31147;
    private static final int[] MOBS = {20654, 20656, 20657, 20658};

    public _377_GiantsExploration2() {
        super(true);
        addStartNpc(31147);
        addKillId(_377_GiantsExploration2.MOBS);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("yes".equalsIgnoreCase(event)) {
            htmltext = "Starting.htm";
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("0".equalsIgnoreCase(event)) {
            htmltext = "ext_msg.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("show".equalsIgnoreCase(event)) {
            htmltext = "no_items.htm";
            for (final int[] i : _377_GiantsExploration2.EXCHANGE) {
                long count = Long.MAX_VALUE;
                for (final int j : i) {
                    count = Math.min(count, st.getQuestItemsCount(j));
                }
                if (count > 0L) {
                    htmltext = "tnx4items.htm";
                    for (final int j : i) {
                        st.takeItems(j, count);
                    }
                    for (int n = 0; n < count; ++n) {
                        final int luck = Rnd.get(100);
                        int item;
                        if (luck > 75) {
                            item = 5420;
                        } else if (luck > 50) {
                            item = 5422;
                        } else if (luck > 25) {
                            item = 5336;
                        } else {
                            item = 5338;
                        }
                        st.giveItems(item, 1L);
                    }
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        if (st.getQuestItemsCount(5892) == 0L) {
            st.exitCurrentQuest(true);
        } else if (id == 1) {
            htmltext = "start.htm";
            if (st.getPlayer().getLevel() < 56) {
                st.exitCurrentQuest(true);
                htmltext = "error_1.htm";
            }
        } else if (id == 2) {
            if (st.getQuestItemsCount(5955) != 0L) {
                htmltext = "checkout.htm";
            } else {
                htmltext = "checkout2.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            st.rollAndGive(5955, 1, 20.0);
        }
        return null;
    }
}
