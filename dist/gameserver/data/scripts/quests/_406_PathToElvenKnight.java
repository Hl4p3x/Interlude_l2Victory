package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _406_PathToElvenKnight extends Quest {
    private static final int Sorius = 30327;
    private static final int Kluto = 30317;
    private static final int SoriussLetter = 1202;
    private static final int KlutoBox = 1203;
    private static final int TopazPiece = 1205;
    private static final int EmeraldPiece = 1206;
    private static final int KlutosMemo = 1276;
    private static final int ElvenKnightBrooch = 1204;
    private static final int TrackerSkeleton = 20035;
    private static final int TrackerSkeletonLeader = 20042;
    private static final int SkeletonScout = 20045;
    private static final int SkeletonBowman = 20051;
    private static final int RagingSpartoi = 20060;
    private static final int OlMahumNovice = 20782;
    private static final int[][] DROPLIST_COND = {{1, 2, 20035, 0, 1205, 20, 70, 1}, {1, 2, 20042, 0, 1205, 20, 70, 1}, {1, 2, 20045, 0, 1205, 20, 70, 1}, {1, 2, 20051, 0, 1205, 20, 70, 1}, {1, 2, 20060, 0, 1205, 20, 70, 1}, {4, 5, 20782, 0, 1206, 20, 50, 1}};

    public _406_PathToElvenKnight() {
        super(false);
        addStartNpc(30327);
        addTalkId(30317);
        for (int i = 0; i < _406_PathToElvenKnight.DROPLIST_COND.length; ++i) {
            addKillId(_406_PathToElvenKnight.DROPLIST_COND[i][2]);
        }
        addQuestItem(1205, 1206, 1202, 1276, 1203);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("master_sorius_q0406_05.htm".equalsIgnoreCase(event)) {
            switch (st.getPlayer().getClassId().getId()) {
                case 18:
                    if (st.getQuestItemsCount(1204) > 0L) {
                        htmltext = "master_sorius_q0406_04.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() < 18) {
                        htmltext = "master_sorius_q0406_03.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 19:
                    htmltext = "master_sorius_q0406_02a.htm";
                    st.exitCurrentQuest(true);
                    break;
                default:
                    htmltext = "master_sorius_q0406_02.htm";
                    st.exitCurrentQuest(true);
                    break;
            }
        } else if ("master_sorius_q0406_06.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("blacksmith_kluto_q0406_02.htm".equalsIgnoreCase(event)) {
            st.takeItems(1202, -1L);
            st.giveItems(1276, 1L);
            st.setCond(4);
            st.setState(2);
        } else {
            htmltext = "noquest";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30327) {
            switch (cond) {
                case 0:
                    htmltext = "master_sorius_q0406_01.htm";
                    break;
                case 1:
                    if (st.getQuestItemsCount(1205) == 0L) {
                        htmltext = "master_sorius_q0406_07.htm";
                    } else {
                        htmltext = "master_sorius_q0406_08.htm";
                    }
                    break;
                case 2:
                    st.takeItems(1205, -1L);
                    st.giveItems(1202, 1L);
                    htmltext = "master_sorius_q0406_09.htm";
                    st.setCond(3);
                    st.setState(2);
                    break;
                case 3:
                case 4:
                case 5:
                    htmltext = "master_sorius_q0406_11.htm";
                    break;
                case 6:
                    st.takeItems(1203, -1L);
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(1204, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 2280L);
                        }
                    }
                    st.exitCurrentQuest(true);
                    st.playSound("ItemSound.quest_finish");
                    htmltext = "master_sorius_q0406_10.htm";
                    break;
            }
        } else if (npcId == 30317) {
            switch (cond) {
                case 3:
                    htmltext = "blacksmith_kluto_q0406_01.htm";
                    break;
                case 4:
                    if (st.getQuestItemsCount(1206) == 0L) {
                        htmltext = "blacksmith_kluto_q0406_03.htm";
                    } else {
                        htmltext = "blacksmith_kluto_q0406_04.htm";
                    }
                    break;
                case 5:
                    st.takeItems(1206, -1L);
                    st.takeItems(1276, -1L);
                    st.giveItems(1203, 1L);
                    htmltext = "blacksmith_kluto_q0406_05.htm";
                    st.setCond(6);
                    st.setState(2);
                    break;
                case 6:
                    htmltext = "blacksmith_kluto_q0406_06.htm";
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _406_PathToElvenKnight.DROPLIST_COND.length; ++i) {
            if (cond == _406_PathToElvenKnight.DROPLIST_COND[i][0] && npcId == _406_PathToElvenKnight.DROPLIST_COND[i][2] && (_406_PathToElvenKnight.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_406_PathToElvenKnight.DROPLIST_COND[i][3]) > 0L)) {
                if (_406_PathToElvenKnight.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_406_PathToElvenKnight.DROPLIST_COND[i][4], _406_PathToElvenKnight.DROPLIST_COND[i][7], (double) _406_PathToElvenKnight.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_406_PathToElvenKnight.DROPLIST_COND[i][4], _406_PathToElvenKnight.DROPLIST_COND[i][7], _406_PathToElvenKnight.DROPLIST_COND[i][7], _406_PathToElvenKnight.DROPLIST_COND[i][5], (double) _406_PathToElvenKnight.DROPLIST_COND[i][6]) && _406_PathToElvenKnight.DROPLIST_COND[i][1] != cond && _406_PathToElvenKnight.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_406_PathToElvenKnight.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        return null;
    }
}
