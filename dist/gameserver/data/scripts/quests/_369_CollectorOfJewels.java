package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _369_CollectorOfJewels extends Quest {
    private static final int salamander_lakin = 20609;
    private static final int salamander_rowin = 20612;
    private static final int undine_lakin = 20616;
    private static final int undine_rowin = 20619;
    private static final int roxide = 20747;
    private static final int death_fire = 20749;
    private static final int magister_nell = 30376;
    private static final int flair_shard = 5882;
    private static final int freezing_shard = 5883;

    public _369_CollectorOfJewels() {
        super(false);
        addStartNpc(magister_nell);
        addKillId(20609, 20612, 20616, 20619, 20747, 20749);
        addQuestItem(flair_shard, freezing_shard);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == magister_nell) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("man_collect_element_gem", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "magister_nell_q0369_03.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                st.setCond(3);
                st.set("man_collect_element_gem", String.valueOf(3), true);
                htmltext = "magister_nell_q0369_07.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                st.takeItems(flair_shard, -1L);
                st.takeItems(freezing_shard, -1L);
                st.unset("man_collect_element_gem");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "magister_nell_q0369_08.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("man_collect_element_gem");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != magister_nell) {
                    break;
                }
                if (st.getPlayer().getLevel() < 25) {
                    htmltext = "magister_nell_q0369_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                htmltext = "magister_nell_q0369_02.htm";
                break;
            }
            case 2: {
                if (npcId != magister_nell) {
                    break;
                }
                if ((st.getQuestItemsCount(freezing_shard) < 50L || st.getQuestItemsCount(flair_shard) < 50L) && GetMemoState == 1) {
                    htmltext = "magister_nell_q0369_04.htm";
                    break;
                }
                if (st.getQuestItemsCount(freezing_shard) >= 50L && st.getQuestItemsCount(flair_shard) >= 50L && GetMemoState == 1) {
                    st.giveItems(57, 12500L);
                    st.takeItems(flair_shard, -1L);
                    st.takeItems(freezing_shard, -1L);
                    st.set("man_collect_element_gem", String.valueOf(2), true);
                    htmltext = "magister_nell_q0369_05.htm";
                    break;
                }
                if (GetMemoState == 2) {
                    htmltext = "magister_nell_q0369_09.htm";
                    break;
                }
                if (GetMemoState == 3 && (st.getQuestItemsCount(freezing_shard) < 200L || st.getQuestItemsCount(flair_shard) < 200L)) {
                    htmltext = "magister_nell_q0369_10.htm";
                    break;
                }
                if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200L && st.getQuestItemsCount(flair_shard) >= 200L) {
                    st.giveItems(57, 76000L);
                    st.takeItems(flair_shard, -1L);
                    st.takeItems(freezing_shard, -1L);
                    st.unset("man_collect_element_gem");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    htmltext = "magister_nell_q0369_11.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("man_collect_element_gem");
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 20609:
                if (Rnd.get(100) < 75) {
                    st.giveItems(flair_shard, 1L);
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50L && st.getQuestItemsCount(flair_shard) >= 49L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200L && st.getQuestItemsCount(flair_shard) >= 199L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
            case 20612:
                if (Rnd.get(100) < 91) {
                    st.giveItems(flair_shard, 1L);
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50L && st.getQuestItemsCount(flair_shard) >= 49L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200L && st.getQuestItemsCount(flair_shard) >= 199L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
            case 20616:
                if (Rnd.get(100) < 80) {
                    st.giveItems(flair_shard, 1L);
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50L && st.getQuestItemsCount(flair_shard) >= 49L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200L && st.getQuestItemsCount(flair_shard) >= 199L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
            case 20619:
                if (Rnd.get(100) < 87) {
                    st.giveItems(flair_shard, 1L);
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50L && st.getQuestItemsCount(flair_shard) >= 49L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200L && st.getQuestItemsCount(flair_shard) >= 199L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
            case 20747:
            case 20749:
                if (Rnd.get(100) < 2) {
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49L) {
                        st.giveItems(freezing_shard, 1L);
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199L) {
                        st.giveItems(freezing_shard, 1L);
                    } else {
                        st.giveItems(freezing_shard, 2L);
                    }
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49L && st.getQuestItemsCount(flair_shard) >= 50L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199L && st.getQuestItemsCount(flair_shard) >= 200L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                } else {
                    st.giveItems(freezing_shard, 1L);
                    if (GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49L && st.getQuestItemsCount(flair_shard) >= 50L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199L && st.getQuestItemsCount(flair_shard) >= 200L) {
                        st.setCond(4);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
        }
        return null;
    }
}
