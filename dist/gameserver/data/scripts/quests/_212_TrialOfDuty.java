package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _212_TrialOfDuty extends Quest {
    private static final int MARK_OF_DUTY_ID = 2633;
    private static final int LETTER_OF_DUSTIN_ID = 2634;
    private static final int KNIGHTS_TEAR_ID = 2635;
    private static final int MIRROR_OF_ORPIC_ID = 2636;
    private static final int TEAR_OF_CONFESSION_ID = 2637;
    private static final int REPORT_PIECE_ID = 2638;
    private static final int TALIANUSS_REPORT_ID = 2639;
    private static final int TEAR_OF_LOYALTY_ID = 2640;
    private static final int MILITAS_ARTICLE_ID = 2641;
    private static final int SAINTS_ASHES_URN_ID = 2642;
    private static final int ATEBALTS_SKULL_ID = 2643;
    private static final int ATEBALTS_RIBS_ID = 2644;
    private static final int ATEBALTS_SHIN_ID = 2645;
    private static final int LETTER_OF_WINDAWOOD_ID = 2646;
    private static final int OLD_KNIGHT_SWORD_ID = 3027;
    private static final int RewardExp = 79832;
    private static final int RewardSP = 37500;

    public _212_TrialOfDuty() {
        super(false);
        addStartNpc(30109);
        addTalkId(30109);
        addTalkId(30109);
        addTalkId(30109);
        addTalkId(30116);
        addTalkId(30311);
        for (int i = 30653; i < 30657; ++i) {
            addTalkId(i);
        }
        addKillId(20144);
        addKillId(20190);
        addKillId(20191);
        addKillId(20200);
        addKillId(20201);
        addKillId(20270);
        addKillId(27119);
        for (int j = 20577; j < 20583; ++j) {
            addKillId(j);
        }
        addQuestItem(2634, 2635, 3027, 2637, 2636, 2639, 2641, 2643, 2644, 2645, 2646, 2640, 2642, 2638);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            htmltext = "hannavalt_q0212_04.htm";
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
            if (!st.getPlayer().getVarB("dd1")) {
                st.giveItems(7562, 45L);
                st.getPlayer().setVar("dd1", "1", -1L);
            }
        } else if ("30116_1".equalsIgnoreCase(event)) {
            htmltext = "dustin_q0212_02.htm";
        } else if ("30116_2".equalsIgnoreCase(event)) {
            htmltext = "dustin_q0212_03.htm";
        } else if ("30116_3".equalsIgnoreCase(event)) {
            htmltext = "dustin_q0212_04.htm";
        } else if ("30116_4".equalsIgnoreCase(event)) {
            htmltext = "dustin_q0212_05.htm";
            st.takeItems(2640, 1L);
            st.setCond(14);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(2633) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (npcId == 30109 && id == 1) {
            if (st.getPlayer().getClassId().ordinal() == 4 || st.getPlayer().getClassId().ordinal() == 19 || st.getPlayer().getClassId().ordinal() == 32) {
                if (st.getPlayer().getLevel() >= 35) {
                    htmltext = "hannavalt_q0212_03.htm";
                } else {
                    htmltext = "hannavalt_q0212_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "hannavalt_q0212_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30109) {
            if (cond == 18 && st.getQuestItemsCount(2634) > 0L) {
                htmltext = "hannavalt_q0212_05.htm";
                st.takeItems(2634, -1L);
                st.giveItems(2633, 1L);
                if (!st.getPlayer().getVarB("prof2.1")) {
                    st.addExpAndSp(79832L, 37500L);
                    st.getPlayer().setVar("prof2.1", "1", -1L);
                }
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            } else if (cond == 1) {
                htmltext = "hannavalt_q0212_04.htm";
            }
        } else if (npcId == 30653) {
            if (cond == 1) {
                htmltext = "sir_aron_tanford_q0212_01.htm";
                if (st.getQuestItemsCount(3027) == 0L) {
                    st.giveItems(3027, 1L);
                }
                st.setCond(2);
            } else if (cond == 2 && st.getQuestItemsCount(2635) == 0L) {
                htmltext = "sir_aron_tanford_q0212_02.htm";
            } else if (cond == 3 && st.getQuestItemsCount(2635) > 0L) {
                htmltext = "sir_aron_tanford_q0212_03.htm";
                st.takeItems(2635, 1L);
                st.takeItems(3027, 1L);
                st.setCond(4);
            } else if (cond == 4) {
                htmltext = "sir_aron_tanford_q0212_04.htm";
            }
        } else if (npcId == 30654) {
            if (cond == 4) {
                htmltext = "sir_kiel_nighthawk_q0212_01.htm";
                st.setCond(5);
            } else if (cond == 5 && st.getQuestItemsCount(2639) == 0L) {
                htmltext = "sir_kiel_nighthawk_q0212_02.htm";
            } else if (cond == 6 && st.getQuestItemsCount(2639) > 0L) {
                htmltext = "sir_kiel_nighthawk_q0212_03.htm";
                st.setCond(7);
                st.giveItems(2636, 1L);
            } else if (cond == 6 && st.getQuestItemsCount(2636) > 0L) {
                htmltext = "sir_kiel_nighthawk_q0212_04.htm";
            } else if (st.getQuestItemsCount(2637) > 0L) {
                htmltext = "sir_kiel_nighthawk_q0212_05.htm";
                st.takeItems(2637, 1L);
                st.setCond(10);
            } else if (cond == 10) {
                htmltext = "sir_kiel_nighthawk_q0212_06.htm";
            }
        } else if (npcId == 30656 && cond == 8 && st.getQuestItemsCount(2636) > 0L) {
            htmltext = "spirit_of_sir_talianus_q0212_01.htm";
            st.takeItems(2636, 1L);
            st.takeItems(2639, 1L);
            st.giveItems(2637, 1L);
            st.setCond(9);
        } else if (npcId == 30655) {
            if (cond == 10) {
                if (st.getPlayer().getLevel() >= 36) {
                    htmltext = "isael_silvershadow_q0212_02.htm";
                    st.setCond(11);
                } else {
                    htmltext = "isael_silvershadow_q0212_01.htm";
                }
            } else if (cond == 11) {
                htmltext = "isael_silvershadow_q0212_03.htm";
            } else if (cond == 12 && st.getQuestItemsCount(2641) >= 20L) {
                htmltext = "isael_silvershadow_q0212_04.htm";
                st.takeItems(2641, st.getQuestItemsCount(2641));
                st.giveItems(2640, 1L);
                st.setCond(13);
            } else if (cond == 13) {
                htmltext = "isael_silvershadow_q0212_05.htm";
            }
        } else if (npcId == 30116) {
            if (cond == 13 && st.getQuestItemsCount(2640) > 0L) {
                htmltext = "dustin_q0212_01.htm";
            } else if (cond == 14 && (st.getQuestItemsCount(2643) <= 0L || st.getQuestItemsCount(2644) <= 0L || st.getQuestItemsCount(2645) <= 0L)) {
                htmltext = "dustin_q0212_06.htm";
            } else if (cond == 15) {
                htmltext = "dustin_q0212_07.htm";
                st.takeItems(2643, 1L);
                st.takeItems(2644, 1L);
                st.takeItems(2645, 1L);
                st.giveItems(2642, 1L);
                st.setCond(16);
            } else if (cond == 17 && st.getQuestItemsCount(2646) > 0L) {
                htmltext = "dustin_q0212_08.htm";
                st.takeItems(2646, 1L);
                st.giveItems(2634, 1L);
                st.setCond(18);
            } else if (cond == 16) {
                htmltext = "dustin_q0212_09.htm";
            } else if (cond == 18) {
                htmltext = "dustin_q0212_10.htm";
            }
        } else if (npcId == 30311) {
            if (cond == 16 && st.getQuestItemsCount(2642) > 0L) {
                htmltext = "sir_collin_windawood_q0212_01.htm";
                st.takeItems(2642, 1L);
                st.giveItems(2646, 1L);
                st.setCond(17);
            } else if (cond == 17) {
                htmltext = "sir_collin_windawood_q0212_02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20190 || npcId == 20191) {
            if (cond == 2 && Rnd.chance(10)) {
                st.addSpawn(27119);
                st.playSound("Itemsound.quest_before_battle");
            }
        } else if (npcId == 27119 && cond == 2 && st.getQuestItemsCount(3027) > 0L) {
            st.giveItems(2635, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
        } else if ((npcId == 20200 || npcId == 20201) && cond == 5 && st.getQuestItemsCount(2639) == 0L) {
            if (Rnd.chance(50)) {
                st.giveItems(2638, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            if (st.getQuestItemsCount(2638) >= 10L) {
                st.takeItems(2638, st.getQuestItemsCount(2638));
                st.giveItems(2639, 1L);
                st.setCond(6);
                st.playSound("ItemSound.quest_middle");
            }
        } else if (npcId == 20144 && cond == 7 && Rnd.chance(20)) {
            st.addSpawn(30656, npc.getX(), npc.getY(), npc.getZ(), 300000);
            st.setCond(8);
            st.playSound("ItemSound.quest_middle");
        } else if (npcId >= 20577 && npcId <= 20582 && cond == 11 && st.getQuestItemsCount(2641) < 20L) {
            if (st.getQuestItemsCount(2641) == 19L) {
                st.giveItems(2641, 1L);
                st.setCond(12);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.giveItems(2641, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20270 && cond == 14 && Rnd.chance(50)) {
            if (st.getQuestItemsCount(2643) == 0L) {
                st.giveItems(2643, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(2644) == 0L) {
                st.giveItems(2644, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(2645) == 0L) {
                st.giveItems(2645, 1L);
                st.setCond(15);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
