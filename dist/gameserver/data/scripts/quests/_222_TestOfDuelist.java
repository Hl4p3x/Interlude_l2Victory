package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _222_TestOfDuelist extends Quest {
    private static final int Kaien = 30623;
    private static final int OrderGludio = 2763;
    private static final int OrderDion = 2764;
    private static final int OrderGiran = 2765;
    private static final int OrderOren = 2766;
    private static final int OrderAden = 2767;
    private static final int PunchersShard = 2768;
    private static final int NobleAntsFeeler = 2769;
    private static final int DronesChitin = 2770;
    private static final int DeadSeekerFang = 2771;
    private static final int OverlordNecklace = 2772;
    private static final int FetteredSoulsChain = 2773;
    private static final int ChiefsAmulet = 2774;
    private static final int EnchantedEyeMeat = 2775;
    private static final int TamrinOrcsRing = 2776;
    private static final int TamrinOrcsArrow = 2777;
    private static final int FinalOrder = 2778;
    private static final int ExcurosSkin = 2779;
    private static final int KratorsShard = 2780;
    private static final int GrandisSkin = 2781;
    private static final int TimakOrcsBelt = 2782;
    private static final int LakinsMace = 2783;
    private static final int MarkOfDuelist = 2762;
    private static final int Puncher = 20085;
    private static final int NobleAntLeader = 20090;
    private static final int MarshStakatoDrone = 20234;
    private static final int DeadSeeker = 20202;
    private static final int BrekaOrcOverlord = 20270;
    private static final int FetteredSoul = 20552;
    private static final int LetoLizardmanOverlord = 20582;
    private static final int EnchantedMonstereye = 20564;
    private static final int TamlinOrc = 20601;
    private static final int TamlinOrcArcher = 20602;
    private static final int Excuro = 20214;
    private static final int Krator = 20217;
    private static final int Grandis = 20554;
    private static final int TimakOrcOverlord = 20588;
    private static final int Lakin = 20604;
    private static final int[][] DROPLIST_COND = {{2, 0, 20085, 0, 2768, 10, 70, 1}, {2, 0, 20090, 0, 2769, 10, 70, 1}, {2, 0, 20234, 0, 2770, 10, 70, 1}, {2, 0, 20202, 0, 2771, 10, 70, 1}, {2, 0, 20270, 0, 2772, 10, 70, 1}, {2, 0, 20552, 0, 2773, 10, 70, 1}, {2, 0, 20582, 0, 2774, 10, 70, 1}, {2, 0, 20564, 0, 2775, 10, 70, 1}, {2, 0, 20601, 0, 2776, 10, 70, 1}, {2, 0, 20602, 0, 2777, 10, 70, 1}, {4, 0, 20214, 0, 2779, 3, 70, 1}, {4, 0, 20217, 0, 2780, 3, 70, 1}, {4, 0, 20554, 0, 2781, 3, 70, 1}, {4, 0, 20588, 0, 2782, 3, 70, 1}, {4, 0, 20604, 0, 2783, 3, 70, 1}};

    public _222_TestOfDuelist() {
        super(false);
        addStartNpc(30623);
        for (int i = 0; i < _222_TestOfDuelist.DROPLIST_COND.length; ++i) {
            addKillId(_222_TestOfDuelist.DROPLIST_COND[i][2]);
            addQuestItem(_222_TestOfDuelist.DROPLIST_COND[i][4]);
        }
        addQuestItem(2763, 2764, 2765, 2766, 2767, 2778);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30623-04.htm".equalsIgnoreCase(event) && st.getPlayer().getRace() == Race.orc) {
            htmltext = "30623-05.htm";
        } else if ("30623-07.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.setState(2);
            st.giveItems(2763, 1L);
            st.giveItems(2764, 1L);
            st.giveItems(2765, 1L);
            st.giveItems(2766, 1L);
            st.giveItems(2767, 1L);
            if (!st.getPlayer().getVarB("dd3")) {
                st.giveItems(7562, 72L, false);
                st.getPlayer().setVar("dd3", "1", -1L);
            }
            st.playSound("ItemSound.quest_accept");
        } else if ("30623-16.htm".equalsIgnoreCase(event)) {
            st.takeItems(2768, -1L);
            st.takeItems(2769, -1L);
            st.takeItems(2770, -1L);
            st.takeItems(2771, -1L);
            st.takeItems(2772, -1L);
            st.takeItems(2773, -1L);
            st.takeItems(2774, -1L);
            st.takeItems(2775, -1L);
            st.takeItems(2776, -1L);
            st.takeItems(2777, -1L);
            st.takeItems(2763, -1L);
            st.takeItems(2764, -1L);
            st.takeItems(2765, -1L);
            st.takeItems(2766, -1L);
            st.takeItems(2767, -1L);
            st.giveItems(2778, 1L);
            st.setCond(4);
            st.setState(2);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30623) {
            if (st.getQuestItemsCount(2762) != 0L) {
                htmltext = "completed";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                if (st.getPlayer().getClassId().getId() == 1 || st.getPlayer().getClassId().getId() == 47 || st.getPlayer().getClassId().getId() == 19 || st.getPlayer().getClassId().getId() == 32) {
                    if (st.getPlayer().getLevel() >= 39) {
                        htmltext = "30623-03.htm";
                    } else {
                        htmltext = "30623-01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "30623-02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 2) {
                htmltext = "30623-14.htm";
            } else if (cond == 3) {
                htmltext = "30623-13.htm";
            } else if (cond == 4) {
                htmltext = "30623-17.htm";
            } else if (cond == 5) {
                st.giveItems(2762, 1L);
                if (!st.getPlayer().getVarB("prof2.3")) {
                    st.addExpAndSp(91457L, 2500L);
                    st.getPlayer().setVar("prof2.3", "1", -1L);
                }
                htmltext = "30623-18.htm";
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _222_TestOfDuelist.DROPLIST_COND.length; ++i) {
            if (cond == _222_TestOfDuelist.DROPLIST_COND[i][0] && npcId == _222_TestOfDuelist.DROPLIST_COND[i][2] && (_222_TestOfDuelist.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_222_TestOfDuelist.DROPLIST_COND[i][3]) > 0L)) {
                if (_222_TestOfDuelist.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_222_TestOfDuelist.DROPLIST_COND[i][4], _222_TestOfDuelist.DROPLIST_COND[i][7], (double) _222_TestOfDuelist.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_222_TestOfDuelist.DROPLIST_COND[i][4], _222_TestOfDuelist.DROPLIST_COND[i][7], _222_TestOfDuelist.DROPLIST_COND[i][7], _222_TestOfDuelist.DROPLIST_COND[i][5], (double) _222_TestOfDuelist.DROPLIST_COND[i][6]) && _222_TestOfDuelist.DROPLIST_COND[i][1] != cond && _222_TestOfDuelist.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_222_TestOfDuelist.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 2 && st.getQuestItemsCount(2768) >= 10L && st.getQuestItemsCount(2769) >= 10L && st.getQuestItemsCount(2770) >= 10L && st.getQuestItemsCount(2771) >= 10L && st.getQuestItemsCount(2772) >= 10L && st.getQuestItemsCount(2773) >= 10L && st.getQuestItemsCount(2774) >= 10L && st.getQuestItemsCount(2775) >= 10L && st.getQuestItemsCount(2776) >= 10L && st.getQuestItemsCount(2777) >= 10L) {
            st.setCond(3);
            st.setState(2);
        } else if (cond == 4 && st.getQuestItemsCount(2779) >= 3L && st.getQuestItemsCount(2780) >= 3L && st.getQuestItemsCount(2783) >= 3L && st.getQuestItemsCount(2781) >= 3L && st.getQuestItemsCount(2782) >= 3L) {
            st.setCond(5);
            st.setState(2);
        }
        return null;
    }
}
