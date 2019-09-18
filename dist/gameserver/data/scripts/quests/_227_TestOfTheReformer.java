package quests;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _227_TestOfTheReformer extends Quest {
    private static final int Pupina = 30118;
    private static final int Sla = 30666;
    private static final int Katari = 30668;
    private static final int OlMahumPilgrimNPC = 30732;
    private static final int Kakan = 30669;
    private static final int Nyakuri = 30670;
    private static final int Ramus = 30667;
    private static final int BookOfReform = 2822;
    private static final int LetterOfIntroduction = 2823;
    private static final int SlasLetter = 2824;
    private static final int Greetings = 2825;
    private static final int OlMahumMoney = 2826;
    private static final int KatarisLetter = 2827;
    private static final int NyakurisLetter = 2828;
    private static final int KakansLetter = 3037;
    private static final int UndeadList = 2829;
    private static final int RamussLetter = 2830;
    private static final int RippedDiary = 2831;
    private static final int HugeNail = 2832;
    private static final int LetterOfBetrayer = 2833;
    private static final int BoneFragment1 = 2834;
    private static final int BoneFragment2 = 2835;
    private static final int BoneFragment3 = 2836;
    private static final int BoneFragment4 = 2837;
    private static final int BoneFragment5 = 2838;
    private static final int MarkOfReformer = 2821;
    private static final int NamelessRevenant = 27099;
    private static final int Aruraune = 27128;
    private static final int OlMahumInspector = 27129;
    private static final int OlMahumBetrayer = 27130;
    private static final int CrimsonWerewolf = 27131;
    private static final int KrudelLizardman = 27132;
    private static final int SilentHorror = 20404;
    private static final int SkeletonLord = 20104;
    private static final int SkeletonMarksman = 20102;
    private static final int MiserySkeleton = 20022;
    private static final int SkeletonArcher = 20100;
    public final int[][] DROPLIST_COND;

    public _227_TestOfTheReformer() {
        super(false);
        DROPLIST_COND = new int[][]{{18, 0, 20404, 0, 2834, 1, 70, 1}, {18, 0, 20104, 0, 2835, 1, 70, 1}, {18, 0, 20102, 0, 2836, 1, 70, 1}, {18, 0, 20022, 0, 2837, 1, 70, 1}, {18, 0, 20100, 0, 2838, 1, 70, 1}};
        addStartNpc(30118);
        addTalkId(30666);
        addTalkId(30668);
        addTalkId(30732);
        addTalkId(30669);
        addTalkId(30670);
        addTalkId(30667);
        addKillId(27099);
        addKillId(27128);
        addKillId(27129);
        addKillId(27130);
        addKillId(27131);
        addKillId(27132);
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[2]);
            addQuestItem(aDROPLIST_COND[4]);
        }
        addQuestItem(2822, 2832, 2823, 2824, 2827, 2833, 2826, 2828, 2829, 2825, 3037, 2830, 2831);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30118-04.htm".equalsIgnoreCase(event)) {
            st.giveItems(2822, 1L);
            if (!st.getPlayer().getVarB("dd3")) {
                st.giveItems(7562, 60L);
                st.getPlayer().setVar("dd3", "1", -1L);
            }
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30118-06.htm".equalsIgnoreCase(event)) {
            st.takeItems(2832, -1L);
            st.takeItems(2822, -1L);
            st.giveItems(2823, 1L);
            st.setCond(4);
            st.setState(2);
        } else if ("30666-04.htm".equalsIgnoreCase(event)) {
            st.takeItems(2823, -1L);
            st.giveItems(2824, 1L);
            st.setCond(5);
            st.setState(2);
        } else if ("30669-03.htm".equalsIgnoreCase(event)) {
            if (GameObjectsStorage.getByNpcId(27131) == null) {
                st.setCond(12);
                st.setState(2);
                st.addSpawn(27131);
                st.startQuestTimer("Wait4", 300000L);
            } else {
                if (!st.isRunningQuestTimer("Wait4")) {
                    st.startQuestTimer("Wait4", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            }
        } else if ("30670-03.htm".equalsIgnoreCase(event)) {
            if (GameObjectsStorage.getByNpcId(27132) == null) {
                st.setCond(15);
                st.setState(2);
                st.addSpawn(27132);
                st.startQuestTimer("Wait5", 300000L);
            } else {
                if (!st.isRunningQuestTimer("Wait5")) {
                    st.startQuestTimer("Wait5", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            }
        } else {
            if ("Wait1".equalsIgnoreCase(event)) {
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27128);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getCond() == 2) {
                    st.setCond(1);
                }
                return null;
            }
            if ("Wait2".equalsIgnoreCase(event)) {
                NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                isQuest = GameObjectsStorage.getByNpcId(30732);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getCond() == 6) {
                    st.setCond(5);
                }
                return null;
            }
            if ("Wait3".equalsIgnoreCase(event)) {
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27130);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                return null;
            }
            if ("Wait4".equalsIgnoreCase(event)) {
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27131);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getCond() == 12) {
                    st.setCond(11);
                }
                return null;
            }
            if ("Wait5".equalsIgnoreCase(event)) {
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27132);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getCond() == 15) {
                    st.setCond(14);
                }
                return null;
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
            case 30118:
                if (st.getQuestItemsCount(2821) != 0L) {
                    htmltext = "completed";
                    st.exitCurrentQuest(true);
                } else if (cond == 0) {
                    if (st.getPlayer().getClassId().getId() == 15 || st.getPlayer().getClassId().getId() == 42) {
                        if (st.getPlayer().getLevel() >= 39) {
                            htmltext = "30118-03.htm";
                        } else {
                            htmltext = "30118-01.htm";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "30118-02.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 3) {
                    htmltext = "30118-05.htm";
                } else if (cond >= 4) {
                    htmltext = "30118-07.htm";
                }
                break;
            case 30666:
                switch (cond) {
                    case 4:
                        htmltext = "30666-01.htm";
                        break;
                    case 5:
                        htmltext = "30666-05.htm";
                        break;
                    case 10:
                        st.takeItems(2826, -1L);
                        st.giveItems(2825, 3L);
                        htmltext = "30666-06.htm";
                        st.setCond(11);
                        st.setState(2);
                        break;
                    case 20:
                        st.takeItems(2827, -1L);
                        st.takeItems(3037, -1L);
                        st.takeItems(2828, -1L);
                        st.takeItems(2830, -1L);
                        st.giveItems(2821, 1L);
                        if (!st.getPlayer().getVarB("prof2.3")) {
                            st.addExpAndSp(164032L, 17500L);
                            st.getPlayer().setVar("prof2.3", "1", -1L);
                        }
                        htmltext = "30666-07.htm";
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(true);
                        break;
                }
                break;
            case 30668:
                switch (cond) {
                    case 5:
                    case 6:
                        final NpcInstance NPC = GameObjectsStorage.getByNpcId(30732);
                        final NpcInstance Mob = GameObjectsStorage.getByNpcId(27129);
                        if (NPC == null && Mob == null) {
                            st.takeItems(2824, -1L);
                            htmltext = "30668-01.htm";
                            st.setCond(6);
                            st.setState(2);
                            st.addSpawn(30732);
                            st.addSpawn(27129);
                            st.startQuestTimer("Wait2", 300000L);
                        } else {
                            if (!st.isRunningQuestTimer("Wait2")) {
                                st.startQuestTimer("Wait2", 300000L);
                            }
                            htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
                        }
                        break;
                    case 8:
                        if (GameObjectsStorage.getByNpcId(27130) == null) {
                            htmltext = "30668-02.htm";
                            st.addSpawn(27130);
                            st.startQuestTimer("Wait3", 300000L);
                        } else {
                            if (!st.isRunningQuestTimer("Wait3")) {
                                st.startQuestTimer("Wait3", 300000L);
                            }
                            htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
                        }
                        break;
                    case 9:
                        st.takeItems(2833, -1L);
                        st.giveItems(2827, 1L);
                        htmltext = "30668-03.htm";
                        st.setCond(10);
                        st.setState(2);
                        break;
                }
                break;
            case 30732:
                if (cond == 7) {
                    st.giveItems(2826, 1L);
                    htmltext = "30732-01.htm";
                    st.setCond(8);
                    st.setState(2);
                    NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
                    if (isQuest != null) {
                        isQuest.deleteMe();
                    }
                    isQuest = GameObjectsStorage.getByNpcId(30732);
                    if (isQuest != null) {
                        isQuest.deleteMe();
                    }
                    st.cancelQuestTimer("Wait2");
                }
                break;
            case 30669:
                if (cond == 11 || cond == 12) {
                    htmltext = "30669-01.htm";
                } else if (cond == 13) {
                    st.takeItems(2825, 1L);
                    st.giveItems(3037, 1L);
                    htmltext = "30669-04.htm";
                    st.setCond(14);
                    st.setState(2);
                }
                break;
            case 30670:
                if (cond == 14 || cond == 15) {
                    htmltext = "30670-01.htm";
                } else if (cond == 16) {
                    st.takeItems(2825, 1L);
                    st.giveItems(2828, 1L);
                    htmltext = "30670-04.htm";
                    st.setCond(17);
                    st.setState(2);
                }
                break;
            case 30667:
                if (cond == 17) {
                    st.takeItems(2825, -1L);
                    st.giveItems(2829, 1L);
                    htmltext = "30667-01.htm";
                    st.setCond(18);
                    st.setState(2);
                } else if (cond == 19) {
                    st.takeItems(2834, -1L);
                    st.takeItems(2835, -1L);
                    st.takeItems(2836, -1L);
                    st.takeItems(2837, -1L);
                    st.takeItems(2838, -1L);
                    st.takeItems(2829, -1L);
                    st.giveItems(2830, 1L);
                    htmltext = "30667-03.htm";
                    st.setCond(20);
                    st.setState(2);
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            if (cond == aDROPLIST_COND[0] && npcId == aDROPLIST_COND[2] && (aDROPLIST_COND[3] == 0 || st.getQuestItemsCount(aDROPLIST_COND[3]) > 0L)) {
                if (aDROPLIST_COND[5] == 0) {
                    st.rollAndGive(aDROPLIST_COND[4], aDROPLIST_COND[7], (double) aDROPLIST_COND[6]);
                } else if (st.rollAndGive(aDROPLIST_COND[4], aDROPLIST_COND[7], aDROPLIST_COND[7], aDROPLIST_COND[5], (double) aDROPLIST_COND[6]) && aDROPLIST_COND[1] != cond && aDROPLIST_COND[1] != 0) {
                    st.setCond(aDROPLIST_COND[1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 18 && st.getQuestItemsCount(2834) != 0L && st.getQuestItemsCount(2835) != 0L && st.getQuestItemsCount(2836) != 0L && st.getQuestItemsCount(2837) != 0L && st.getQuestItemsCount(2838) != 0L) {
            st.setCond(19);
            st.setState(2);
        } else if (npcId == 27099 && (cond == 1 || cond == 2)) {
            if (st.getQuestItemsCount(2831) < 6L) {
                st.giveItems(2831, 1L);
            } else if (GameObjectsStorage.getByNpcId(27128) == null) {
                st.takeItems(2831, -1L);
                st.setCond(2);
                st.setState(2);
                st.addSpawn(27128);
                st.startQuestTimer("Wait1", 300000L);
            } else if (!st.isRunningQuestTimer("Wait1")) {
                st.startQuestTimer("Wait1", 300000L);
            }
        } else if (npcId == 27128) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27128);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            if (cond == 2) {
                if (st.getQuestItemsCount(2832) == 0L) {
                    st.giveItems(2832, 1L);
                }
                st.setCond(3);
                st.setState(2);
                st.cancelQuestTimer("Wait1");
            }
        } else if (npcId == 27129) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.cancelQuestTimer("Wait2");
            if (cond == 6) {
                st.setCond(7);
                st.setState(2);
            }
        } else if (npcId == 27130) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27130);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.cancelQuestTimer("Wait3");
            if (cond == 8) {
                if (st.getQuestItemsCount(2833) == 0L) {
                    st.giveItems(2833, 1L);
                }
                st.setCond(9);
                st.setState(2);
            }
        } else if (npcId == 27131) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27131);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.cancelQuestTimer("Wait4");
            if (cond == 12) {
                st.setCond(13);
                st.setState(2);
            }
        } else if (npcId == 27132) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27132);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.cancelQuestTimer("Wait5");
            if (cond == 15) {
                st.setCond(16);
                st.setState(2);
            }
        }
        return null;
    }
}
