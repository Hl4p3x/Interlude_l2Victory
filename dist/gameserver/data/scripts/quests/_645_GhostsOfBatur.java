package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _645_GhostsOfBatur extends Quest {
    private static final int Karuda = 32017;
    private static final int CursedGraveGoods = 8089;
    private static final int[][] REWARDS = {{1878, 18}, {1879, 7}, {1880, 4}, {1881, 6}, {1882, 10}, {1883, 2}};
    private static final int[] MOBS = {22007, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016};

    public _645_GhostsOfBatur() {
        super(false);
        addStartNpc(32017);
        addTalkId(32017);
        for (final int i : _645_GhostsOfBatur.MOBS) {
            addKillId(i);
        }
        addQuestItem(8089);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("32017-03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if (st.getCond() == 2) {
            if (st.getQuestItemsCount(8089) >= 180L) {
                for (int i = 0; i < _645_GhostsOfBatur.REWARDS.length; ++i) {
                    if (event.equalsIgnoreCase(String.valueOf(_645_GhostsOfBatur.REWARDS[i][0]))) {
                        st.takeItems(8089, -1L);
                        st.giveItems(_645_GhostsOfBatur.REWARDS[i][0], (long) _645_GhostsOfBatur.REWARDS[i][1], true);
                        htmltext = "32017-07.htm";
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(true);
                    }
                }
            } else {
                htmltext = "32017-04.htm";
                st.setCond(1);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (cond) {
            case 0:
                if (st.getPlayer().getLevel() < 21 || st.getPlayer().getLevel() > 32) {
                    htmltext = "32017-02.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "32017-01.htm";
                }
                break;
            case 1:
                htmltext = "32017-04.htm";
                break;
            case 2:
                if (st.getQuestItemsCount(8089) >= 180L) {
                    htmltext = "32017-05.htm";
                } else {
                    htmltext = "32017-01.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(8089) < 180L && st.rollAndGive(8089, 1, 2, 180, 70.0)) {
            st.setCond(2);
            st.setState(2);
        }
        return null;
    }
}
