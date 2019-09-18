package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _017_LightAndDarkness extends Quest {
    public _017_LightAndDarkness() {
        super(false);
        addStartNpc(31517);
        addTalkId(31508);
        addTalkId(31509);
        addTalkId(31510);
        addTalkId(31511);
        addQuestItem(7168);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "dark_presbyter_q0017_04.htm":
                st.setState(2);
                st.setCond(1);
                st.giveItems(7168, 4L);
                st.playSound("ItemSound.quest_accept");
                break;
            case "blessed_altar1_q0017_02.htm":
                st.takeItems(7168, 1L);
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
                break;
            case "blessed_altar2_q0017_02.htm":
                st.takeItems(7168, 1L);
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
                break;
            case "blessed_altar3_q0017_02.htm":
                st.takeItems(7168, 1L);
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
                break;
            case "blessed_altar4_q0017_02.htm":
                st.takeItems(7168, 1L);
                st.setCond(5);
                st.playSound("ItemSound.quest_middle");
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 31517:
                if (cond == 0) {
                    if (st.getPlayer().getLevel() >= 61) {
                        htmltext = "dark_presbyter_q0017_01.htm";
                    } else {
                        htmltext = "dark_presbyter_q0017_03.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond > 0 && cond < 5 && st.getQuestItemsCount(7168) > 0L) {
                    htmltext = "dark_presbyter_q0017_05.htm";
                } else if (cond > 0 && cond < 5 && st.getQuestItemsCount(7168) == 0L) {
                    htmltext = "dark_presbyter_q0017_06.htm";
                    st.setCond(0);
                    st.exitCurrentQuest(false);
                } else if (cond == 5 && st.getQuestItemsCount(7168) == 0L) {
                    htmltext = "dark_presbyter_q0017_07.htm";
                    st.addExpAndSp(105527L, 0L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
                break;
            case 31508:
                if (cond == 1) {
                    if (st.getQuestItemsCount(7168) != 0L) {
                        htmltext = "blessed_altar1_q0017_01.htm";
                    } else {
                        htmltext = "blessed_altar1_q0017_03.htm";
                    }
                } else if (cond == 2) {
                    htmltext = "blessed_altar1_q0017_05.htm";
                }
                break;
            case 31509:
                if (cond == 2) {
                    if (st.getQuestItemsCount(7168) != 0L) {
                        htmltext = "blessed_altar2_q0017_01.htm";
                    } else {
                        htmltext = "blessed_altar2_q0017_03.htm";
                    }
                } else if (cond == 3) {
                    htmltext = "blessed_altar2_q0017_05.htm";
                }
                break;
            case 31510:
                if (cond == 3) {
                    if (st.getQuestItemsCount(7168) != 0L) {
                        htmltext = "blessed_altar3_q0017_01.htm";
                    } else {
                        htmltext = "blessed_altar3_q0017_03.htm";
                    }
                } else if (cond == 4) {
                    htmltext = "blessed_altar3_q0017_05.htm";
                }
                break;
            case 31511:
                if (cond == 4) {
                    if (st.getQuestItemsCount(7168) != 0L) {
                        htmltext = "blessed_altar4_q0017_01.htm";
                    } else {
                        htmltext = "blessed_altar4_q0017_03.htm";
                    }
                } else if (cond == 5) {
                    htmltext = "blessed_altar4_q0017_05.htm";
                }
                break;
        }
        return htmltext;
    }
}
