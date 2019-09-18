package quests;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _225_TestOfTheSearcher extends Quest {
    private static final int Luther = 30690;
    private static final int Alex = 30291;
    private static final int Tyra = 30420;
    private static final int Chest = 30628;
    private static final int Leirynn = 30728;
    private static final int Borys = 30729;
    private static final int Jax = 30730;
    private static final int Tree = 30627;
    private static final int LuthersLetter = 2784;
    private static final int AlexsWarrant = 2785;
    private static final int Leirynns1stOrder = 2786;
    private static final int DeluTotem = 2787;
    private static final int Leirynns2ndOrder = 2788;
    private static final int ChiefKalkisFang = 2789;
    private static final int AlexsRecommend = 2808;
    private static final int LambertsMap = 2792;
    private static final int LeirynnsReport = 2790;
    private static final int AlexsLetter = 2793;
    private static final int StrangeMap = 2791;
    private static final int AlexsOrder = 2794;
    private static final int CombinedMap = 2805;
    private static final int GoldBar = 2807;
    private static final int WineCatalog = 2795;
    private static final int OldOrder = 2799;
    private static final int MalrukianWine = 2798;
    private static final int TyrasContract = 2796;
    private static final int RedSporeDust = 2797;
    private static final int JaxsDiary = 2800;
    private static final int SoltsMap = 2803;
    private static final int MakelsMap = 2804;
    private static final int RustedKey = 2806;
    private static final int TornMapPiece1st = 2801;
    private static final int TornMapPiece2st = 2802;
    private static final int MarkOfSearcher = 2809;
    private static final int DeluLizardmanShaman = 20781;
    private static final int DeluLizardmanAssassin = 27094;
    private static final int DeluChiefKalkis = 27093;
    private static final int GiantFungus = 20555;
    private static final int RoadScavenger = 20551;
    private static final int HangmanTree = 20144;
    private static final int[][] DROPLIST_COND = {{3, 4, 20781, 0, 2787, 10, 100, 1}, {3, 4, 27094, 0, 2787, 10, 100, 1}, {10, 11, 20555, 0, 2797, 10, 100, 1}};

    public _225_TestOfTheSearcher() {
        super(false);
        addStartNpc(30690);
        addTalkId(30291);
        addTalkId(30728);
        addTalkId(30729);
        addTalkId(30420);
        addTalkId(30730);
        addTalkId(30627);
        addTalkId(30628);
        addKillId(27093);
        addKillId(20551);
        addKillId(20144);
        for (int i = 0; i < _225_TestOfTheSearcher.DROPLIST_COND.length; ++i) {
            addKillId(_225_TestOfTheSearcher.DROPLIST_COND[i][2]);
        }
        addQuestItem(2787, 2797, 2784, 2785, 2786, 2788, 2790, 2789, 2791, 2792, 2793, 2794, 2795, 2796, 2799, 2798, 2800, 2801, 2802, 2803, 2804, 2806, 2805);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30690-05.htm".equalsIgnoreCase(event)) {
            st.giveItems(2784, 1L);
            st.setCond(1);
            st.setState(2);
            if (!st.getPlayer().getVarB("dd3")) {
                st.giveItems(7562, 82L);
                st.getPlayer().setVar("dd3", "1", -1L);
            }
            st.playSound("ItemSound.quest_accept");
        } else if ("30291-07.htm".equalsIgnoreCase(event)) {
            st.takeItems(2790, -1L);
            st.takeItems(2791, -1L);
            st.giveItems(2792, 1L);
            st.giveItems(2793, 1L);
            st.giveItems(2794, 1L);
            st.setCond(8);
            st.setState(2);
        } else if ("30420-01a.htm".equalsIgnoreCase(event)) {
            st.takeItems(2795, -1L);
            st.giveItems(2796, 1L);
            st.setCond(10);
            st.setState(2);
        } else if ("30730-01d.htm".equalsIgnoreCase(event)) {
            st.takeItems(2799, -1L);
            st.giveItems(2800, 1L);
            st.setCond(14);
            st.setState(2);
        } else if ("30627-01a.htm".equalsIgnoreCase(event)) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(30628);
            if (isQuest == null) {
                if (st.getQuestItemsCount(2806) == 0L) {
                    st.giveItems(2806, 1L);
                }
                st.addSpawn(30628);
                st.startQuestTimer("Chest", 300000L);
                st.setCond(17);
                st.setState(2);
            } else {
                if (!st.isRunningQuestTimer("Wait1")) {
                    st.startQuestTimer("Wait1", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            }
        } else if ("30628-01a.htm".equalsIgnoreCase(event)) {
            st.takeItems(2806, -1L);
            st.giveItems(2807, 20L);
            st.setCond(18);
        } else if ("Wait1".equalsIgnoreCase(event) || "Chest".equalsIgnoreCase(event)) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(30628);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.cancelQuestTimer("Wait1");
            st.cancelQuestTimer("Chest");
            if (st.getCond() == 17) {
                st.setCond(16);
            }
            return null;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30690:
                if (st.getQuestItemsCount(2809) != 0L) {
                    htmltext = "completed";
                    st.exitCurrentQuest(true);
                } else if (cond == 0) {
                    if (st.getPlayer().getClassId().getId() == 7 || st.getPlayer().getClassId().getId() == 22 || st.getPlayer().getClassId().getId() == 35 || st.getPlayer().getClassId().getId() == 54) {
                        if (st.getPlayer().getLevel() >= 39) {
                            if (st.getPlayer().getClassId().getId() == 54) {
                                htmltext = "30690-04.htm";
                            } else {
                                htmltext = "30690-03.htm";
                            }
                        } else {
                            htmltext = "30690-02.htm";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "30690-01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1) {
                    htmltext = "30690-06.htm";
                } else if (cond > 1 && cond < 16) {
                    htmltext = "30623-17.htm";
                } else if (cond == 19) {
                    htmltext = "30690-08.htm";
                    if (!st.getPlayer().getVarB("prof2.3")) {
                        st.addExpAndSp(37831L, 18750L);
                        st.getPlayer().setVar("prof2.3", "1", -1L);
                    }
                    st.takeItems(2808, -1L);
                    st.giveItems(2809, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                }
                break;
            case 30291:
                if (cond == 1) {
                    htmltext = "30291-01.htm";
                    st.takeItems(2784, -1L);
                    st.giveItems(2785, 1L);
                    st.setCond(2);
                    st.setState(2);
                } else if (cond == 2) {
                    htmltext = "30291-02.htm";
                } else if (cond > 2 && cond < 7) {
                    htmltext = "30291-03.htm";
                } else if (cond == 7) {
                    htmltext = "30291-04.htm";
                } else if (cond == 8) {
                    htmltext = "30291-08.htm";
                } else if (cond == 13 || cond == 14) {
                    htmltext = "30291-09.htm";
                } else if (cond == 18) {
                    st.takeItems(2794, -1L);
                    st.takeItems(2805, -1L);
                    st.takeItems(2807, -1L);
                    st.giveItems(2808, 1L);
                    htmltext = "30291-11.htm";
                    st.setCond(19);
                    st.setState(2);
                } else if (cond == 19) {
                    htmltext = "30291-12.htm";
                }
                break;
            case 30728:
                switch (cond) {
                    case 2:
                        htmltext = "30728-01.htm";
                        st.takeItems(2785, -1L);
                        st.giveItems(2786, 1L);
                        st.setCond(3);
                        st.setState(2);
                        break;
                    case 3:
                        htmltext = "30728-02.htm";
                        break;
                    case 4:
                        htmltext = "30728-03.htm";
                        st.takeItems(2787, -1L);
                        st.takeItems(2786, -1L);
                        st.giveItems(2788, 1L);
                        st.setCond(5);
                        st.setState(2);
                        break;
                    case 5:
                        htmltext = "30728-04.htm";
                        break;
                    case 6:
                        st.takeItems(2789, -1L);
                        st.takeItems(2788, -1L);
                        st.giveItems(2790, 1L);
                        htmltext = "30728-05.htm";
                        st.setCond(7);
                        st.setState(2);
                        break;
                    case 7:
                        htmltext = "30728-06.htm";
                        break;
                    case 8:
                        htmltext = "30728-07.htm";
                        break;
                }
                break;
            case 30729:
                if (cond == 8) {
                    st.takeItems(2793, -1L);
                    st.giveItems(2795, 1L);
                    htmltext = "30729-01.htm";
                    st.setCond(9);
                    st.setState(2);
                } else if (cond == 9) {
                    htmltext = "30729-02.htm";
                } else if (cond == 12) {
                    st.takeItems(2795, -1L);
                    st.takeItems(2798, -1L);
                    st.giveItems(2799, 1L);
                    htmltext = "30729-03.htm";
                    st.setCond(13);
                    st.setState(2);
                } else if (cond == 13) {
                    htmltext = "30729-04.htm";
                } else if (cond >= 8 && cond <= 14) {
                    htmltext = "30729-05.htm";
                }
                break;
            case 30420:
                switch (cond) {
                    case 9:
                        htmltext = "30420-01.htm";
                        break;
                    case 10:
                        htmltext = "30420-02.htm";
                        break;
                    case 11:
                        st.takeItems(2796, -1L);
                        st.takeItems(2797, -1L);
                        st.giveItems(2798, 1L);
                        htmltext = "30420-03.htm";
                        st.setCond(12);
                        st.setState(2);
                        break;
                    case 12:
                    case 13:
                        htmltext = "30420-04.htm";
                        break;
                }
                break;
            case 30730:
                switch (cond) {
                    case 13:
                        htmltext = "30730-01.htm";
                        break;
                    case 14:
                        htmltext = "30730-02.htm";
                        break;
                    case 15:
                        st.takeItems(2803, -1L);
                        st.takeItems(2804, -1L);
                        st.takeItems(2792, -1L);
                        st.takeItems(2800, -1L);
                        st.giveItems(2805, 1L);
                        htmltext = "30730-03.htm";
                        st.setCond(16);
                        st.setState(2);
                        break;
                    case 16:
                        htmltext = "30730-04.htm";
                        break;
                }
                break;
            case 30627:
                if (cond == 16 || cond == 17) {
                    htmltext = "30627-01.htm";
                }
                break;
            case 30628:
                if (cond == 17) {
                    htmltext = "30628-01.htm";
                } else {
                    htmltext = "<html><head><body>You haven't got a Key for this Chest.</body></html>";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _225_TestOfTheSearcher.DROPLIST_COND.length; ++i) {
            if (cond == _225_TestOfTheSearcher.DROPLIST_COND[i][0] && npcId == _225_TestOfTheSearcher.DROPLIST_COND[i][2] && (_225_TestOfTheSearcher.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_225_TestOfTheSearcher.DROPLIST_COND[i][3]) > 0L)) {
                if (_225_TestOfTheSearcher.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_225_TestOfTheSearcher.DROPLIST_COND[i][4], _225_TestOfTheSearcher.DROPLIST_COND[i][7], (double) _225_TestOfTheSearcher.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_225_TestOfTheSearcher.DROPLIST_COND[i][4], _225_TestOfTheSearcher.DROPLIST_COND[i][7], _225_TestOfTheSearcher.DROPLIST_COND[i][7], _225_TestOfTheSearcher.DROPLIST_COND[i][5], (double) _225_TestOfTheSearcher.DROPLIST_COND[i][6]) && _225_TestOfTheSearcher.DROPLIST_COND[i][1] != cond && _225_TestOfTheSearcher.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_225_TestOfTheSearcher.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 5 && npcId == 27093) {
            if (st.getQuestItemsCount(2791) == 0L) {
                st.giveItems(2791, 1L);
            }
            if (st.getQuestItemsCount(2789) == 0L) {
                st.giveItems(2789, 1L);
            }
            st.playSound("ItemSound.quest_middle");
            st.setCond(6);
            st.setState(2);
        } else if (cond == 14) {
            if (npcId == 20551 && st.getQuestItemsCount(2803) == 0L) {
                st.giveItems(2801, 1L);
                if (st.getQuestItemsCount(2801) >= 4L) {
                    st.takeItems(2801, -1L);
                    st.giveItems(2803, 1L);
                }
            } else if (npcId == 20144 && st.getQuestItemsCount(2804) == 0L) {
                st.giveItems(2802, 1L);
                if (st.getQuestItemsCount(2802) >= 4L) {
                    st.takeItems(2802, -1L);
                    st.giveItems(2804, 1L);
                }
            }
            if (st.getQuestItemsCount(2803) != 0L && st.getQuestItemsCount(2804) != 0L) {
                st.setCond(15);
                st.setState(2);
            }
        }
        return null;
    }
}
