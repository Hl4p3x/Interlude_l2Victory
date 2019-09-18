package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class _171_ActsOfEvil extends Quest {
    private static final int Alvah = 30381;
    private static final int Tyra = 30420;
    private static final int Arodin = 30207;
    private static final int Rolento = 30437;
    private static final int Neti = 30425;
    private static final int Burai = 30617;
    private static final int BladeMold = 4239;
    private static final int OlMahumCaptainHead = 4249;
    private static final int TyrasBill = 4240;
    private static final int RangerReportPart1 = 4241;
    private static final int RangerReportPart2 = 4242;
    private static final int RangerReportPart3 = 4243;
    private static final int RangerReportPart4 = 4244;
    private static final int WeaponsTradeContract = 4245;
    private static final int AttackDirectives = 4246;
    private static final int CertificateOfTheSilverScaleGuild = 4247;
    private static final int RolentoCargobox = 4248;
    private static final int TurekOrcArcher = 20496;
    private static final int TurekOrcSkirmisher = 20497;
    private static final int TurekOrcSupplier = 20498;
    private static final int TurekOrcFootman = 20499;
    private static final int TumranBugbear = 20062;
    private static final int OlMahumGeneral = 20438;
    private static final int OlMahumCaptain = 20066;
    private static final int OlMahumSupportTroop = 27190;
    private static final int[][] DROPLIST_COND = {{2, 0, 20496, 0, 4239, 20, 50, 1}, {2, 0, 20497, 0, 4239, 20, 50, 1}, {2, 0, 20498, 0, 4239, 20, 50, 1}, {2, 0, 20499, 0, 4239, 20, 50, 1}, {10, 0, 20438, 0, 4249, 30, 100, 1}, {10, 0, 20066, 0, 4249, 30, 100, 1}};
    private static final int CHANCE2 = 100;
    private static final int CHANCE21 = 20;
    private static final int CHANCE22 = 20;
    private static final int CHANCE23 = 20;
    private static final int CHANCE24 = 10;
    private static final int CHANCE25 = 10;

    public NpcInstance OlMahumSupportTroop_Spawn;

    public _171_ActsOfEvil() {
        super(false);
        addStartNpc(30381);
        addTalkId(30207);
        addTalkId(30420);
        addTalkId(30437);
        addTalkId(30425);
        addTalkId(30617);
        addKillId(20062);
        addKillId(20438);
        addKillId(27190);
        addQuestItem(4248, 4240, 4247, 4241, 4242, 4243, 4244, 4245, 4246, 4239, 4249);
        for (int i = 0; i < _171_ActsOfEvil.DROPLIST_COND.length; ++i) {
            addKillId(_171_ActsOfEvil.DROPLIST_COND[i][2]);
        }
    }

    

    private void Despawn_OlMahumSupportTroop() {
        if (OlMahumSupportTroop_Spawn != null) {
            OlMahumSupportTroop_Spawn.deleteMe();
        }
        OlMahumSupportTroop_Spawn = null;
    }

    private void Spawn_OlMahumSupportTroop(final QuestState st) {
        OlMahumSupportTroop_Spawn = Functions.spawn(Location.findPointToStay(st.getPlayer(), 50, 100), 27190);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        if ("30381-02.htm".equals(event) && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30207-02.htm".equals(event) && cond == 1) {
            st.setCond(2);
            st.setState(2);
        } else if ("30381-04.htm".equals(event) && cond == 4) {
            st.setCond(5);
            st.setState(2);
        } else if ("30381-07.htm".equals(event) && cond == 6) {
            st.setCond(7);
            st.setState(2);
            st.takeItems(4245, -1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30437-03.htm".equals(event) && cond == 8) {
            st.giveItems(4248, 1L);
            st.giveItems(4247, 1L);
            st.setCond(9);
            st.setState(2);
        } else if ("30617-04.htm".equals(event) && cond == 9) {
            st.takeItems(4247, -1L);
            st.takeItems(4246, -1L);
            st.takeItems(4248, -1L);
            st.setCond(10);
            st.setState(2);
        } else if ("Wait1".equals(event)) {
            Despawn_OlMahumSupportTroop();
            st.cancelQuestTimer("Wait1");
            return null;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30381:
                switch (cond) {
                    case 0:
                        if (st.getPlayer().getLevel() <= 26) {
                            htmltext = "30381-01a.htm";
                            st.exitCurrentQuest(true);
                        } else {
                            htmltext = "30381-01.htm";
                        }
                        break;
                    case 1:
                        htmltext = "30381-02a.htm";
                        break;
                    case 4:
                        htmltext = "30381-03.htm";
                        break;
                    case 5:
                        if (st.getQuestItemsCount(4241) > 0L && st.getQuestItemsCount(4242) > 0L && st.getQuestItemsCount(4243) > 0L && st.getQuestItemsCount(4244) > 0L) {
                            htmltext = "30381-05.htm";
                            st.takeItems(4241, -1L);
                            st.takeItems(4242, -1L);
                            st.takeItems(4243, -1L);
                            st.takeItems(4244, -1L);
                            st.setCond(6);
                            st.setState(2);
                        } else {
                            htmltext = "30381-04a.htm";
                        }
                        break;
                    case 6:
                        if (st.getQuestItemsCount(4245) > 0L && st.getQuestItemsCount(4246) > 0L) {
                            htmltext = "30381-06.htm";
                        } else {
                            htmltext = "30381-05a.htm";
                        }
                        break;
                    case 7:
                        htmltext = "30381-07a.htm";
                        break;
                    case 11:
                        htmltext = "30381-08.htm";
                        st.giveItems(57, 90000L);
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(false);
                        break;
                }
                break;
            case 30207:
                switch (cond) {
                    case 1:
                        htmltext = "30207-01.htm";
                        break;
                    case 2:
                        htmltext = "30207-01a.htm";
                        break;
                    case 3:
                        if (st.getQuestItemsCount(4240) > 0L) {
                            st.takeItems(4240, -1L);
                            htmltext = "30207-03.htm";
                            st.setCond(4);
                            st.setState(2);
                        } else {
                            htmltext = "30207-01a.htm";
                        }
                        break;
                    case 4:
                        htmltext = "30207-03a.htm";
                        break;
                }
                break;
            case 30420:
                if (cond == 2) {
                    if (st.getQuestItemsCount(4239) >= 20L) {
                        st.takeItems(4239, -1L);
                        st.giveItems(4240, 1L);
                        htmltext = "30420-01.htm";
                        st.setCond(3);
                        st.setState(2);
                    } else {
                        htmltext = "30420-01b.htm";
                    }
                } else if (cond == 3) {
                    htmltext = "30420-01a.htm";
                } else if (cond > 3) {
                    htmltext = "30420-02.htm";
                }
                break;
            case 30425:
                if (cond == 7) {
                    htmltext = "30425-01.htm";
                    st.setCond(8);
                    st.setState(2);
                } else if (cond == 8) {
                    htmltext = "30425-02.htm";
                }
                break;
            case 30437:
                if (cond == 8) {
                    htmltext = "30437-01.htm";
                } else if (cond == 9) {
                    htmltext = "30437-03a.htm";
                }
                break;
            case 30617:
                if (cond == 9 && st.getQuestItemsCount(4247) > 0L && st.getQuestItemsCount(4248) > 0L && st.getQuestItemsCount(4246) > 0L) {
                    htmltext = "30617-01.htm";
                }
                if (cond == 10) {
                    if (st.getQuestItemsCount(4249) >= 30L) {
                        htmltext = "30617-05.htm";
                        st.giveItems(57, 8000L);
                        st.takeItems(4249, -1L);
                        st.setCond(11);
                        st.setState(2);
                        st.playSound("ItemSound.quest_itemget");
                    } else {
                        htmltext = "30617-04a.htm";
                    }
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _171_ActsOfEvil.DROPLIST_COND.length; ++i) {
            if (cond == _171_ActsOfEvil.DROPLIST_COND[i][0] && npcId == _171_ActsOfEvil.DROPLIST_COND[i][2] && (_171_ActsOfEvil.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_171_ActsOfEvil.DROPLIST_COND[i][3]) > 0L)) {
                if (_171_ActsOfEvil.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_171_ActsOfEvil.DROPLIST_COND[i][4], _171_ActsOfEvil.DROPLIST_COND[i][7], (double) _171_ActsOfEvil.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_171_ActsOfEvil.DROPLIST_COND[i][4], _171_ActsOfEvil.DROPLIST_COND[i][7], _171_ActsOfEvil.DROPLIST_COND[i][7], _171_ActsOfEvil.DROPLIST_COND[i][5], (double) _171_ActsOfEvil.DROPLIST_COND[i][6]) && _171_ActsOfEvil.DROPLIST_COND[i][1] != cond && _171_ActsOfEvil.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_171_ActsOfEvil.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (npcId == 27190) {
            Despawn_OlMahumSupportTroop();
        } else if (cond == 2 && Rnd.chance(10)) {
            if (OlMahumSupportTroop_Spawn == null) {
                Spawn_OlMahumSupportTroop(st);
            } else if (!st.isRunningQuestTimer("Wait1")) {
                st.startQuestTimer("Wait1", 300000L);
            }
        } else if (cond == 5 && npcId == 20062) {
            if (st.getQuestItemsCount(4241) == 0L && Rnd.chance(100)) {
                st.giveItems(4241, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(4242) == 0L && Rnd.chance(20)) {
                st.giveItems(4242, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(4243) == 0L && Rnd.chance(20)) {
                st.giveItems(4243, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(4244) == 0L && Rnd.chance(20)) {
                st.giveItems(4244, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (cond == 6 && npcId == 20438) {
            if (st.getQuestItemsCount(4245) == 0L && Rnd.chance(10)) {
                st.giveItems(4245, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(4246) == 0L && Rnd.chance(10)) {
                st.giveItems(4246, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
