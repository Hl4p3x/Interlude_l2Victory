package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _627_HeartInSearchOfPower extends Quest {
    private static final int M_NECROMANCER = 31518;
    private static final int ENFEUX = 31519;
    private static final int SEAL_OF_LIGHT = 7170;
    private static final int GEM_OF_SUBMISSION = 7171;
    private static final int GEM_OF_SAINTS = 7172;
    private static final int MOLD_HARDENER = 4041;
    private static final int ENRIA = 4042;
    private static final int ASOFE = 4043;
    private static final int THONS = 4044;

    public _627_HeartInSearchOfPower() {
        super(true);
        addStartNpc(31518);
        addTalkId(31518);
        addTalkId(31519);
        for (int mobs = 21520; mobs <= 21541; ++mobs) {
            addKillId(mobs);
        }
        addQuestItem(7171);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "dark_necromancer_q0627_0104.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "dark_necromancer_q0627_0201.htm":
                st.takeItems(7171, -1L);
                st.giveItems(7170, 1L, false);
                st.setCond(3);
                break;
            case "enfeux_q0627_0301.htm":
                st.takeItems(7170, 1L);
                st.giveItems(7172, 1L, false);
                st.setCond(4);
                break;
            case "dark_necromancer_q0627_0401.htm":
                st.takeItems(7172, 1L);
                break;
            default:
                switch (event) {
                    case "627_11":
                        htmltext = "dark_necromancer_q0627_0402.htm";
                        st.giveItems(57, 100000L, true);
                        break;
                    case "627_12":
                        htmltext = "dark_necromancer_q0627_0402.htm";
                        st.giveItems(4043, 13L, true);
                        st.giveItems(57, 6400L, true);
                        break;
                    case "627_13":
                        htmltext = "dark_necromancer_q0627_0402.htm";
                        st.giveItems(4044, 13L, true);
                        st.giveItems(57, 6400L, true);
                        break;
                    case "627_14":
                        htmltext = "dark_necromancer_q0627_0402.htm";
                        st.giveItems(4042, 6L, true);
                        st.giveItems(57, 13600L, true);
                        break;
                    case "627_15":
                        htmltext = "dark_necromancer_q0627_0402.htm";
                        st.giveItems(4041, 3L, true);
                        st.giveItems(57, 17200L, true);
                        break;
                }
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31518) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 60) {
                    htmltext = "dark_necromancer_q0627_0101.htm";
                } else {
                    htmltext = "dark_necromancer_q0627_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "dark_necromancer_q0627_0106.htm";
            } else if (st.getQuestItemsCount(7171) >= 300L) {
                htmltext = "dark_necromancer_q0627_0105.htm";
            } else if (st.getQuestItemsCount(7172) > 0L) {
                htmltext = "dark_necromancer_q0627_0301.htm";
            }
        } else if (npcId == 31519 && st.getQuestItemsCount(7170) > 0L) {
            htmltext = "enfeux_q0627_0201.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            st.rollAndGive(7171, 1, 100.0);
            if (st.getQuestItemsCount(7171) >= 300L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
