package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _347_GoGetTheCalculator extends Quest {
    public final int blacksmith_bronp = 30526;
    public final int blacksmith_silvery = 30527;
    public final int elder_spiron = 30532;
    public final int elder_balanki = 30533;
    public final int gemstone_beast = 20540;
    public final int q_gemstone = 4286;
    public final int q_calculator = 4285;
    public final int calculator = 4393;

    public _347_GoGetTheCalculator() {
        super(false);
        addStartNpc(30526);
        addTalkId(30527, 30532, 30533);
        addKillId(20540);
        addQuestItem(4286);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("get_calculator");
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("get_calculator", String.valueOf(100), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "blacksmith_bronp_q0347_08.htm";
        } else if ("reply_7".equalsIgnoreCase(event) && GetMemoState == 600 && st.getQuestItemsCount(4285) >= 1L) {
            st.unset("get_calculator");
            st.takeItems(4285, -1L);
            st.giveItems(4393, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            htmltext = "blacksmith_bronp_q0347_10.htm";
        } else if ("reply_8".equalsIgnoreCase(event) && GetMemoState == 600 && st.getQuestItemsCount(4285) >= 1L) {
            st.unset("get_calculator");
            st.takeItems(4285, -1L);
            st.giveItems(57, 1000L, true);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            htmltext = "blacksmith_bronp_q0347_11.htm";
        } else if ("reply_3".equalsIgnoreCase(event)) {
            st.set("get_calculator", String.valueOf(200 + GetMemoState), true);
            if (GetMemoState == 100) {
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
            } else if (GetMemoState == 200) {
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
            }
            htmltext = "elder_spiron_q0347_02.htm";
        } else if ("reply_2".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(57) >= 100L) {
                st.set("get_calculator", String.valueOf(100 + GetMemoState), true);
                st.takeItems(57, 100L);
                if (GetMemoState == 100) {
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                } else if (GetMemoState == 300) {
                    st.setCond(4);
                    st.playSound("ItemSound.quest_middle");
                }
                htmltext = "elder_balanki_q0347_02.htm";
            } else {
                htmltext = "elder_balanki_q0347_03.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("get_calculator");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30526) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 12) {
                    htmltext = "blacksmith_bronp_q0347_01.htm";
                    break;
                }
                htmltext = "blacksmith_bronp_q0347_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 30526:
                        if (st.getQuestItemsCount(4285) >= 1L) {
                            htmltext = "blacksmith_bronp_q0347_09.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(4285) == 0L && GetMemoState == 600) {
                            htmltext = "blacksmith_bronp_q0347_12.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(4285) == 0L && GetMemoState == 100) {
                            htmltext = "blacksmith_bronp_q0347_13.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(4285) == 0L && (GetMemoState == 200 || GetMemoState == 300)) {
                            htmltext = "blacksmith_bronp_q0347_14.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(4285) == 0L && (GetMemoState == 400 || GetMemoState == 500 || GetMemoState == 600)) {
                            htmltext = "blacksmith_bronp_q0347_15.htm";
                            break;
                        }
                        break;
                    case 30527:
                        if (GetMemoState == 100 || GetMemoState == 200 || GetMemoState == 300) {
                            htmltext = "blacksmith_silvery_q0347_01.htm";
                            break;
                        }
                        if (GetMemoState == 400) {
                            st.setCond(5);
                            st.set("get_calculator", String.valueOf(500), true);
                            st.playSound("ItemSound.quest_middle");
                            htmltext = "blacksmith_silvery_q0347_02.htm";
                            break;
                        }
                        if (GetMemoState == 500 && st.getQuestItemsCount(4286) >= 10L) {
                            st.setCond(6);
                            st.set("get_calculator", String.valueOf(600), true);
                            st.giveItems(4285, 1L);
                            st.takeItems(4286, -1L);
                            st.playSound("ItemSound.quest_middle");
                            htmltext = "blacksmith_silvery_q0347_03.htm";
                            break;
                        }
                        if (GetMemoState == 500 && st.getQuestItemsCount(4286) < 10L) {
                            htmltext = "blacksmith_silvery_q0347_04.htm";
                            break;
                        }
                        if (GetMemoState == 600 || GetMemoState == 600) {
                            htmltext = "blacksmith_silvery_q0347_05.htm";
                            break;
                        }
                        break;
                    case 30532:
                        if (GetMemoState == 100 || GetMemoState == 200) {
                            htmltext = "elder_spiron_q0347_01.htm";
                            break;
                        }
                        if (GetMemoState == 300 || GetMemoState == 400 || GetMemoState == 500 || GetMemoState == 600) {
                            htmltext = "elder_spiron_q0347_05.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 30533) {
                            break;
                        }
                        if (GetMemoState == 100 || GetMemoState == 300) {
                            htmltext = "elder_balanki_q0347_01.htm";
                            break;
                        }
                        if (GetMemoState == 200 || GetMemoState == 400 || GetMemoState == 500 || GetMemoState == 600) {
                            htmltext = "elder_balanki_q0347_04.htm";
                            break;
                        }
                        break;
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int GetMemoState = st.getInt("get_calculator");
        if (npcId == 20540 && GetMemoState == 500 && st.getQuestItemsCount(4286) < 10L && Rnd.get(10) <= 4) {
            st.giveItems(4286, 1L);
            if (st.getQuestItemsCount(4286) >= 10L) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
