package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _272_WrathOfAncestors extends Quest {
    private static final int Livina = 30572;
    private static final int GraveRobbersHead = 1474;
    private static final int GoblinGraveRobber = 20319;
    private static final int GoblinTombRaiderLeader = 20320;
    private static final int[][] DROPLIST_COND = {{1, 2, 20319, 0, 1474, 50, 100, 1}, {1, 2, 20320, 0, 1474, 50, 100, 1}};

    public _272_WrathOfAncestors() {
        super(false);
        addStartNpc(30572);
        for (int i = 0; i < _272_WrathOfAncestors.DROPLIST_COND.length; ++i) {
            addKillId(_272_WrathOfAncestors.DROPLIST_COND[i][2]);
        }
        addQuestItem(1474);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "seer_livina_q0272_03.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30572) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() != Race.orc) {
                        htmltext = "seer_livina_q0272_00.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        if (st.getPlayer().getLevel() >= 5) {
                            htmltext = "seer_livina_q0272_02.htm";
                            return htmltext;
                        }
                        htmltext = "seer_livina_q0272_01.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    htmltext = "seer_livina_q0272_04.htm";
                    break;
                case 2:
                    st.takeItems(1474, -1L);
                    st.giveItems(57, 1500L);
                    htmltext = "seer_livina_q0272_05.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _272_WrathOfAncestors.DROPLIST_COND.length; ++i) {
            if (cond == _272_WrathOfAncestors.DROPLIST_COND[i][0] && npcId == _272_WrathOfAncestors.DROPLIST_COND[i][2] && (_272_WrathOfAncestors.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_272_WrathOfAncestors.DROPLIST_COND[i][3]) > 0L)) {
                if (_272_WrathOfAncestors.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_272_WrathOfAncestors.DROPLIST_COND[i][4], _272_WrathOfAncestors.DROPLIST_COND[i][7], (double) _272_WrathOfAncestors.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_272_WrathOfAncestors.DROPLIST_COND[i][4], _272_WrathOfAncestors.DROPLIST_COND[i][7], _272_WrathOfAncestors.DROPLIST_COND[i][7], _272_WrathOfAncestors.DROPLIST_COND[i][5], (double) _272_WrathOfAncestors.DROPLIST_COND[i][6]) && _272_WrathOfAncestors.DROPLIST_COND[i][1] != cond && _272_WrathOfAncestors.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_272_WrathOfAncestors.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        return null;
    }
}
