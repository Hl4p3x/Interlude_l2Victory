package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _418_PathToArtisan extends Quest {
    private static final int Silvera = 30527;
    private static final int Kluto = 30317;
    private static final int Pinter = 30298;
    private static final int SilverasRing = 1632;
    private static final int BoogleRatmanTooth = 1636;
    private static final int BoogleRatmanLeadersTooth = 1637;
    private static final int PassCertificate1st = 1633;
    private static final int KlutosLetter = 1638;
    private static final int FootprintOfThief = 1639;
    private static final int StolenSecretBox = 1640;
    private static final int PassCertificate2nd = 1634;
    private static final int SecretBox = 1641;
    private static final int FinalPassCertificate = 1635;
    private static final int BoogleRatman = 20389;
    private static final int BoogleRatmanLeader = 20390;
    private static final int VukuOrcFighter = 20017;
    private static final int[][] DROPLIST_COND = {{1, 0, 20389, 1632, 1636, 10, 35, 1}, {1, 0, 20390, 1632, 1637, 2, 25, 1}, {5, 6, 20017, 1639, 1640, 1, 20, 1}};

    public _418_PathToArtisan() {
        super(false);
        addStartNpc(30527);
        addTalkId(30317, 30298);
        for (int i = 0; i < _418_PathToArtisan.DROPLIST_COND.length; ++i) {
            addKillId(_418_PathToArtisan.DROPLIST_COND[i][2]);
            addQuestItem(_418_PathToArtisan.DROPLIST_COND[i][4]);
        }
        addQuestItem(1632, 1633, 1641, 1638, 1639, 1634);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("blacksmith_silvery_q0418_06.htm".equalsIgnoreCase(event)) {
            st.giveItems(1632, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("blacksmith_kluto_q0418_04.htm".equalsIgnoreCase(event) || "blacksmith_kluto_q0418_07.htm".equalsIgnoreCase(event)) {
            st.giveItems(1638, 1L);
            st.setCond(4);
            st.setState(2);
        } else if ("blacksmith_pinter_q0418_03.htm".equalsIgnoreCase(event)) {
            st.takeItems(1638, -1L);
            st.giveItems(1639, 1L);
            st.setCond(5);
            st.setState(2);
        } else if ("blacksmith_pinter_q0418_06.htm".equalsIgnoreCase(event)) {
            st.takeItems(1640, -1L);
            st.takeItems(1639, -1L);
            st.giveItems(1641, 1L);
            st.giveItems(1634, 1L);
            st.setCond(7);
            st.setState(2);
        } else if ("blacksmith_kluto_q0418_10.htm".equalsIgnoreCase(event) || "blacksmith_kluto_q0418_12.htm".equalsIgnoreCase(event)) {
            st.takeItems(1633, -1L);
            st.takeItems(1634, -1L);
            st.takeItems(1641, -1L);
            if (st.getPlayer().getClassId().getLevel() == 1) {
                st.giveItems(1635, 1L);
                if (!st.getPlayer().getVarB("prof1")) {
                    st.getPlayer().setVar("prof1", "1", -1L);
                    st.addExpAndSp(3200L, 2790L);
                }
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30527:
                if (st.getQuestItemsCount(1635) != 0L) {
                    htmltext = "blacksmith_silvery_q0418_04.htm";
                    st.exitCurrentQuest(true);
                } else if (cond == 0) {
                    if (st.getPlayer().getClassId().getId() != 53) {
                        if (st.getPlayer().getClassId().getId() == 56) {
                            htmltext = "blacksmith_silvery_q0418_02a.htm";
                        } else {
                            htmltext = "blacksmith_silvery_q0418_02.htm";
                        }
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() < 18) {
                        htmltext = "blacksmith_silvery_q0418_03.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "blacksmith_silvery_q0418_01.htm";
                    }
                } else if (cond == 1) {
                    htmltext = "blacksmith_silvery_q0418_07.htm";
                } else if (cond == 2) {
                    st.takeItems(1636, -1L);
                    st.takeItems(1637, -1L);
                    st.takeItems(1632, -1L);
                    st.giveItems(1633, 1L);
                    htmltext = "blacksmith_silvery_q0418_08.htm";
                    st.setCond(3);
                } else if (cond == 3) {
                    htmltext = "blacksmith_silvery_q0418_09.htm";
                }
                break;
            case 30317:
                switch (cond) {
                    case 3:
                        htmltext = "blacksmith_kluto_q0418_01.htm";
                        break;
                    case 4:
                    case 5:
                        htmltext = "blacksmith_kluto_q0418_08.htm";
                        break;
                    case 7:
                        htmltext = "blacksmith_kluto_q0418_09.htm";
                        break;
                }
                break;
            case 30298:
                switch (cond) {
                    case 4:
                        htmltext = "blacksmith_pinter_q0418_01.htm";
                        break;
                    case 5:
                        htmltext = "blacksmith_pinter_q0418_04.htm";
                        break;
                    case 6:
                        htmltext = "blacksmith_pinter_q0418_05.htm";
                        break;
                    case 7:
                        htmltext = "blacksmith_pinter_q0418_07.htm";
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
        for (int i = 0; i < _418_PathToArtisan.DROPLIST_COND.length; ++i) {
            if (cond == _418_PathToArtisan.DROPLIST_COND[i][0] && npcId == _418_PathToArtisan.DROPLIST_COND[i][2] && (_418_PathToArtisan.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_418_PathToArtisan.DROPLIST_COND[i][3]) > 0L)) {
                if (_418_PathToArtisan.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_418_PathToArtisan.DROPLIST_COND[i][4], _418_PathToArtisan.DROPLIST_COND[i][7], (double) _418_PathToArtisan.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_418_PathToArtisan.DROPLIST_COND[i][4], _418_PathToArtisan.DROPLIST_COND[i][7], _418_PathToArtisan.DROPLIST_COND[i][7], _418_PathToArtisan.DROPLIST_COND[i][5], (double) _418_PathToArtisan.DROPLIST_COND[i][6]) && _418_PathToArtisan.DROPLIST_COND[i][1] != cond && _418_PathToArtisan.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_418_PathToArtisan.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        if (cond == 1 && st.getQuestItemsCount(1636) >= 10L && st.getQuestItemsCount(1637) >= 2L) {
            st.setCond(2);
            st.setState(2);
        }
        return null;
    }
}
