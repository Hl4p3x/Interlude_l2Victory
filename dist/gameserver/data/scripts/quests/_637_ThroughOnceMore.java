package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _637_ThroughOnceMore extends Quest {
    private static final int falsepriest_flauron = 32010;
    private static final int bone_animator = 21565;
    private static final int skull_animator = 21566;
    private static final int bone_slayer = 21567;
    private static final int q_heart_of_reanimated = 8066;
    private static final int q_mark_of_sacrifice = 8064;
    private static final int q_faded_mark_of_sac = 8065;
    private static final int q_mark_of_heresy = 8067;
    private static final int q_key_of_anteroom = 8273;

    public _637_ThroughOnceMore() {
        super(false);
        addStartNpc(32010);
        addKillId(21565, 21566, 21567);
        addQuestItem(8066);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 32010) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("beyond_the_door_again", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "falsepriest_flauron_q0637_11.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "falsepriest_flauron_q0637_06.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "falsepriest_flauron_q0637_07.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                htmltext = "falsepriest_flauron_q0637_08.htm";
            } else if ("reply_4".equalsIgnoreCase(event)) {
                htmltext = "falsepriest_flauron_q0637_09.htm";
            } else if ("reply_5".equalsIgnoreCase(event)) {
                htmltext = "falsepriest_flauron_q0637_10.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("beyond_the_door_again");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32010) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 73 && st.getQuestItemsCount(8065) >= 1L && st.getQuestItemsCount(8067) == 0L) {
                    htmltext = "falsepriest_flauron_q0637_01.htm";
                    break;
                }
                htmltext = "falsepriest_flauron_q0637_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 32010) {
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(8064) >= 1L && st.getQuestItemsCount(8065) == 0L && st.getQuestItemsCount(8067) == 0L) {
                    htmltext = "falsepriest_flauron_q0637_03.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(8064) == 0L && st.getQuestItemsCount(8065) == 0L && st.getQuestItemsCount(8067) == 0L) {
                    htmltext = "falsepriest_flauron_q0637_04.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(8067) >= 1L) {
                    htmltext = "falsepriest_flauron_q0637_05.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(8066) < 10L) {
                    htmltext = "falsepriest_flauron_q0637_12.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(8066) >= 10L) {
                    st.giveItems(8067, 1L);
                    st.giveItems(8273, 10L);
                    st.takeItems(8064, -1L);
                    st.takeItems(8065, -1L);
                    st.takeItems(8066, -1L);
                    st.unset("beyond_the_door_again");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    htmltext = "falsepriest_flauron_q0637_13.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("beyond_the_door_again");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 1 && st.getQuestItemsCount(8066) < 10L) {
            switch (npcId) {
                case 21565:
                    if (Rnd.get(100) < 84) {
                        st.giveItems(8066, 1L);
                        if (st.getQuestItemsCount(8066) >= 9L) {
                            st.setCond(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                case 21566:
                    if (Rnd.get(100) < 92) {
                        st.giveItems(8066, 1L);
                        if (st.getQuestItemsCount(8066) >= 9L) {
                            st.setCond(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                case 21567:
                    if (Rnd.get(100) < 10) {
                        st.giveItems(8066, 1L);
                        if (st.getQuestItemsCount(8066) >= 9L) {
                            st.setCond(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else {
                        st.giveItems(8066, 1L);
                        if (st.getQuestItemsCount(8066) >= 9L) {
                            st.setCond(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
            }
        }
        return null;
    }
}
