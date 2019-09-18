package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;

public class _317_CatchTheWind extends Quest {
    private static final int Rizraell = 30361;
    private static final int WindShard = 1078;
    private static final int Lirein = 20036;
    private static final int LireinElder = 20044;

    public final int[][] DROPLIST_COND = {{1, 0, Lirein, 0, WindShard, 0, 60, 1}, {1, 0, LireinElder, 0, WindShard, 0, 60, 1}};

    public _317_CatchTheWind() {
        super(false);
        addStartNpc(Rizraell);
        Arrays.stream(DROPLIST_COND).map(aDROPLIST_COND -> aDROPLIST_COND[2]).forEach(this::addKillId);
        addQuestItem(WindShard);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("rizraell_q0317_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("rizraell_q0317_08.htm".equalsIgnoreCase(event)) {
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
        if (npcId == Rizraell) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 18) {
                    htmltext = "rizraell_q0317_03.htm";
                } else {
                    htmltext = "rizraell_q0317_02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                final long count = st.getQuestItemsCount(WindShard);
                if (count > 0L) {
                    st.takeItems(WindShard, -1L);
                    st.giveItems(57, 40L * count);
                    htmltext = "rizraell_q0317_07.htm";
                } else {
                    htmltext = "rizraell_q0317_05.htm";
                }
            }
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
        return null;
    }
}
