package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _117_OceanOfDistantStar extends Quest {
    private static final int Abey = 32053;
    private static final int GhostEngineer = 32055;
    private static final int Obi = 32052;
    private static final int GhostEngineer2 = 32054;
    private static final int Box = 32076;
    private static final int BookOfGreyStar = 8495;
    private static final int EngravedHammer = 8488;
    private static final int BanditWarrior = 22023;
    private static final int BanditInspector = 22024;

    public _117_OceanOfDistantStar() {
        super(false);
        addStartNpc(32053);
        addTalkId(32055);
        addTalkId(32052);
        addTalkId(32076);
        addTalkId(32054);
        addKillId(22023);
        addKillId(22024);
        addQuestItem(8495, 8488);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("railman_abu_q0117_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("ghost_of_railroadman2_q0117_0201.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
        } else if ("railman_obi_q0117_0301.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
        } else if ("railman_abu_q0117_0401.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
        } else if ("q_box_of_railroad_q0117_0501.htm".equalsIgnoreCase(event)) {
            st.setCond(5);
            st.giveItems(8488, 1L);
        } else if ("railman_abu_q0117_0601.htm".equalsIgnoreCase(event)) {
            st.setCond(6);
        } else if ("railman_obi_q0117_0701.htm".equalsIgnoreCase(event)) {
            st.setCond(7);
        } else if ("railman_obi_q0117_0801.htm".equalsIgnoreCase(event)) {
            st.takeItems(8495, -1L);
            st.setCond(9);
        } else if ("ghost_of_railroadman2_q0117_0901.htm".equalsIgnoreCase(event)) {
            st.takeItems(8488, -1L);
            st.setCond(10);
        } else if ("ghost_of_railroadman_q0117_1002.htm".equalsIgnoreCase(event)) {
            st.addExpAndSp(63591L, 0L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 32053) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 39) {
                    htmltext = "railman_abu_q0117_0101.htm";
                } else {
                    htmltext = "railman_abu_q0117_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 3) {
                htmltext = "railman_abu_q0117_0301.htm";
            } else if (cond == 5 && st.getQuestItemsCount(8488) > 0L) {
                htmltext = "railman_abu_q0117_0501.htm";
            } else if (cond == 6 && st.getQuestItemsCount(8488) > 0L) {
                htmltext = "railman_abu_q0117_0601.htm";
            }
        } else if (npcId == 32055) {
            if (cond == 1) {
                htmltext = "ghost_of_railroadman2_q0117_0101.htm";
            } else if (cond == 9 && st.getQuestItemsCount(8488) > 0L) {
                htmltext = "ghost_of_railroadman2_q0117_0801.htm";
            }
        } else if (npcId == 32052) {
            if (cond == 2) {
                htmltext = "railman_obi_q0117_0201.htm";
            } else if (cond == 6 && st.getQuestItemsCount(8488) > 0L) {
                htmltext = "railman_obi_q0117_0601.htm";
            } else if (cond == 7 && st.getQuestItemsCount(8488) > 0L) {
                htmltext = "railman_obi_q0117_0701.htm";
            } else if (cond == 8 && st.getQuestItemsCount(8495) > 0L) {
                htmltext = "railman_obi_q0117_0704.htm";
            }
        } else if (npcId == 32076 && cond == 4) {
            htmltext = "q_box_of_railroad_q0117_0401.htm";
        } else if (npcId == 32054 && cond == 10) {
            htmltext = "ghost_of_railroadman_q0117_0901.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 7 && Rnd.chance(30)) {
            if (st.getQuestItemsCount(8495) < 1L) {
                st.giveItems(8495, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            st.setCond(8);
            st.setState(2);
        }
        return null;
    }
}
