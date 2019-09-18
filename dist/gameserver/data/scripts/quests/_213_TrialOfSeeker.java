package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _213_TrialOfSeeker extends Quest {
    private static final int Dufner = 30106;
    private static final int Terry = 30064;
    private static final int Viktor = 30684;
    private static final int Marina = 30715;
    private static final int Brunon = 30526;
    private static final int DufnersLetter = 2647;
    private static final int Terrys1stOrder = 2648;
    private static final int Terrys2ndOrder = 2649;
    private static final int TerrysLetter = 2650;
    private static final int ViktorsLetter = 2651;
    private static final int HawkeyesLetter = 2652;
    private static final int MysteriousRunestone = 2653;
    private static final int OlMahumRunestone = 2654;
    private static final int TurekRunestone = 2655;
    private static final int AntRunestone = 2656;
    private static final int TurakBugbearRunestone = 2657;
    private static final int TerrysBox = 2658;
    private static final int ViktorsRequest = 2659;
    private static final int MedusaScales = 2660;
    private static final int ShilensRunestone = 2661;
    private static final int AnalysisRequest = 2662;
    private static final int MarinasLetter = 2663;
    private static final int ExperimentTools = 2664;
    private static final int AnalysisResult = 2665;
    private static final int Terrys3rdOrder = 2666;
    private static final int ListOfHost = 2667;
    private static final int AbyssRunestone1 = 2668;
    private static final int AbyssRunestone2 = 2669;
    private static final int AbyssRunestone3 = 2670;
    private static final int AbyssRunestone4 = 2671;
    private static final int TerrysReport = 2672;
    private static final int MarkofSeeker = 2673;
    private static final int NeerGhoulBerserker = 20198;
    private static final int OlMahumCaptain = 20211;
    private static final int TurekOrcWarlord = 20495;
    private static final int AntCaptain = 20080;
    private static final int TurakBugbearWarrior = 20249;
    private static final int Medusa = 20158;
    private static final int MarshStakatoDrone = 20234;
    private static final int BrekaOrcOverlord = 20270;
    private static final int AntWarriorCaptain = 20088;
    private static final int LetoLizardmanWarrior = 20580;
    private static final int[][] DROPLIST_COND = {{2, 3, 20198, 2648, 2653, 1, 10, 1}, {4, 0, 20211, 2649, 2654, 1, 20, 1}, {4, 0, 20495, 2649, 2655, 1, 20, 1}, {4, 0, 20080, 2649, 2656, 1, 20, 1}, {4, 0, 20249, 2649, 2657, 1, 20, 1}, {9, 10, 20158, 2659, 2660, 10, 30, 1}, {16, 0, 20234, 2667, 2668, 1, 25, 1}, {16, 0, 20270, 2667, 2669, 1, 25, 1}, {16, 0, 20088, 2667, 2670, 1, 25, 1}, {16, 0, 20580, 2667, 2671, 1, 25, 1}};

    public _213_TrialOfSeeker() {
        super(false);
        addStartNpc(30106);
        addTalkId(30106);
        addTalkId(30064);
        addTalkId(30684);
        addTalkId(30715);
        addTalkId(30526);
        for (int i = 0; i < _213_TrialOfSeeker.DROPLIST_COND.length; ++i) {
            addKillId(_213_TrialOfSeeker.DROPLIST_COND[i][2]);
        }
        addQuestItem(2647, 2648, 2649, 2650, 2658, 2651, 2659, 2652, 2661, 2662, 2663, 2664, 2665, 2667, 2666, 2672, 2653, 2654, 2655, 2656, 2657, 2660, 2668, 2669, 2670, 2671);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("dufner_q0213_05a.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2647, 1L);
            if (!st.getPlayer().getVarB("dd1")) {
                st.giveItems(7562, 128L, false);
                st.getPlayer().setVar("dd1", "1", -1L);
            }
        } else if ("terry_q0213_03.htm".equalsIgnoreCase(event)) {
            st.giveItems(2648, 1L);
            st.takeItems(2647, -1L);
            st.setCond(2);
            st.setState(2);
        } else if ("terry_q0213_07.htm".equalsIgnoreCase(event)) {
            st.takeItems(2648, -1L);
            st.takeItems(2653, -1L);
            st.giveItems(2649, 1L);
            st.setCond(4);
            st.setState(2);
        } else if ("terry_q0213_10.htm".equalsIgnoreCase(event)) {
            st.takeItems(2649, -1L);
            st.takeItems(2654, -1L);
            st.takeItems(2655, -1L);
            st.takeItems(2656, -1L);
            st.takeItems(2657, -1L);
            st.giveItems(2650, 1L);
            st.giveItems(2658, 1L);
            st.setCond(6);
            st.setState(2);
        } else if ("trader_viktor_q0213_05.htm".equalsIgnoreCase(event)) {
            st.takeItems(2650, -1L);
            st.giveItems(2651, 1L);
            st.setCond(7);
            st.setState(2);
        } else if ("trader_viktor_q0213_11.htm".equalsIgnoreCase(event)) {
            st.takeItems(2650, -1L);
            st.takeItems(2658, -1L);
            st.takeItems(2652, -1L);
            st.giveItems(2659, 1L);
            st.setCond(9);
            st.setState(2);
        } else if ("trader_viktor_q0213_15.htm".equalsIgnoreCase(event)) {
            st.takeItems(2659, -1L);
            st.takeItems(2660, -1L);
            st.giveItems(2661, 1L);
            st.giveItems(2662, 1L);
            st.setCond(11);
            st.setState(2);
        } else if ("magister_marina_q0213_02.htm".equalsIgnoreCase(event)) {
            st.takeItems(2661, -1L);
            st.takeItems(2662, -1L);
            st.giveItems(2663, 1L);
            st.setCond(12);
            st.setState(2);
        } else if ("magister_marina_q0213_05.htm".equalsIgnoreCase(event)) {
            st.takeItems(2664, 1L);
            st.giveItems(2665, 1L);
            st.setCond(14);
            st.setState(2);
        } else if ("terry_q0213_18.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() < 36) {
                htmltext = "terry_q0213_17.htm";
                st.takeItems(2665, -1L);
                st.giveItems(2666, 1L);
            } else {
                htmltext = "terry_q0213_18.htm";
                st.giveItems(2667, 1L);
                st.takeItems(2665, -1L);
                st.setCond(16);
                st.setState(2);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30106:
                if (st.getQuestItemsCount(2673) != 0L) {
                    htmltext = "completed";
                    st.exitCurrentQuest(true);
                } else if (cond == 0 && st.getQuestItemsCount(2672) == 0L) {
                    if (st.getPlayer().getClassId().ordinal() == 7 || st.getPlayer().getClassId().ordinal() == 22 || st.getPlayer().getClassId().ordinal() == 35) {
                        if (st.getPlayer().getLevel() < 35) {
                            htmltext = "dufner_q0213_02.htm";
                            st.exitCurrentQuest(true);
                        } else {
                            htmltext = "dufner_q0213_03.htm";
                        }
                    } else {
                        htmltext = "dufner_q0213_00.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (st.getQuestItemsCount(2647) == 1L && st.getQuestItemsCount(2672) == 0L) {
                    htmltext = "dufner_q0213_06.htm";
                } else if (st.getQuestItemsCount(2647) == 0L && st.getQuestItemsCount(2672) == 0L) {
                    htmltext = "dufner_q0213_07.htm";
                } else if (st.getQuestItemsCount(2672) != 0L) {
                    if (!st.getPlayer().getVarB("prof2.1")) {
                        st.addExpAndSp(72126L, 11000L);
                        st.getPlayer().setVar("prof2.1", "1", -1L);
                    }
                    htmltext = "dufner_q0213_08.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.takeItems(2672, -1L);
                    st.giveItems(2673, 1L);
                    st.exitCurrentQuest(true);
                }
                break;
            case 30064:
                if (cond == 1) {
                    htmltext = "terry_q0213_01.htm";
                } else if (cond == 2) {
                    htmltext = "terry_q0213_04.htm";
                } else if (cond == 3 && st.getQuestItemsCount(2653) == 0L) {
                    htmltext = "terry_q0213_04.htm";
                    st.setCond(2);
                } else if (cond == 3) {
                    htmltext = "terry_q0213_05.htm";
                } else if (cond == 4) {
                    htmltext = "terry_q0213_07.htm";
                } else if (cond == 5 && st.getQuestItemsCount(2654) != 0L && st.getQuestItemsCount(2655) != 0L && st.getQuestItemsCount(2656) != 0L && st.getQuestItemsCount(2657) != 0L) {
                    htmltext = "terry_q0213_09.htm";
                } else if (cond == 5) {
                    htmltext = "terry_q0213_07.htm";
                    st.setCond(4);
                } else if (cond == 6) {
                    htmltext = "terry_q0213_11.htm";
                } else if (cond == 7) {
                    st.takeItems(2651, -1L);
                    st.giveItems(2652, 1L);
                    htmltext = "terry_q0213_12.htm";
                    st.setCond(8);
                    st.setState(2);
                } else if (cond == 8) {
                    htmltext = "terry_q0213_13.htm";
                } else if (cond > 8 && cond < 14) {
                    htmltext = "terry_q0213_14.htm";
                } else if (cond == 14 && st.getQuestItemsCount(2665) > 0L) {
                    htmltext = "terry_q0213_15.htm";
                } else if ((cond == 14 || cond == 15) && st.getQuestItemsCount(2666) > 0L) {
                    if (st.getPlayer().getLevel() < 36) {
                        htmltext = "terry_q0213_20.htm";
                    } else {
                        htmltext = "terry_q0213_21.htm";
                        st.takeItems(2666, -1L);
                        st.giveItems(2667, 1L);
                        st.setCond(16);
                        st.setState(2);
                    }
                } else if (cond == 15 || cond == 16) {
                    htmltext = "terry_q0213_22.htm";
                } else if (cond == 17) {
                    if (st.getQuestItemsCount(2668) != 0L && st.getQuestItemsCount(2669) != 0L && st.getQuestItemsCount(2670) != 0L && st.getQuestItemsCount(2671) != 0L) {
                        htmltext = "terry_q0213_23.htm";
                        st.takeItems(2667, -1L);
                        st.takeItems(2668, -1L);
                        st.takeItems(2669, -1L);
                        st.takeItems(2670, -1L);
                        st.takeItems(2671, -1L);
                        st.giveItems(2672, 1L);
                        st.setCond(0);
                    } else {
                        htmltext = "terry_q0213_22.htm";
                        st.setCond(16);
                    }
                }
                break;
            case 30684:
                if (cond == 6) {
                    htmltext = "trader_viktor_q0213_01.htm";
                } else if (cond == 8) {
                    htmltext = "trader_viktor_q0213_12.htm";
                } else if (cond == 9) {
                    htmltext = "trader_viktor_q0213_13.htm";
                } else if (cond == 10 && st.getQuestItemsCount(2660) >= 10L) {
                    htmltext = "trader_viktor_q0213_14.htm";
                } else if (cond == 10) {
                    st.setCond(9);
                    htmltext = "trader_viktor_q0213_13.htm";
                }
                break;
            case 30715:
                if (cond == 11) {
                    htmltext = "magister_marina_q0213_01.htm";
                } else if (cond == 12) {
                    htmltext = "magister_marina_q0213_03.htm";
                } else if (cond == 13) {
                    htmltext = "magister_marina_q0213_04.htm";
                } else if (cond > 13) {
                    htmltext = "magister_marina_q0213_06.htm";
                }
                break;
            case 30526:
                if (cond == 12) {
                    htmltext = "blacksmith_bronp_q0213_01.htm";
                    st.takeItems(2663, 1L);
                    st.giveItems(2664, 1L);
                    st.setCond(13);
                    st.setState(2);
                } else if (cond == 13) {
                    htmltext = "blacksmith_bronp_q0213_02.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _213_TrialOfSeeker.DROPLIST_COND.length; ++i) {
            if (cond == _213_TrialOfSeeker.DROPLIST_COND[i][0] && npcId == _213_TrialOfSeeker.DROPLIST_COND[i][2] && (_213_TrialOfSeeker.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_213_TrialOfSeeker.DROPLIST_COND[i][3]) > 0L)) {
                if (_213_TrialOfSeeker.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_213_TrialOfSeeker.DROPLIST_COND[i][4], _213_TrialOfSeeker.DROPLIST_COND[i][7], (double) _213_TrialOfSeeker.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_213_TrialOfSeeker.DROPLIST_COND[i][4], _213_TrialOfSeeker.DROPLIST_COND[i][7], _213_TrialOfSeeker.DROPLIST_COND[i][7], _213_TrialOfSeeker.DROPLIST_COND[i][5], (double) _213_TrialOfSeeker.DROPLIST_COND[i][6]) && _213_TrialOfSeeker.DROPLIST_COND[i][1] != cond && _213_TrialOfSeeker.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_213_TrialOfSeeker.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 4 && st.getQuestItemsCount(2654) != 0L && st.getQuestItemsCount(2655) != 0L && st.getQuestItemsCount(2656) != 0L && st.getQuestItemsCount(2657) != 0L) {
            st.setCond(5);
            st.setState(2);
        } else if (cond == 16 && st.getQuestItemsCount(2668) != 0L && st.getQuestItemsCount(2669) != 0L && st.getQuestItemsCount(2670) != 0L && st.getQuestItemsCount(2671) != 0L) {
            st.setCond(17);
            st.setState(2);
        }
        return null;
    }
}
