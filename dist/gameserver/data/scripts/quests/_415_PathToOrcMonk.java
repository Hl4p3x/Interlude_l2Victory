package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _415_PathToOrcMonk extends Quest {
    private static final int Urutu = 30587;
    private static final int Rosheek = 30590;
    private static final int Kasman = 30501;
    private static final int Toruku = 30591;
    private static final int Pomegranate = 1593;
    private static final int KashaBearClaw = 1600;
    private static final int KashaBladeSpiderTalon = 1601;
    private static final int ScarletSalamanderScale = 1602;
    private static final int LeatherPouch1st = 1594;
    private static final int LeatherPouchFull1st = 1597;
    private static final int LeatherPouch2st = 1595;
    private static final int LeatherPouchFull2st = 1598;
    private static final int LeatherPouch3st = 1596;
    private static final int LeatherPouchFull3st = 1599;
    private static final int LeatherPouch4st = 1607;
    private static final int LeatherPouchFull4st = 1608;
    private static final int FierySpiritScroll = 1603;
    private static final int RosheeksLetter = 1604;
    private static final int GantakisLetterOfRecommendation = 1605;
    private static final int Fig = 1606;
    private static final int VukuOrcTusk = 1609;
    private static final int RatmanFang = 1610;
    private static final int LangkLizardmanTooth = 1611;
    private static final int FelimLizardmanTooth = 1612;
    private static final int IronWillScroll = 1613;
    private static final int TorukusLetter = 1614;
    private static final int KhavatariTotem = 1615;
    private static final int KashaBear = 20479;
    private static final int KashaBladeSpider = 20478;
    private static final int ScarletSalamander = 20415;
    private static final int VukuOrcFighter = 20017;
    private static final int RatmanWarrior = 20359;
    private static final int LangkLizardmanWarrior = 20024;
    private static final int FelimLizardmanWarrior = 20014;
    private static final int[][] DROPLIST_COND = {{2, 3, 20479, 1594, 1600, 5, 70, 1}, {4, 5, 20478, 1595, 1601, 5, 70, 1}, {6, 7, 20415, 1596, 1602, 5, 70, 1}, {11, 0, 20017, 1607, 1609, 3, 70, 1}, {11, 0, 20359, 1607, 1610, 3, 70, 1}, {11, 0, 20024, 1607, 1611, 3, 70, 1}, {11, 0, 20014, 1607, 1612, 3, 70, 1}};

    public _415_PathToOrcMonk() {
        super(false);
        addStartNpc(30587);
        addTalkId(30590);
        addTalkId(30501);
        addTalkId(30591);
        for (int i = 0; i < _415_PathToOrcMonk.DROPLIST_COND.length; ++i) {
            addKillId(_415_PathToOrcMonk.DROPLIST_COND[i][2]);
            addQuestItem(_415_PathToOrcMonk.DROPLIST_COND[i][4]);
        }
        addQuestItem(1593, 1594, 1597, 1595, 1598, 1596, 1599, 1606, 1603, 1604, 1605, 1607, 1608, 1613, 1614);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("gantaki_zu_urutu_q0415_06.htm".equalsIgnoreCase(event)) {
            st.giveItems(1593, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30587:
                if (st.getQuestItemsCount(1615) != 0L) {
                    htmltext = "gantaki_zu_urutu_q0415_04.htm";
                    st.exitCurrentQuest(true);
                } else if (cond == 0) {
                    if (st.getPlayer().getClassId().getId() != 44) {
                        if (st.getPlayer().getClassId().getId() == 47) {
                            htmltext = "gantaki_zu_urutu_q0415_02a.htm";
                        } else {
                            htmltext = "gantaki_zu_urutu_q0415_02.htm";
                        }
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() < 18) {
                        htmltext = "gantaki_zu_urutu_q0415_03.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "gantaki_zu_urutu_q0415_01.htm";
                    }
                } else if (cond == 1) {
                    htmltext = "gantaki_zu_urutu_q0415_07.htm";
                } else if (cond >= 2 && cond <= 7) {
                    htmltext = "gantaki_zu_urutu_q0415_08.htm";
                } else if (cond == 8) {
                    st.takeItems(1604, 1L);
                    st.giveItems(1605, 1L);
                    htmltext = "gantaki_zu_urutu_q0415_09.htm";
                    st.setCond(9);
                    st.setState(2);
                } else if (cond == 9) {
                    htmltext = "gantaki_zu_urutu_q0415_10.htm";
                } else if (cond >= 10) {
                    htmltext = "gantaki_zu_urutu_q0415_11.htm";
                }
                break;
            case 30590:
                switch (cond) {
                    case 1:
                        st.takeItems(1593, -1L);
                        st.giveItems(1594, 1L);
                        htmltext = "khavatari_rosheek_q0415_01.htm";
                        st.setCond(2);
                        st.setState(2);
                        break;
                    case 2:
                        htmltext = "khavatari_rosheek_q0415_02.htm";
                        break;
                    case 3:
                        htmltext = "khavatari_rosheek_q0415_03.htm";
                        st.takeItems(1597, -1L);
                        st.giveItems(1595, 1L);
                        st.setCond(4);
                        st.setState(2);
                        break;
                    case 4:
                        htmltext = "khavatari_rosheek_q0415_04.htm";
                        break;
                    case 5:
                        st.takeItems(1598, -1L);
                        st.giveItems(1596, 1L);
                        htmltext = "khavatari_rosheek_q0415_05.htm";
                        st.setCond(6);
                        st.setState(2);
                        break;
                    case 6:
                        htmltext = "khavatari_rosheek_q0415_06.htm";
                        break;
                    case 7:
                        st.takeItems(1599, -1L);
                        st.giveItems(1603, 1L);
                        st.giveItems(1604, 1L);
                        htmltext = "khavatari_rosheek_q0415_07.htm";
                        st.setCond(8);
                        st.setState(2);
                        break;
                    case 8:
                        htmltext = "khavatari_rosheek_q0415_08.htm";
                        break;
                    case 9:
                        htmltext = "khavatari_rosheek_q0415_09.htm";
                        break;
                }
                break;
            case 30501:
                switch (cond) {
                    case 9:
                        st.takeItems(1605, -1L);
                        st.giveItems(1606, 1L);
                        htmltext = "prefect_kasman_q0415_01.htm";
                        st.setCond(10);
                        st.setState(2);
                        break;
                    case 10:
                        htmltext = "prefect_kasman_q0415_02.htm";
                        break;
                    case 11:
                    case 12:
                        htmltext = "prefect_kasman_q0415_03.htm";
                        break;
                    case 13:
                        st.takeItems(1603, -1L);
                        st.takeItems(1613, -1L);
                        st.takeItems(1614, -1L);
                        htmltext = "prefect_kasman_q0415_04.htm";
                        if (st.getPlayer().getClassId().getLevel() == 1) {
                            st.giveItems(1615, 1L);
                            if (!st.getPlayer().getVarB("prof1")) {
                                st.getPlayer().setVar("prof1", "1", -1L);
                                st.addExpAndSp(3200L, 3380L);
                            }
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(true);
                        break;
                }
                break;
            case 30591:
                switch (cond) {
                    case 10:
                        st.takeItems(1606, -1L);
                        st.giveItems(1607, 1L);
                        htmltext = "khavatari_toruku_q0415_01.htm";
                        st.setCond(11);
                        st.setState(2);
                        break;
                    case 11:
                        htmltext = "khavatari_toruku_q0415_02.htm";
                        break;
                    case 12:
                        st.takeItems(1608, -1L);
                        st.giveItems(1613, 1L);
                        st.giveItems(1614, 1L);
                        htmltext = "khavatari_toruku_q0415_03.htm";
                        st.setCond(13);
                        st.setState(2);
                        break;
                    case 13:
                        htmltext = "khavatari_toruku_q0415_04.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _415_PathToOrcMonk.DROPLIST_COND.length; ++i) {
            if (cond == _415_PathToOrcMonk.DROPLIST_COND[i][0] && npcId == _415_PathToOrcMonk.DROPLIST_COND[i][2] && (_415_PathToOrcMonk.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_415_PathToOrcMonk.DROPLIST_COND[i][3]) > 0L)) {
                if (_415_PathToOrcMonk.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_415_PathToOrcMonk.DROPLIST_COND[i][4], _415_PathToOrcMonk.DROPLIST_COND[i][7], (double) _415_PathToOrcMonk.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_415_PathToOrcMonk.DROPLIST_COND[i][4], _415_PathToOrcMonk.DROPLIST_COND[i][7], _415_PathToOrcMonk.DROPLIST_COND[i][7], _415_PathToOrcMonk.DROPLIST_COND[i][5], (double) _415_PathToOrcMonk.DROPLIST_COND[i][6]) && _415_PathToOrcMonk.DROPLIST_COND[i][1] != cond && _415_PathToOrcMonk.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_415_PathToOrcMonk.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 3 && st.getQuestItemsCount(1597) == 0L) {
            st.takeItems(1600, -1L);
            st.takeItems(1594, -1L);
            st.giveItems(1597, 1L);
        } else if (cond == 5 && st.getQuestItemsCount(1598) == 0L) {
            st.takeItems(1601, -1L);
            st.takeItems(1595, -1L);
            st.giveItems(1598, 1L);
        } else if (cond == 7 && st.getQuestItemsCount(1599) == 0L) {
            st.takeItems(1602, -1L);
            st.takeItems(1596, -1L);
            st.giveItems(1599, 1L);
        } else if (cond == 11 && st.getQuestItemsCount(1610) >= 3L && st.getQuestItemsCount(1611) >= 3L && st.getQuestItemsCount(1612) >= 3L && st.getQuestItemsCount(1609) >= 3L) {
            st.takeItems(1609, -1L);
            st.takeItems(1610, -1L);
            st.takeItems(1611, -1L);
            st.takeItems(1612, -1L);
            st.takeItems(1607, -1L);
            st.giveItems(1608, 1L);
            st.setCond(12);
            st.setState(2);
        }
        return null;
    }
}
