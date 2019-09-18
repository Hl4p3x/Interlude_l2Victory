package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _107_MercilessPunishment extends Quest {
    int HATOSS_ORDER1;
    int HATOSS_ORDER2;
    int HATOSS_ORDER3;
    int LETTER_TO_HUMAN;
    int LETTER_TO_DARKELF;
    int LETTER_TO_ELF;
    int BUTCHER;

    public _107_MercilessPunishment() {
        super(false);
        HATOSS_ORDER1 = 1553;
        HATOSS_ORDER2 = 1554;
        HATOSS_ORDER3 = 1555;
        LETTER_TO_HUMAN = 1557;
        LETTER_TO_DARKELF = 1556;
        LETTER_TO_ELF = 1558;
        BUTCHER = 1510;
        addStartNpc(30568);
        addTalkId(30580);
        addKillId(27041);
        addQuestItem(LETTER_TO_DARKELF, LETTER_TO_HUMAN, LETTER_TO_ELF, HATOSS_ORDER1, HATOSS_ORDER2, HATOSS_ORDER3);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("urutu_chief_hatos_q0107_03.htm".equalsIgnoreCase(event)) {
            st.giveItems(HATOSS_ORDER1, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("urutu_chief_hatos_q0107_06.htm".equalsIgnoreCase(event)) {
            st.takeItems(HATOSS_ORDER2, 1L);
            st.takeItems(LETTER_TO_DARKELF, 1L);
            st.takeItems(LETTER_TO_HUMAN, 1L);
            st.takeItems(LETTER_TO_ELF, 1L);
            st.takeItems(HATOSS_ORDER1, 1L);
            st.takeItems(HATOSS_ORDER2, 1L);
            st.takeItems(HATOSS_ORDER3, 1L);
            st.giveItems(57, 200L);
            st.unset("cond");
            st.playSound("ItemSound.quest_giveup");
        } else if ("urutu_chief_hatos_q0107_07.htm".equalsIgnoreCase(event)) {
            st.takeItems(HATOSS_ORDER1, 1L);
            if (st.getQuestItemsCount(HATOSS_ORDER2) == 0L) {
                st.giveItems(HATOSS_ORDER2, 1L);
            }
        } else if ("urutu_chief_hatos_q0107_09.htm".equalsIgnoreCase(event)) {
            st.takeItems(HATOSS_ORDER2, 1L);
            if (st.getQuestItemsCount(HATOSS_ORDER3) == 0L) {
                st.giveItems(HATOSS_ORDER3, 1L);
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30568) {
            if (id == 1) {
                if (st.getPlayer().getRace() != Race.orc) {
                    htmltext = "urutu_chief_hatos_q0107_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() >= 10) {
                    htmltext = "urutu_chief_hatos_q0107_02.htm";
                } else {
                    htmltext = "urutu_chief_hatos_q0107_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(HATOSS_ORDER1) > 0L) {
                htmltext = "urutu_chief_hatos_q0107_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(HATOSS_ORDER1) > 0L && st.getQuestItemsCount(LETTER_TO_HUMAN) == 0L) {
                htmltext = "urutu_chief_hatos_q0107_04.htm";
            } else if (cond == 3 && st.getQuestItemsCount(HATOSS_ORDER1) > 0L && st.getQuestItemsCount(LETTER_TO_HUMAN) >= 1L) {
                htmltext = "urutu_chief_hatos_q0107_05.htm";
                st.setCond(4);
            } else if (cond == 4 && st.getQuestItemsCount(HATOSS_ORDER2) > 0L && st.getQuestItemsCount(LETTER_TO_DARKELF) == 0L) {
                htmltext = "urutu_chief_hatos_q0107_05.htm";
            } else if (cond == 5 && st.getQuestItemsCount(HATOSS_ORDER2) > 0L && st.getQuestItemsCount(LETTER_TO_DARKELF) >= 1L) {
                htmltext = "urutu_chief_hatos_q0107_08.htm";
                st.setCond(6);
            } else if (cond == 6 && st.getQuestItemsCount(HATOSS_ORDER3) > 0L && st.getQuestItemsCount(LETTER_TO_ELF) == 0L) {
                htmltext = "urutu_chief_hatos_q0107_08.htm";
            } else if (cond == 7 && st.getQuestItemsCount(HATOSS_ORDER3) > 0L && st.getQuestItemsCount(LETTER_TO_ELF) + st.getQuestItemsCount(LETTER_TO_HUMAN) + st.getQuestItemsCount(LETTER_TO_DARKELF) == 3L) {
                htmltext = "urutu_chief_hatos_q0107_10.htm";
                st.takeItems(LETTER_TO_DARKELF, -1L);
                st.takeItems(LETTER_TO_HUMAN, -1L);
                st.takeItems(LETTER_TO_ELF, -1L);
                st.takeItems(HATOSS_ORDER3, -1L);
                st.giveItems(BUTCHER, 1L);
                st.getPlayer().addExpAndSp(34565L, 2962L);
                st.giveItems(57, 14666L, false);
                if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3")) {
                    st.getPlayer().setVar("p1q3", "1", -1L);
                    st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of race-specific weapon complete.\n           Go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    st.giveItems(1060, 100L);
                    for (int item = 4412; item <= 4417; ++item) {
                        st.giveItems(item, 10L);
                    }
                    st.playTutorialVoice("tutorial_voice_026");
                    st.giveItems(5789, 7000L);
                }
                st.exitCurrentQuest(false);
                st.playSound("ItemSound.quest_finish");
            }
        } else if (npcId == 30580 && cond >= 1 && (st.getQuestItemsCount(HATOSS_ORDER1) > 0L || st.getQuestItemsCount(HATOSS_ORDER2) > 0L || st.getQuestItemsCount(HATOSS_ORDER3) > 0L)) {
            if (cond == 1) {
                st.setCond(2);
            }
            htmltext = "centurion_parugon_q0107_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 27041) {
            if (cond == 2 && st.getQuestItemsCount(HATOSS_ORDER1) > 0L && st.getQuestItemsCount(LETTER_TO_HUMAN) == 0L) {
                st.giveItems(LETTER_TO_HUMAN, 1L);
                st.setCond(3);
                st.playSound("ItemSound.quest_itemget");
            } else if (cond == 4 && st.getQuestItemsCount(HATOSS_ORDER2) > 0L && st.getQuestItemsCount(LETTER_TO_DARKELF) == 0L) {
                st.giveItems(LETTER_TO_DARKELF, 1L);
                st.setCond(5);
                st.playSound("ItemSound.quest_itemget");
            } else if (cond == 6 && st.getQuestItemsCount(HATOSS_ORDER3) > 0L && st.getQuestItemsCount(LETTER_TO_ELF) == 0L) {
                st.giveItems(LETTER_TO_ELF, 1L);
                st.setCond(7);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
