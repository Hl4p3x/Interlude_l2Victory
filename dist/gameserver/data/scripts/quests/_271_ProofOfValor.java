package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _271_ProofOfValor extends Quest {
    private static final int RUKAIN = 30577;
    private static final int KASHA_WOLF_FANG_ID = 1473;
    private static final int NECKLACE_OF_VALOR_ID = 1507;
    private static final int NECKLACE_OF_COURAGE_ID = 1506;
    private static final int[][] DROPLIST_COND = {{1, 2, 20475, 0, 1473, 50, 25, 2}};

    public _271_ProofOfValor() {
        super(false);
        addStartNpc(30577);
        addTalkId(30577);
        for (int i = 0; i < _271_ProofOfValor.DROPLIST_COND.length; ++i) {
            addKillId(_271_ProofOfValor.DROPLIST_COND[i][2]);
        }
        addQuestItem(1473);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("praetorian_rukain_q0271_03.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(1506) > 0L || st.getQuestItemsCount(1507) > 0L) {
                htmltext = "praetorian_rukain_q0271_07.htm";
            }
            st.setCond(1);
            st.setState(2);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30577) {
            if (cond == 0) {
                if (st.getPlayer().getRace() != Race.orc) {
                    htmltext = "praetorian_rukain_q0271_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() < 4) {
                    htmltext = "praetorian_rukain_q0271_01.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getQuestItemsCount(1506) > 0L || st.getQuestItemsCount(1507) > 0L) {
                    htmltext = "praetorian_rukain_q0271_06.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "praetorian_rukain_q0271_02.htm";
                }
            } else if (cond == 1) {
                htmltext = "praetorian_rukain_q0271_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(1473) == 50L) {
                st.takeItems(1473, -1L);
                if (Rnd.chance(14)) {
                    st.takeItems(1507, -1L);
                    st.giveItems(1507, 1L);
                } else {
                    st.takeItems(1506, -1L);
                    st.giveItems(1506, 1L);
                }
                htmltext = "praetorian_rukain_q0271_05.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 2 && st.getQuestItemsCount(1473) < 50L) {
                htmltext = "praetorian_rukain_q0271_04.htm";
                st.setCond(1);
                st.setState(2);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _271_ProofOfValor.DROPLIST_COND.length; ++i) {
            if (cond == _271_ProofOfValor.DROPLIST_COND[i][0] && npcId == _271_ProofOfValor.DROPLIST_COND[i][2] && (_271_ProofOfValor.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_271_ProofOfValor.DROPLIST_COND[i][3]) > 0L)) {
                if (_271_ProofOfValor.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_271_ProofOfValor.DROPLIST_COND[i][4], _271_ProofOfValor.DROPLIST_COND[i][7], (double) _271_ProofOfValor.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_271_ProofOfValor.DROPLIST_COND[i][4], _271_ProofOfValor.DROPLIST_COND[i][7], _271_ProofOfValor.DROPLIST_COND[i][7], _271_ProofOfValor.DROPLIST_COND[i][5], (double) _271_ProofOfValor.DROPLIST_COND[i][6]) && _271_ProofOfValor.DROPLIST_COND[i][1] != cond && _271_ProofOfValor.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_271_ProofOfValor.DROPLIST_COND[i][1]);
                    st.setState(2);
                }
            }
        }
        return null;
    }
}
