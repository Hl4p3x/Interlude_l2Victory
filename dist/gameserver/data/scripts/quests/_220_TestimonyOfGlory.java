package quests;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _220_TestimonyOfGlory extends Quest {
    private static final int Vokian = 30514;
    private static final int Chianta = 30642;
    private static final int Manakia = 30515;
    private static final int Kasman = 30501;
    private static final int Voltar = 30615;
    private static final int Kepra = 30616;
    private static final int Burai = 30617;
    private static final int Harak = 30618;
    private static final int Driko = 30619;
    private static final int Tanapi = 30571;
    private static final int Kakai = 30565;
    private static final int VokiansOrder = 3204;
    private static final int ManashenShard = 3205;
    private static final int TyrantTalon = 3206;
    private static final int GuardianBasiliskFang = 3207;
    private static final int VokiansOrder2 = 3208;
    private static final int NecklaceOfAuthority = 3209;
    private static final int ChiantaOrder1st = 3210;
    private static final int ScepterOfBreka = 3211;
    private static final int ScepterOfEnku = 3212;
    private static final int ScepterOfVuku = 3213;
    private static final int ScepterOfTurek = 3214;
    private static final int ScepterOfTunath = 3215;
    private static final int ChiantasOrder2rd = 3216;
    private static final int ChiantasOrder3rd = 3217;
    private static final int TamlinOrcSkull = 3218;
    private static final int TimakOrcHead = 3219;
    private static final int ScepterBox = 3220;
    private static final int PashikasHead = 3221;
    private static final int VultusHead = 3222;
    private static final int GloveOfVoltar = 3223;
    private static final int EnkuOverlordHead = 3224;
    private static final int GloveOfKepra = 3225;
    private static final int MakumBugbearHead = 3226;
    private static final int GloveOfBurai = 3227;
    private static final int ManakiaLetter1st = 3228;
    private static final int ManakiaLetter2st = 3229;
    private static final int KasmansLetter1rd = 3230;
    private static final int KasmansLetter2rd = 3231;
    private static final int KasmansLetter3rd = 3232;
    private static final int DrikosContract = 3233;
    private static final int StakatoDroneHusk = 3234;
    private static final int TanapisOrder = 3235;
    private static final int ScepterOfTantos = 3236;
    private static final int RitualBox = 3237;
    private static final int MarkOfGlory = 3203;
    private static final int Tyrant = 20192;
    private static final int TyrantKingpin = 20193;
    private static final int GuardianBasilisk = 20550;
    private static final int ManashenGargoyle = 20563;
    private static final int MarshStakatoDrone = 20234;
    private static final int PashikasSonOfVoltarQuestMonster = 27080;
    private static final int VultusSonOfVoltarQuestMonster = 27081;
    private static final int EnkuOrcOverlordQuestMonster = 27082;
    private static final int MakumBugbearThugQuestMonster = 27083;
    private static final int TimakOrc = 20583;
    private static final int TimakOrcArcher = 20584;
    private static final int TimakOrcSoldier = 20585;
    private static final int TimakOrcWarrior = 20586;
    private static final int TimakOrcShaman = 20587;
    private static final int TimakOrcOverlord = 20588;
    private static final int TamlinOrc = 20601;
    private static final int TamlinOrcArcher = 20602;
    private static final int RagnaOrcOverlord = 20778;
    private static final int RagnaOrcSeer = 20779;
    private static final int RevenantOfTantosChief = 27086;
    private static final int[][] DROPLIST_COND = {{1, 0, 20563, 3204, 3205, 10, 70, 1}, {1, 0, 20192, 3204, 3206, 10, 70, 1}, {1, 0, 20193, 3204, 3206, 10, 70, 1}, {1, 0, 20550, 3204, 3207, 10, 70, 1}, {4, 0, 20234, 3233, 3234, 30, 70, 1}, {4, 0, 27082, 3225, 3224, 4, 100, 1}, {4, 0, 27083, 3227, 3226, 2, 100, 1}, {6, 0, 20583, 3217, 3219, 20, 50, 1}, {6, 0, 20584, 3217, 3219, 20, 60, 1}, {6, 0, 20585, 3217, 3219, 20, 70, 1}, {6, 0, 20586, 3217, 3219, 20, 80, 1}, {6, 0, 20587, 3217, 3219, 20, 90, 1}, {6, 0, 20588, 3217, 3219, 20, 100, 1}, {6, 0, 20601, 3217, 3218, 20, 50, 1}, {6, 0, 20602, 3217, 3218, 20, 60, 1}};

    public _220_TestimonyOfGlory() {
        super(false);
        addStartNpc(30514);
        addTalkId(30642);
        addTalkId(30515);
        addTalkId(30501);
        addTalkId(30615);
        addTalkId(30616);
        addTalkId(30617);
        addTalkId(30618);
        addTalkId(30619);
        addTalkId(30571);
        addTalkId(30565);
        for (int i = 0; i < _220_TestimonyOfGlory.DROPLIST_COND.length; ++i) {
            addKillId(_220_TestimonyOfGlory.DROPLIST_COND[i][2]);
        }
        addKillId(27080);
        addKillId(27081);
        addKillId(20778);
        addKillId(20779);
        addKillId(27086);
        addQuestItem(3204, 3208, 3209, 3210, 3228, 3229, 3230, 3231, 3232, 3211, 3221, 3222, 3223, 3225, 3212, 3214, 3227, 3215, 3233, 3216, 3217, 3220, 3235, 3236, 3237, 3205, 3206, 3207, 3234, 3224, 3226, 3219, 3218);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("RETURN".equalsIgnoreCase(event)) {
            return null;
        }
        if ("30514-05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.giveItems(3204, 1L);
            if (!st.getPlayer().getVarB("dd2")) {
                st.giveItems(7562, 102L);
                st.getPlayer().setVar("dd2", "1", -1L);
            }
            st.playSound("ItemSound.quest_accept");
        } else if ("30642-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3208, -1L);
            st.giveItems(3210, 1L);
            st.setCond(4);
            st.setState(2);
        } else if ("30571-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3220, -1L);
            st.giveItems(3235, 1L);
            st.setCond(9);
            st.setState(2);
        } else if ("30642-07.htm".equalsIgnoreCase(event)) {
            st.takeItems(3211, -1L);
            st.takeItems(3212, -1L);
            st.takeItems(3213, -1L);
            st.takeItems(3214, -1L);
            st.takeItems(3215, -1L);
            st.takeItems(3210, -1L);
            if (st.getPlayer().getLevel() >= 38) {
                st.giveItems(3217, 1L);
                st.setCond(6);
                st.setState(2);
            } else {
                htmltext = "30642-06.htm";
                st.giveItems(3216, 1L);
            }
        } else if ("BREKA".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(3211) > 0L) {
                htmltext = "30515-02.htm";
            } else if (st.getQuestItemsCount(3228) > 0L) {
                htmltext = "30515-04.htm";
            } else {
                htmltext = "30515-03.htm";
                st.giveItems(3228, 1L);
            }
        } else if ("ENKU".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(3212) > 0L) {
                htmltext = "30515-05.htm";
            } else if (st.getQuestItemsCount(3229) > 0L) {
                htmltext = "30515-07.htm";
            } else {
                htmltext = "30515-06.htm";
                st.giveItems(3229, 1L);
            }
        } else if ("VUKU".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(3213) > 0L) {
                htmltext = "30501-02.htm";
            } else if (st.getQuestItemsCount(3230) > 0L) {
                htmltext = "30501-04.htm";
            } else {
                htmltext = "30501-03.htm";
                st.giveItems(3230, 1L);
            }
        } else if ("TUREK".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(3214) > 0L) {
                htmltext = "30501-05.htm";
            } else if (st.getQuestItemsCount(3231) > 0L) {
                htmltext = "30501-07.htm";
            } else {
                htmltext = "30501-06.htm";
                st.giveItems(3231, 1L);
            }
        } else if ("TUNATH".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(3215) > 0L) {
                htmltext = "30501-08.htm";
            } else if (st.getQuestItemsCount(3232) > 0L) {
                htmltext = "30501-10.htm";
            } else {
                htmltext = "30501-09.htm";
                st.giveItems(3232, 1L);
            }
        } else if ("30615-04.htm".equalsIgnoreCase(event)) {
            int spawn = 0;
            NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
            if (isQuest != null) {
                spawn = 1;
            }
            isQuest = GameObjectsStorage.getByNpcId(27081);
            if (isQuest != null) {
                spawn = 1;
            }
            if (spawn == 1) {
                if (!st.isRunningQuestTimer("Wait1")) {
                    st.startQuestTimer("Wait1", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            } else {
                st.takeItems(3228, -1L);
                st.giveItems(3223, 1L);
                st.cancelQuestTimer("Wait1");
                st.startQuestTimer("PashikasSonOfVoltarQuestMonster", 200000L);
                st.startQuestTimer("VultusSonOfVoltarQuestMonster", 200000L);
                st.addSpawn(27080);
                st.addSpawn(27081);
                st.playSound("Itemsound.quest_before_battle");
            }
        } else if ("30616-04.htm".equalsIgnoreCase(event)) {
            final NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27082);
            if (isQuest2 != null) {
                if (!st.isRunningQuestTimer("Wait2")) {
                    st.startQuestTimer("Wait2", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            } else {
                st.takeItems(3229, -1L);
                st.giveItems(3225, 1L);
                st.cancelQuestTimer("Wait2");
                st.startQuestTimer("EnkuOrcOverlordQuestMonster", 200000L);
                st.addSpawn(27082);
                st.addSpawn(27082);
                st.addSpawn(27082);
                st.addSpawn(27082);
                st.playSound("Itemsound.quest_before_battle");
            }
        } else if ("30617-04.htm".equalsIgnoreCase(event)) {
            final NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27083);
            if (isQuest2 != null) {
                if (!st.isRunningQuestTimer("Wait3")) {
                    st.startQuestTimer("Wait3", 300000L);
                }
                htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
            } else {
                st.takeItems(3231, -1L);
                st.giveItems(3227, 1L);
                st.cancelQuestTimer("Wait3");
                st.startQuestTimer("MakumBugbearThugQuestMonster", 200000L);
                st.addSpawn(27083);
                st.addSpawn(27083);
                st.playSound("Itemsound.quest_before_battle");
            }
        } else if ("30618-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3232, -1L);
            st.giveItems(3215, 1L);
            if (st.getQuestItemsCount(3211) != 0L && st.getQuestItemsCount(3212) != 0L && st.getQuestItemsCount(3213) != 0L && st.getQuestItemsCount(3214) != 0L && st.getQuestItemsCount(3215) != 0L) {
                st.setCond(5);
                st.setState(2);
            }
        } else if ("30619-03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3230, -1L);
            st.giveItems(3233, 1L);
        } else if ("Wait1".equalsIgnoreCase(event) || "PashikasSonOfVoltarQuestMonster".equalsIgnoreCase(event) || "VultusSonOfVoltarQuestMonster".equalsIgnoreCase(event)) {
            NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27080);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            isQuest2 = GameObjectsStorage.getByNpcId(27081);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            st.cancelQuestTimer("Wait1");
            st.cancelQuestTimer("PashikasSonOfVoltarQuestMonster");
        } else if ("Wait2".equalsIgnoreCase(event) || "EnkuOrcOverlordQuestMonster".equalsIgnoreCase(event)) {
            NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27082);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            isQuest2 = GameObjectsStorage.getByNpcId(27082);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            isQuest2 = GameObjectsStorage.getByNpcId(27082);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            isQuest2 = GameObjectsStorage.getByNpcId(27082);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            st.cancelQuestTimer("Wait2");
            st.cancelQuestTimer("EnkuOrcOverlordQuestMonster");
        } else if ("Wait3".equalsIgnoreCase(event) || "MakumBugbearThugQuestMonster".equalsIgnoreCase(event)) {
            NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27083);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            isQuest2 = GameObjectsStorage.getByNpcId(27083);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            st.cancelQuestTimer("Wait3");
            st.cancelQuestTimer("MakumBugbearThugQuestMonster");
        } else if ("Wait4".equalsIgnoreCase(event) || "RevenantOfTantosChief".equalsIgnoreCase(event)) {
            final NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27086);
            if (isQuest2 != null) {
                isQuest2.deleteMe();
            }
            st.cancelQuestTimer("Wait4");
            st.cancelQuestTimer("RevenantOfTantosChief");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30514:
                if (st.getQuestItemsCount(3203) != 0L) {
                    htmltext = "completed";
                    st.exitCurrentQuest(true);
                } else if (cond == 0) {
                    if (st.getPlayer().getClassId().getId() == 45 || st.getPlayer().getClassId().getId() == 47 || st.getPlayer().getClassId().getId() == 50) {
                        if (st.getPlayer().getLevel() >= 37) {
                            htmltext = "30514-03.htm";
                        } else {
                            htmltext = "30514-01.htm";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "30514-02.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1) {
                    htmltext = "30514-06.htm";
                } else if (cond == 2) {
                    st.takeItems(3204, -1L);
                    st.takeItems(3205, -10L);
                    st.takeItems(3206, -10L);
                    st.takeItems(3207, -10L);
                    st.giveItems(3208, 1L);
                    st.giveItems(3209, 1L);
                    htmltext = "30514-08.htm";
                    st.setCond(3);
                    st.setState(2);
                } else if (cond == 3) {
                    htmltext = "30514-09.htm";
                } else if (cond == 4) {
                    htmltext = "30514-10.htm";
                }
                break;
            case 30642:
                switch (cond) {
                    case 3:
                        htmltext = "30642-01.htm";
                        break;
                    case 4:
                        htmltext = "30642-04.htm";
                        break;
                    case 5:
                        if (st.getQuestItemsCount(3210) > 0L) {
                            htmltext = "30642-05.htm";
                        } else if (st.getQuestItemsCount(3216) > 0L) {
                            if (st.getPlayer().getLevel() >= 38) {
                                st.takeItems(3216, -1L);
                                st.giveItems(3217, 1L);
                                htmltext = "30642-09.htm";
                                st.setCond(6);
                                st.setState(2);
                            } else {
                                htmltext = "30642-08.htm";
                            }
                        }
                        break;
                    case 6:
                        htmltext = "30642-10.htm";
                        break;
                    case 7:
                        st.takeItems(3209, -1L);
                        st.takeItems(3217, -1L);
                        st.takeItems(3218, -1L);
                        st.takeItems(3219, -1L);
                        st.giveItems(3220, 1L);
                        htmltext = "30642-11.htm";
                        st.setCond(8);
                        st.setState(2);
                        break;
                    case 8:
                        htmltext = "30642-12.htm";
                        break;
                }
                break;
            case 30515:
                if (cond == 4) {
                    htmltext = "30515-01.htm";
                }
                break;
            case 30501:
                if (cond == 4) {
                    htmltext = "30501-01.htm";
                }
                break;
            case 30615:
                if (cond == 4) {
                    if (st.getQuestItemsCount(3228) > 0L) {
                        htmltext = "30615-02.htm";
                    } else if (st.getQuestItemsCount(3223) > 0L && (st.getQuestItemsCount(3221) == 0L || st.getQuestItemsCount(3222) == 0L)) {
                        htmltext = "30615-05.htm";
                        int sound = 0;
                        NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
                        if (isQuest == null) {
                            sound = 1;
                            st.addSpawn(27080);
                            st.startQuestTimer("PashikasSonOfVoltarQuestMonster", 200000L);
                        }
                        isQuest = GameObjectsStorage.getByNpcId(27081);
                        if (isQuest == null) {
                            sound = 1;
                            st.addSpawn(27081);
                            st.startQuestTimer("VultusSonOfVoltarQuestMonster", 200000L);
                        }
                        if (sound == 1) {
                            st.playSound("Itemsound.quest_before_battle");
                            st.cancelQuestTimer("Wait1");
                        } else {
                            st.startQuestTimer("Wait1", 300000L);
                            htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
                        }
                    } else if (st.getQuestItemsCount(3221) > 0L && st.getQuestItemsCount(3222) > 0L) {
                        st.takeItems(3221, -1L);
                        st.takeItems(3222, -1L);
                        st.takeItems(3223, -1L);
                        st.giveItems(3211, 1L);
                        htmltext = "30615-06.htm";
                        if (st.getQuestItemsCount(3211) > 0L && st.getQuestItemsCount(3212) > 0L && st.getQuestItemsCount(3213) > 0L && st.getQuestItemsCount(3214) > 0L && st.getQuestItemsCount(3215) > 0L) {
                            st.setCond(5);
                            st.setState(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(3211) > 0L) {
                        htmltext = "30615-07.htm";
                    } else {
                        htmltext = "30615-01.htm";
                    }
                }
                break;
            case 30616:
                if (cond == 4) {
                    if (st.getQuestItemsCount(3229) > 0L) {
                        htmltext = "30616-02.htm";
                    } else if (st.getQuestItemsCount(3225) > 0L && st.getQuestItemsCount(3224) < 4L) {
                        htmltext = "30616-05.htm";
                        final NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27082);
                        if (isQuest2 != null) {
                            st.startQuestTimer("Wait2", 300000L);
                            htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
                        } else {
                            st.cancelQuestTimer("Wait2");
                            st.startQuestTimer("EnkuOrcOverlordQuestMonster", 200000L);
                            st.addSpawn(27082);
                            st.addSpawn(27082);
                            st.addSpawn(27082);
                            st.addSpawn(27082);
                            st.playSound("Itemsound.quest_before_battle");
                        }
                    } else if (st.getQuestItemsCount(3224) >= 4L) {
                        htmltext = "30616-06.htm";
                        st.takeItems(3224, -1L);
                        st.takeItems(3225, -1L);
                        st.giveItems(3212, 1L);
                        if (st.getQuestItemsCount(3211) > 0L && st.getQuestItemsCount(3212) > 0L && st.getQuestItemsCount(3213) > 0L && st.getQuestItemsCount(3214) > 0L && st.getQuestItemsCount(3215) > 0L) {
                            st.setCond(5);
                            st.setState(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(3212) > 0L) {
                        htmltext = "30616-07.htm";
                    } else {
                        htmltext = "30616-01.htm";
                    }
                }
                break;
            case 30617:
                if (cond == 4) {
                    if (st.getQuestItemsCount(3231) > 0L) {
                        htmltext = "30617-02.htm";
                    } else if (st.getQuestItemsCount(3227) > 0L && st.getQuestItemsCount(3226) < 2L) {
                        htmltext = "30617-05.htm";
                        final NpcInstance isQuest2 = GameObjectsStorage.getByNpcId(27083);
                        if (isQuest2 != null) {
                            st.startQuestTimer("Wait3", 300000L);
                            htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
                        } else {
                            st.cancelQuestTimer("Wait3");
                            st.startQuestTimer("MakumBugbearThugQuestMonster", 200000L);
                            st.addSpawn(27083);
                            st.addSpawn(27083);
                            st.playSound("Itemsound.quest_before_battle");
                        }
                    } else if (st.getQuestItemsCount(3226) == 2L) {
                        htmltext = "30617-06.htm";
                        st.takeItems(3226, -1L);
                        st.takeItems(3227, -1L);
                        st.giveItems(3214, 1L);
                        if (st.getQuestItemsCount(3211) > 0L && st.getQuestItemsCount(3212) > 0L && st.getQuestItemsCount(3213) > 0L && st.getQuestItemsCount(3214) > 0L && st.getQuestItemsCount(3215) > 0L) {
                            st.setCond(5);
                            st.setState(2);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(3214) > 0L) {
                        htmltext = "30617-07.htm";
                    } else {
                        htmltext = "30617-01.htm";
                    }
                }
                break;
            case 30618:
                if (cond == 4) {
                    if (st.getQuestItemsCount(3232) > 0L) {
                        htmltext = "30618-02.htm";
                    } else if (st.getQuestItemsCount(3215) > 0L) {
                        htmltext = "30618-04.htm";
                    } else {
                        htmltext = "30618-01.htm";
                    }
                }
                break;
            case 30619:
                if (cond == 4) {
                    if (st.getQuestItemsCount(3230) > 0L) {
                        htmltext = "30619-02.htm";
                    } else if (st.getQuestItemsCount(3233) > 0L) {
                        if (st.getQuestItemsCount(3234) >= 30L) {
                            htmltext = "30619-05.htm";
                            st.takeItems(3234, -1L);
                            st.takeItems(3233, -1L);
                            st.giveItems(3213, 1L);
                            if (st.getQuestItemsCount(3211) > 0L && st.getQuestItemsCount(3212) > 0L && st.getQuestItemsCount(3213) > 0L && st.getQuestItemsCount(3214) > 0L && st.getQuestItemsCount(3215) > 0L) {
                                st.setCond(5);
                                st.setState(2);
                                st.playSound("ItemSound.quest_middle");
                            } else {
                                st.playSound("ItemSound.quest_itemget");
                            }
                        } else {
                            htmltext = "30619-04.htm";
                        }
                    } else if (st.getQuestItemsCount(3213) > 0L) {
                        htmltext = "30619-06.htm";
                    } else {
                        htmltext = "30619-01.htm";
                    }
                }
                break;
            case 30571:
                switch (cond) {
                    case 8:
                        htmltext = "30571-01.htm";
                        break;
                    case 9:
                        htmltext = "30571-04.htm";
                        break;
                    case 10:
                        st.takeItems(3236, -1L);
                        st.takeItems(3235, -1L);
                        st.giveItems(3237, 1L);
                        htmltext = "30571-05.htm";
                        st.setCond(11);
                        st.setState(2);
                        break;
                    case 11:
                        htmltext = "30571-06.htm";
                        break;
                }
                break;
        }
        if (npcId == 30565 && cond == 11) {
            st.takeItems(3237, -1L);
            st.giveItems(3203, 1L);
            if (!st.getPlayer().getVarB("prof2.2")) {
                st.addExpAndSp(91457L, 2500L);
                st.getPlayer().setVar("prof2.2", "1", -1L);
            }
            htmltext = "30565-02.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _220_TestimonyOfGlory.DROPLIST_COND.length; ++i) {
            if (cond == _220_TestimonyOfGlory.DROPLIST_COND[i][0] && npcId == _220_TestimonyOfGlory.DROPLIST_COND[i][2] && (_220_TestimonyOfGlory.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_220_TestimonyOfGlory.DROPLIST_COND[i][3]) > 0L)) {
                if (_220_TestimonyOfGlory.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_220_TestimonyOfGlory.DROPLIST_COND[i][4], _220_TestimonyOfGlory.DROPLIST_COND[i][7], (double) _220_TestimonyOfGlory.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_220_TestimonyOfGlory.DROPLIST_COND[i][4], _220_TestimonyOfGlory.DROPLIST_COND[i][7], _220_TestimonyOfGlory.DROPLIST_COND[i][7], _220_TestimonyOfGlory.DROPLIST_COND[i][5], (double) _220_TestimonyOfGlory.DROPLIST_COND[i][6]) && _220_TestimonyOfGlory.DROPLIST_COND[i][1] != cond && _220_TestimonyOfGlory.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_220_TestimonyOfGlory.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 1 && st.getQuestItemsCount(3206) >= 10L && st.getQuestItemsCount(3207) >= 10L && st.getQuestItemsCount(3205) >= 10L) {
            st.setCond(2);
            st.setState(2);
        } else if (cond == 4) {
            if (npcId == 27080) {
                st.cancelQuestTimer("PashikasSonOfVoltarQuestMonster");
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getQuestItemsCount(3223) > 0L && st.getQuestItemsCount(3221) == 0L) {
                    st.giveItems(3221, 1L);
                }
            } else if (npcId == 27081) {
                st.cancelQuestTimer("VultusSonOfVoltarQuestMonster");
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27081);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                if (st.getQuestItemsCount(3223) > 0L && st.getQuestItemsCount(3222) == 0L) {
                    st.giveItems(3222, 1L);
                }
            }
        } else if (cond == 6 && st.getQuestItemsCount(3219) >= 20L && st.getQuestItemsCount(3218) >= 20L) {
            st.setCond(7);
            st.setState(2);
        } else if (cond == 9) {
            if (npcId == 20778 || npcId == 20779) {
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27086);
                if (isQuest == null) {
                    st.startQuestTimer("RevenantOfTantosChief", 300000L);
                    st.addSpawn(27086);
                    st.playSound("Itemsound.quest_before_battle");
                } else if (!st.isRunningQuestTimer("Wait4")) {
                    st.startQuestTimer("Wait4", 300000L);
                }
            } else if (npcId == 27086) {
                st.cancelQuestTimer("RevenantOfTantosChief");
                st.cancelQuestTimer("Wait4");
                final NpcInstance isQuest = GameObjectsStorage.getByNpcId(27086);
                if (isQuest != null) {
                    isQuest.deleteMe();
                }
                st.giveItems(3236, 1L);
                st.setCond(10);
                st.setState(2);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }
}
