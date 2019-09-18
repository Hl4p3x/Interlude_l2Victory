package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _378_MagnificentFeast extends Quest {
    private static final int RANSPO = 30594;
    private static final int WINE_15 = 5956;
    private static final int WINE_30 = 5957;
    private static final int WINE_60 = 5958;
    private static final int Musical_Score__Theme_of_the_Feast = 4421;
    private static final int Ritrons_Dessert_Recipe = 5959;
    private static final int Jonass_Salad_Recipe = 1455;
    private static final int Jonass_Sauce_Recipe = 1456;
    private static final int Jonass_Steak_Recipe = 1457;

    private final Map<Integer, int[]> rewards;

    public _378_MagnificentFeast() {
        super(false);
        rewards = new HashMap<>();
        addStartNpc(RANSPO);
        rewards.put(9, new int[]{847, 1, 5700});
        rewards.put(10, new int[]{846, 2, 0});
        rewards.put(12, new int[]{909, 1, 25400});
        rewards.put(17, new int[]{846, 2, 1200});
        rewards.put(18, new int[]{879, 1, 6900});
        rewards.put(20, new int[]{890, 2, 8500});
        rewards.put(33, new int[]{879, 1, 8100});
        rewards.put(34, new int[]{910, 1, 0});
        rewards.put(36, new int[]{910, 1, 0});
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int _state = st.getState();
        final int cond = st.getCond();
        final int score = st.getInt("score");
        if ("quest_accept".equalsIgnoreCase(event) && _state == 1) {
            htmltext = "warehouse_chief_ranspo_q0378_03.htm";
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("378_1".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 1 && st.getQuestItemsCount(WINE_15) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_05.htm";
                st.takeItems(WINE_15, 1L);
                st.setCond(2);
                st.set("score", String.valueOf(score + 1));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_08.htm";
            }
        } else if ("378_2".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 1 && st.getQuestItemsCount(WINE_30) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_06.htm";
                st.takeItems(WINE_30, 1L);
                st.setCond(2);
                st.set("score", String.valueOf(score + 2));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_08.htm";
            }
        } else if ("378_3".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 1 && st.getQuestItemsCount(WINE_60) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_07.htm";
                st.takeItems(WINE_60, 1L);
                st.setCond(2);
                st.set("score", String.valueOf(score + 4));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_08.htm";
            }
        } else if ("378_5".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 2 && st.getQuestItemsCount(Musical_Score__Theme_of_the_Feast) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_12.htm";
                st.takeItems(Musical_Score__Theme_of_the_Feast, 1L);
                st.setCond(3);
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_10.htm";
            }
        } else if ("378_6".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 3 && st.getQuestItemsCount(Jonass_Salad_Recipe) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_14.htm";
                st.takeItems(Jonass_Salad_Recipe, 1L);
                st.setCond(4);
                st.set("score", String.valueOf(score + 8));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_17.htm";
            }
        } else if ("378_7".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 3 && st.getQuestItemsCount(Jonass_Sauce_Recipe) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_15.htm";
                st.takeItems(Jonass_Sauce_Recipe, 1L);
                st.setCond(4);
                st.set("score", String.valueOf(score + 16));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_17.htm";
            }
        } else if ("378_8".equalsIgnoreCase(event) && _state == 2) {
            if (cond == 3 && st.getQuestItemsCount(Jonass_Steak_Recipe) > 0L) {
                htmltext = "warehouse_chief_ranspo_q0378_16.htm";
                st.takeItems(Jonass_Steak_Recipe, 1L);
                st.setCond(4);
                st.set("score", String.valueOf(score + 32));
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_17.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != RANSPO) {
            return htmltext;
        }
        final int _state = st.getState();
        final int cond = st.getCond();
        if (_state == 1) {
            if (st.getPlayer().getLevel() < 20) {
                htmltext = "warehouse_chief_ranspo_q0378_01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_02.htm";
                st.setCond(0);
            }
        } else if (cond == 1 && _state == 2) {
            htmltext = "warehouse_chief_ranspo_q0378_04.htm";
        } else if (cond == 2 && _state == 2) {
            htmltext = ((st.getQuestItemsCount(Musical_Score__Theme_of_the_Feast) > 0L) ? "warehouse_chief_ranspo_q0378_11.htm" : "warehouse_chief_ranspo_q0378_10.htm");
        } else if (cond == 3 && _state == 2) {
            htmltext = "warehouse_chief_ranspo_q0378_13.htm";
        } else if (cond == 4 && _state == 2) {
            final int[] reward = rewards.get(st.getInt("score"));
            if (st.getQuestItemsCount(Ritrons_Dessert_Recipe) > 0L && reward != null) {
                htmltext = "warehouse_chief_ranspo_q0378_20.htm";
                st.takeItems(Ritrons_Dessert_Recipe, 1L);
                st.giveItems(reward[0], (long) reward[1]);
                if (reward[2] > 0) {
                    st.giveItems(57, (long) reward[2]);
                }
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "warehouse_chief_ranspo_q0378_19.htm";
            }
        }
        return htmltext;
    }

    
}
