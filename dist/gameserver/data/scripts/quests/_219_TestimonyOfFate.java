package quests;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _219_TestimonyOfFate extends Quest {
    private static final int Kaira = 30476;
    private static final int Metheus = 30614;
    private static final int Ixia = 30463;
    private static final int AldersSpirit = 30613;
    private static final int Roa = 30114;
    private static final int Norman = 30210;
    private static final int Thifiell = 30358;
    private static final int Arkenia = 30419;
    private static final int BloodyPixy = 31845;
    private static final int BlightTreant = 31850;
    private static final int KairasLetter = 3173;
    private static final int MetheussFuneralJar = 3174;
    private static final int KasandrasRemains = 3175;
    private static final int HerbalismTextbook = 3176;
    private static final int IxiasList = 3177;
    private static final int MedusasIchor = 3178;
    private static final int MarshSpiderFluids = 3179;
    private static final int DeadSeekerDung = 3180;
    private static final int TyrantsBlood = 3181;
    private static final int NightshadeRoot = 3182;
    private static final int Belladonna = 3183;
    private static final int AldersSkull1 = 3184;
    private static final int AldersSkull2 = 3185;
    private static final int AldersReceipt = 3186;
    private static final int RevelationsManuscript = 3187;
    private static final int KairasRecommendation = 3189;
    private static final int KairasInstructions = 3188;
    private static final int PalusCharm = 3190;
    private static final int ThifiellsLetter = 3191;
    private static final int ArkeniasNote = 3192;
    private static final int PixyGarnet = 3193;
    private static final int BlightTreantSeed = 3199;
    private static final int GrandissSkull = 3194;
    private static final int KarulBugbearSkull = 3195;
    private static final int BrekaOverlordSkull = 3196;
    private static final int LetoOverlordSkull = 3197;
    private static final int BlackWillowLeaf = 3200;
    private static final int RedFairyDust = 3198;
    private static final int BlightTreantSap = 3201;
    private static final int ArkeniasLetter = 1246;
    private static final int MarkofFate = 3172;
    private static final int HangmanTree = 20144;
    private static final int Medusa = 20158;
    private static final int MarshSpider = 20233;
    private static final int DeadSeeker = 20202;
    private static final int Tyrant = 20192;
    private static final int TyrantKingpin = 20193;
    private static final int MarshStakatoWorker = 20230;
    private static final int MarshStakato = 20157;
    private static final int MarshStakatoSoldier = 20232;
    private static final int MarshStakatoDrone = 20234;
    private static final int Grandis = 20554;
    private static final int KarulBugbear = 20600;
    private static final int BrekaOrcOverlord = 20270;
    private static final int LetoLizardmanOverlord = 20582;
    private static final int BlackWillowLurker = 27079;
    private static final int[][] DROPLIST_COND = {{6, 0, 20158, 3177, 3178, 10, 100, 1}, {6, 0, 20233, 3177, 3179, 10, 100, 1}, {6, 0, 20202, 3177, 3180, 10, 100, 1}, {6, 0, 20192, 3177, 3181, 10, 100, 1}, {6, 0, 20193, 3177, 3181, 10, 100, 1}, {6, 0, 20230, 3177, 3182, 10, 100, 1}, {6, 0, 20157, 3177, 3182, 10, 100, 1}, {6, 0, 20232, 3177, 3182, 10, 100, 1}, {6, 0, 20234, 3177, 3182, 10, 100, 1}, {17, 0, 20554, 3193, 3194, 10, 100, 1}, {17, 0, 20600, 3193, 3195, 10, 100, 1}, {17, 0, 20270, 3193, 3196, 10, 100, 1}, {17, 0, 20582, 3193, 3197, 10, 100, 1}, {17, 0, 27079, 3199, 3200, 10, 100, 1}};

    public _219_TestimonyOfFate() {
        super(false);
        addStartNpc(30476);
        addTalkId(30614);
        addTalkId(30463);
        addTalkId(30613);
        addTalkId(30114);
        addTalkId(30210);
        addTalkId(30358);
        addTalkId(30419);
        addTalkId(31845);
        addTalkId(31850);
        for (int i = 0; i < _219_TestimonyOfFate.DROPLIST_COND.length; ++i) {
            addKillId(_219_TestimonyOfFate.DROPLIST_COND[i][2]);
        }
        addKillId(20144);
        addQuestItem(3173, 3174, 3175, 3177, 3183, 3184, 3185, 3186, 3187, 3189, 3188, 3191, 3190, 3192, 3193, 3199, 3198, 3201, 1246, 3178, 3179, 3180, 3181, 3182, 3194, 3195, 3196, 3197, 3200);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30476-05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3173, 1L);
            if (!st.getPlayer().getVarB("dd2")) {
                st.giveItems(7562, 72L);
                st.getPlayer().setVar("dd2", "1", -1L);
            }
        } else if ("30114-04.htm".equalsIgnoreCase(event)) {
            st.takeItems(3185, 1L);
            st.giveItems(3186, 1L);
            st.setCond(12);
            st.setState(2);
        } else if ("30476-12.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 38) {
                st.takeItems(3187, -1L);
                st.giveItems(3189, 1L);
                st.setCond(15);
                st.setState(2);
            } else {
                htmltext = "30476-13.htm";
                st.takeItems(3187, -1L);
                st.giveItems(3188, 1L);
                st.setCond(14);
                st.setState(2);
            }
        } else if ("30419-02.htm".equalsIgnoreCase(event)) {
            st.takeItems(3191, -1L);
            st.giveItems(3192, 1L);
            st.setCond(17);
            st.setState(2);
        } else if ("31845-02.htm".equalsIgnoreCase(event)) {
            st.giveItems(3193, 1L);
        } else if ("31850-02.htm".equalsIgnoreCase(event)) {
            st.giveItems(3199, 1L);
        } else if ("30419-05.htm".equalsIgnoreCase(event)) {
            st.takeItems(3192, -1L);
            st.takeItems(3198, -1L);
            st.takeItems(3201, -1L);
            st.giveItems(1246, 1L);
            st.setCond(18);
            st.setState(2);
        }
        if ("AldersSpirit_Fail".equalsIgnoreCase(event)) {
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(30613);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
            st.setCond(9);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30476) {
            if (st.getQuestItemsCount(3172) != 0L) {
                htmltext = "completed";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                if (st.getPlayer().getRace() == Race.darkelf && st.getPlayer().getLevel() >= 37) {
                    htmltext = "30476-03.htm";
                } else if (st.getPlayer().getRace() == Race.darkelf) {
                    htmltext = "30476-02.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "30476-01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 2) {
                htmltext = "30476-06.htm";
            } else if (cond == 9 || cond == 10) {
                final NpcInstance AldersSpiritObject = GameObjectsStorage.getByNpcId(30613);
                if (AldersSpiritObject == null) {
                    st.takeItems(3184, -1L);
                    if (st.getQuestItemsCount(3185) == 0L) {
                        st.giveItems(3185, 1L);
                    }
                    htmltext = "30476-09.htm";
                    st.setCond(10);
                    st.setState(2);
                    st.addSpawn(30613);
                    st.startQuestTimer("AldersSpirit_Fail", 300000L);
                } else {
                    htmltext = "<html><head><body>I am borrowed, approach in some minutes</body></html>";
                }
            } else if (cond == 13) {
                htmltext = "30476-11.htm";
            } else if (cond == 14) {
                if (st.getQuestItemsCount(3188) != 0L && st.getPlayer().getLevel() < 38) {
                    htmltext = "30476-14.htm";
                } else if (st.getQuestItemsCount(3188) != 0L && st.getPlayer().getLevel() >= 38) {
                    st.giveItems(3189, 1L);
                    st.takeItems(3188, 1L);
                    htmltext = "30476-15.htm";
                    st.setCond(15);
                    st.setState(2);
                }
            } else if (cond == 15) {
                htmltext = "30476-16.htm";
            } else if (cond == 16 || cond == 17) {
                htmltext = "30476-17.htm";
            } else if (st.getQuestItemsCount(3174) > 0L || st.getQuestItemsCount(3175) > 0L) {
                htmltext = "30476-07.htm";
            } else if (st.getQuestItemsCount(3176) > 0L || st.getQuestItemsCount(3177) > 0L) {
                htmltext = "30476-08.htm";
            } else if (st.getQuestItemsCount(3185) > 0L || st.getQuestItemsCount(3186) > 0L) {
                htmltext = "30476-10.htm";
            }
        } else if (npcId == 30614) {
            if (cond == 1) {
                htmltext = "30614-01.htm";
                st.takeItems(3173, -1L);
                st.giveItems(3174, 1L);
                st.setCond(2);
                st.setState(2);
            } else if (cond == 2) {
                htmltext = "30614-02.htm";
            } else if (cond == 3) {
                st.takeItems(3175, -1L);
                st.giveItems(3176, 1L);
                htmltext = "30614-03.htm";
                st.setCond(5);
                st.setState(2);
            } else if (cond == 8) {
                st.takeItems(3183, -1L);
                st.giveItems(3184, 1L);
                htmltext = "30614-05.htm";
                st.setCond(9);
                st.setState(2);
            } else if (st.getQuestItemsCount(3176) > 0L || st.getQuestItemsCount(3177) > 0L) {
                htmltext = "30614-04.htm";
            } else if (st.getQuestItemsCount(3184) > 0L || st.getQuestItemsCount(3185) > 0L || st.getQuestItemsCount(3186) > 0L || st.getQuestItemsCount(3187) > 0L || st.getQuestItemsCount(3188) > 0L || st.getQuestItemsCount(3189) > 0L) {
                htmltext = "30614-06.htm";
            }
        } else if (npcId == 30463) {
            if (cond == 5) {
                st.takeItems(3176, -1L);
                st.giveItems(3177, 1L);
                htmltext = "30463-01.htm";
                st.setCond(6);
                st.setState(2);
            } else if (cond == 6) {
                htmltext = "30463-02.htm";
            } else if (cond == 7 && st.getQuestItemsCount(3178) >= 10L && st.getQuestItemsCount(3179) >= 10L && st.getQuestItemsCount(3180) >= 10L && st.getQuestItemsCount(3181) >= 10L && st.getQuestItemsCount(3182) >= 10L) {
                st.takeItems(3178, -1L);
                st.takeItems(3179, -1L);
                st.takeItems(3180, -1L);
                st.takeItems(3181, -1L);
                st.takeItems(3182, -1L);
                st.takeItems(3177, -1L);
                st.giveItems(3183, 1L);
                htmltext = "30463-03.htm";
                st.setCond(8);
                st.setState(2);
            } else if (cond == 7) {
                htmltext = "30463-02.htm";
                st.setCond(6);
            } else if (cond == 8) {
                htmltext = "30463-04.htm";
            } else if (st.getQuestItemsCount(3184) > 0L || st.getQuestItemsCount(3185) > 0L || st.getQuestItemsCount(3186) > 0L || st.getQuestItemsCount(3187) > 0L || st.getQuestItemsCount(3188) > 0L || st.getQuestItemsCount(3189) > 0L) {
                htmltext = "30463-05.htm";
            }
        } else if (npcId == 30613) {
            htmltext = "30613-02.htm";
            st.setCond(11);
            st.setState(2);
            st.cancelQuestTimer("AldersSpirit_Fail");
            final NpcInstance isQuest = GameObjectsStorage.getByNpcId(30613);
            if (isQuest != null) {
                isQuest.deleteMe();
            }
        } else if (npcId == 30114) {
            if (cond == 11) {
                htmltext = "30114-01.htm";
            } else if (cond == 12) {
                htmltext = "30114-05.htm";
            } else if (st.getQuestItemsCount(3187) > 0L || st.getQuestItemsCount(3188) > 0L || st.getQuestItemsCount(3189) > 0L) {
                htmltext = "30114-06.htm";
            }
        } else if (npcId == 30210) {
            if (cond == 12) {
                st.takeItems(3186, -1L);
                st.giveItems(3187, 1L);
                htmltext = "30210-01.htm";
                st.setCond(13);
                st.setState(2);
            } else if (cond == 13) {
                htmltext = "30210-02.htm";
            }
        } else if (npcId == 30358) {
            switch (cond) {
                case 15:
                    st.takeItems(3189, -1L);
                    st.giveItems(3191, 1L);
                    st.giveItems(3190, 1L);
                    htmltext = "30358-01.htm";
                    st.setCond(16);
                    st.setState(2);
                    break;
                case 16:
                    htmltext = "30358-02.htm";
                    break;
                case 17:
                    htmltext = "30358-03.htm";
                    break;
                case 18:
                    if (!st.getPlayer().getVarB("prof2.2")) {
                        st.addExpAndSp(68183L, 1750L);
                        st.getPlayer().setVar("prof2.2", "1", -1L);
                    }
                    st.takeItems(1246, -1L);
                    st.takeItems(3190, -1L);
                    st.giveItems(3172, 1L);
                    htmltext = "30358-04.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    break;
            }
        } else if (npcId == 30419) {
            switch (cond) {
                case 16:
                    htmltext = "30419-01.htm";
                    break;
                case 17:
                    if (st.getQuestItemsCount(3198) < 1L || st.getQuestItemsCount(3201) < 1L) {
                        htmltext = "30419-03.htm";
                    } else if (st.getQuestItemsCount(3198) >= 1L && st.getQuestItemsCount(3201) >= 1L) {
                        htmltext = "30419-04.htm";
                    }
                    break;
                case 18:
                    htmltext = "30419-06.htm";
                    break;
            }
        } else if (npcId == 31845 && cond == 17) {
            if (st.getQuestItemsCount(3198) == 0L && st.getQuestItemsCount(3193) == 0L) {
                htmltext = "31845-01.htm";
            } else if (st.getQuestItemsCount(3198) == 0L && st.getQuestItemsCount(3193) > 0L && (st.getQuestItemsCount(3194) < 10L || st.getQuestItemsCount(3195) < 10L || st.getQuestItemsCount(3196) < 10L || st.getQuestItemsCount(3197) < 10L)) {
                htmltext = "31845-03.htm";
            } else if (st.getQuestItemsCount(3198) == 0L && st.getQuestItemsCount(3193) > 0L && st.getQuestItemsCount(3194) >= 10L && st.getQuestItemsCount(3195) >= 10L && st.getQuestItemsCount(3196) >= 10L && st.getQuestItemsCount(3197) >= 10L) {
                st.takeItems(3194, -1L);
                st.takeItems(3195, -1L);
                st.takeItems(3196, -1L);
                st.takeItems(3197, -1L);
                st.takeItems(3193, -1L);
                st.giveItems(3198, 1L);
                htmltext = "31845-04.htm";
            } else if (st.getQuestItemsCount(3198) != 0L) {
                htmltext = "31845-05.htm";
            }
        } else if (npcId == 31850 && cond == 17) {
            if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) == 0L) {
                htmltext = "31850-01.htm";
            } else if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) > 0L && st.getQuestItemsCount(3200) == 0L) {
                htmltext = "31850-03.htm";
            } else if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) > 0L && st.getQuestItemsCount(3200) > 0L) {
                st.takeItems(3200, -1L);
                st.takeItems(3199, -1L);
                st.giveItems(3201, 1L);
                htmltext = "31850-04.htm";
            } else if (st.getQuestItemsCount(3201) > 0L) {
                htmltext = "31850-05.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _219_TestimonyOfFate.DROPLIST_COND.length; ++i) {
            if (cond == _219_TestimonyOfFate.DROPLIST_COND[i][0] && npcId == _219_TestimonyOfFate.DROPLIST_COND[i][2] && (_219_TestimonyOfFate.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_219_TestimonyOfFate.DROPLIST_COND[i][3]) > 0L)) {
                if (_219_TestimonyOfFate.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_219_TestimonyOfFate.DROPLIST_COND[i][4], _219_TestimonyOfFate.DROPLIST_COND[i][7], (double) _219_TestimonyOfFate.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_219_TestimonyOfFate.DROPLIST_COND[i][4], _219_TestimonyOfFate.DROPLIST_COND[i][7], _219_TestimonyOfFate.DROPLIST_COND[i][7], _219_TestimonyOfFate.DROPLIST_COND[i][5], (double) _219_TestimonyOfFate.DROPLIST_COND[i][6]) && _219_TestimonyOfFate.DROPLIST_COND[i][1] != cond && _219_TestimonyOfFate.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_219_TestimonyOfFate.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 2 && npcId == 20144) {
            st.takeItems(3174, -1L);
            st.giveItems(3175, 1L);
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
            st.setState(2);
        } else if (cond == 6 && st.getQuestItemsCount(3178) >= 10L && st.getQuestItemsCount(3179) >= 10L && st.getQuestItemsCount(3180) >= 10L && st.getQuestItemsCount(3181) >= 10L && st.getQuestItemsCount(3182) >= 10L) {
            st.setCond(7);
            st.setState(2);
        }
        return null;
    }
}
