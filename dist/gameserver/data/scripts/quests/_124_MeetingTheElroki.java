package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _124_MeetingTheElroki extends Quest {
    private static final int marquez = 32113;
    private static final int mushika = 32114;
    private static final int asama = 32115;
    private static final int shaman_caracawe = 32117;
    private static final int egg_of_mantarasa = 32118;
    private static final int q_egg_of_mantarasa = 8778;

    public _124_MeetingTheElroki() {
        super(false);
        addStartNpc(32113);
        addTalkId(32114, 32115, 32117, 32118);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("encounter_with_crokian");
        if ("quest_accept".equals(event)) {
            st.setCond(1);
            st.set("encounter_with_crokian", String.valueOf(0), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "marquez_q0124_04.htm";
        }
        if ("reply_2".equals(event) && GetMemoState == 0) {
            st.setCond(2);
            st.set("encounter_with_crokian", String.valueOf(1), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "marquez_q0124_06.htm";
        }
        if ("reply_2a".equals(event) && GetMemoState == 1) {
            st.setCond(3);
            st.set("encounter_with_crokian", String.valueOf(2), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "mushika_q0124_03.htm";
        }
        if ("reply_6".equals(event) && GetMemoState == 2) {
            st.setCond(4);
            st.set("encounter_with_crokian", String.valueOf(3), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "asama_q0124_06.htm";
        }
        if ("reply_9".equals(event) && GetMemoState == 3) {
            st.setCond(5);
            st.set("encounter_with_crokian", String.valueOf(4), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "shaman_caracawe_q0124_05.htm";
        }
        if ("reply_10".equals(event) && GetMemoState == 4 && st.getQuestItemsCount(8778) < 1L) {
            st.setCond(6);
            st.giveItems(8778, 1L);
            st.playSound("ItemSound.quest_middle");
            htmltext = "egg_of_mantarasa_q0124_02.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("encounter_with_crokian");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32113) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 75) {
                    htmltext = "marquez_q0124_01.htm";
                    break;
                }
                htmltext = "marquez_q0124_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 32113:
                        if (GetMemoState == 0) {
                            htmltext = "marquez_q0124_05.htm";
                            break;
                        }
                        if (GetMemoState == 1) {
                            htmltext = "marquez_q0124_07.htm";
                            break;
                        }
                        if (GetMemoState >= 2 && GetMemoState <= 5) {
                            htmltext = "marquez_q0124_08.htm";
                            break;
                        }
                        break;
                    case 32114:
                        if (GetMemoState == 1) {
                            htmltext = "mushika_q0124_01.htm";
                            break;
                        }
                        if (GetMemoState < 1) {
                            htmltext = "mushika_q0124_02.htm";
                            break;
                        }
                        if (GetMemoState >= 2) {
                            htmltext = "mushika_q0124_04.htm";
                            break;
                        }
                        break;
                    case 32115:
                        if (GetMemoState == 2) {
                            htmltext = "asama_q0124_01.htm";
                            break;
                        }
                        if (GetMemoState < 2) {
                            htmltext = "asama_q0124_02.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "asama_q0124_07.htm";
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) >= 1L) {
                            st.giveItems(57, 71318L);
                            st.takeItems(8778, -1L);
                            st.playSound("ItemSound.quest_finish");
                            st.unset("encounter_with_crokian");
                            htmltext = "asama_q0124_08.htm";
                            st.exitCurrentQuest(false);
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) < 1L) {
                            htmltext = "asama_q0124_09.htm";
                            break;
                        }
                        break;
                    case 32117:
                        if (GetMemoState == 3) {
                            htmltext = "shaman_caracawe_q0124_01.htm";
                            break;
                        }
                        if (GetMemoState < 3) {
                            htmltext = "shaman_caracawe_q0124_02.htm";
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) < 1L) {
                            htmltext = "shaman_caracawe_q0124_06.htm";
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) >= 1L) {
                            htmltext = "shaman_caracawe_q0124_07.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 32118) {
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) < 1L) {
                            htmltext = "egg_of_mantarasa_q0124_01.htm";
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(8778) >= 1L) {
                            htmltext = "egg_of_mantarasa_q0124_03.htm";
                            break;
                        }
                        if (GetMemoState < 4) {
                            htmltext = "egg_of_mantarasa_q0124_04.htm";
                            break;
                        }
                        break;
                }
            }
        }
        return htmltext;
    }
}
