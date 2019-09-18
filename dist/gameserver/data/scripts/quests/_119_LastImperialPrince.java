package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _119_LastImperialPrince extends Quest {
    private static final int printessa_spirit = 31453;
    private static final int frintessa_nurse = 32009;
    private static final int q_antique_brooch = 7262;

    public _119_LastImperialPrince() {
        super(false);
        addStartNpc(31453);
        addTalkId(32009);
        addQuestItem(7262);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("the_last_imperial_prince");
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("the_last_imperial_prince", String.valueOf(1), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "printessa_spirit_q0119_06.htm";
        } else if ("reply_4".equalsIgnoreCase(event) && GetMemoState == 2) {
            st.giveItems(57, 68787L, true);
            st.playSound("ItemSound.quest_finish");
            st.unset("the_last_imperial_prince");
            htmltext = "printessa_spirit_q0119_10.htm";
            st.exitCurrentQuest(false);
        } else if ("reply_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7262) >= 1L) {
                htmltext = "frintessa_nurse_q0119_02.htm";
            } else {
                htmltext = "frintessa_nurse_q0119_02a.htm";
            }
        } else if ("reply_2".equalsIgnoreCase(event) && GetMemoState == 1 && st.getQuestItemsCount(7262) >= 1L) {
            st.setCond(2);
            st.set("the_last_imperial_prince", String.valueOf(2), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "frintessa_nurse_q0119_03.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int GetMemoState = st.getInt("the_last_imperial_prince");
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31453) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 74 && st.getQuestItemsCount(7262) >= 1L) {
                    htmltext = "printessa_spirit_q0119_01.htm";
                    break;
                }
                htmltext = "printessa_spirit_q0119_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId == 31453) {
                    if (GetMemoState == 1 && st.getQuestItemsCount(7262) >= 1L) {
                        htmltext = "printessa_spirit_q0119_07.htm";
                        break;
                    }
                    if (GetMemoState == 1 && st.getQuestItemsCount(7262) == 0L) {
                        htmltext = "printessa_spirit_q0119_07a.htm";
                        st.exitCurrentQuest(true);
                        break;
                    }
                    if (GetMemoState == 2) {
                        htmltext = "printessa_spirit_q0119_08.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId != 32009) {
                        break;
                    }
                    if (GetMemoState == 1) {
                        htmltext = "frintessa_nurse_q0119_01.htm";
                        break;
                    }
                    if (GetMemoState == 2) {
                        htmltext = "frintessa_nurse_q0119_04.htm";
                        break;
                    }
                    break;
                }
            }
            case 3: {
                if (npcId == 31453) {
                    htmltext = "printessa_spirit_q0119_03.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }
}
