package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;

public class _292_BrigandsSweep extends Quest {
    private static final int Spiron = 30532;
    private static final int Balanki = 30533;
    private static final int GoblinBrigand = 20322;
    private static final int GoblinBrigandLeader = 20323;
    private static final int GoblinBrigandLieutenant = 20324;
    private static final int GoblinSnooper = 20327;
    private static final int GoblinLord = 20528;
    private static final int GoblinNecklace = 1483;
    private static final int GoblinPendant = 1484;
    private static final int GoblinLordPendant = 1485;
    private static final int SuspiciousMemo = 1486;
    private static final int SuspiciousContract = 1487;
    private static final int Chance = 10;
    private static final int[][] DROPLIST_COND = {{1, 0, GoblinBrigand, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinBrigandLeader, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinSnooper, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinBrigandLieutenant, 0, GoblinPendant, 0, 40, 1}, {1, 0, GoblinLord, 0, GoblinLordPendant, 0, 40, 1}};


    public _292_BrigandsSweep() {
        super(false);
        addStartNpc(Spiron);
        addTalkId(Balanki);
        Arrays.stream(DROPLIST_COND).map(ints -> ints[2]).forEach(this::addKillId);
        addQuestItem(SuspiciousMemo);
        addQuestItem(SuspiciousContract);
        addQuestItem(GoblinNecklace);
        addQuestItem(GoblinPendant);
        addQuestItem(GoblinLordPendant);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("elder_spiron_q0292_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("elder_spiron_q0292_06.htm".equalsIgnoreCase(event)) {
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
        if (npcId == Spiron) {
            if (cond == 0) {
                if (st.getPlayer().getRace() != Race.dwarf) {
                    htmltext = "elder_spiron_q0292_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() < 5) {
                    htmltext = "elder_spiron_q0292_01.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "elder_spiron_q0292_02.htm";
                }
            } else if (cond == 1) {
                final long reward = st.getQuestItemsCount(GoblinNecklace) * 12L + st.getQuestItemsCount(GoblinPendant) * 36L + st.getQuestItemsCount(GoblinLordPendant) * 33L + st.getQuestItemsCount(SuspiciousContract) * 100L;
                if (reward == 0L) {
                    return "elder_spiron_q0292_04.htm";
                }
                if (st.getQuestItemsCount(SuspiciousContract) != 0L) {
                    htmltext = "elder_spiron_q0292_10.htm";
                } else if (st.getQuestItemsCount(SuspiciousMemo) == 0L) {
                    htmltext = "elder_spiron_q0292_05.htm";
                } else if (st.getQuestItemsCount(SuspiciousMemo) == 1L) {
                    htmltext = "elder_spiron_q0292_08.htm";
                } else {
                    htmltext = "elder_spiron_q0292_09.htm";
                }
                st.takeItems(GoblinNecklace, -1L);
                st.takeItems(GoblinPendant, -1L);
                st.takeItems(GoblinLordPendant, -1L);
                st.takeItems(SuspiciousContract, -1L);
                st.giveItems(57, reward);
            }
        } else if (npcId == Balanki && cond == 1) {
            if (st.getQuestItemsCount(SuspiciousContract) == 0L) {
                htmltext = "balanki_q0292_01.htm";
            } else {
                st.takeItems(SuspiciousContract, -1L);
                st.giveItems(57, 120L);
                htmltext = "balanki_q0292_02.htm";
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
        if (st.getQuestItemsCount(SuspiciousContract) == 0L && Rnd.chance(Chance)) {
            if (st.getQuestItemsCount(SuspiciousMemo) < 3L) {
                st.giveItems(SuspiciousMemo, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.takeItems(SuspiciousMemo, -1L);
                st.giveItems(SuspiciousContract, 1L);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }
}
