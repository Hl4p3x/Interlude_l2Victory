package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _338_AlligatorHunter extends Quest {
    private static final int Enverun = 30892;
    private static final int AlligatorLeather = 4337;
    private static final int CrokianLad = 20804;
    private static final int DailaonLad = 20805;
    private static final int CrokianLadWarrior = 20806;
    private static final int FarhiteLad = 20807;
    private static final int NosLad = 20808;
    private static final int SwampTribe = 20991;
    public final int[][] DROPLIST_COND;

    public _338_AlligatorHunter() {
        super(false);
        DROPLIST_COND = new int[][]{{1, 0, 20804, 0, 4337, 0, 60, 1}, {1, 0, 20805, 0, 4337, 0, 60, 1}, {1, 0, 20806, 0, 4337, 0, 60, 1}, {1, 0, 20807, 0, 4337, 0, 60, 1}, {1, 0, 20808, 0, 4337, 0, 60, 1}, {1, 0, 20991, 0, 4337, 0, 60, 1}};
        addStartNpc(30892);
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[2]);
        }
        addQuestItem(4337);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30892-02.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
            st.setState(2);
        } else if ("30892-02-afmenu.htm".equalsIgnoreCase(event)) {
            final long AdenaCount = st.getQuestItemsCount(4337) * 40L;
            st.takeItems(4337, -1L);
            st.giveItems(57, AdenaCount);
        } else if ("quit".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(4337) >= 1L) {
                final long AdenaCount = st.getQuestItemsCount(4337) * 40L;
                st.takeItems(4337, -1L);
                st.giveItems(57, AdenaCount);
                htmltext = "30892-havequit.htm";
            } else {
                htmltext = "30892-havent.htm";
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30892) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 40) {
                    htmltext = "30892-01.htm";
                } else {
                    htmltext = "30892-00.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (st.getQuestItemsCount(4337) == 0L) {
                htmltext = "30892-02-rep.htm";
            } else {
                htmltext = "30892-menu.htm";
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
