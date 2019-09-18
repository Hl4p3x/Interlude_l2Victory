package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _357_WarehouseKeepersAmbition extends Quest {
    private static final int DROPRATE = 50;
    private static final int REWARD1 = 900;
    private static final int REWARD2 = 10000;
    private static final int SILVA = 30686;
    private static final int MOB1 = 20594;
    private static final int MOB2 = 20595;
    private static final int MOB3 = 20596;
    private static final int MOB4 = 20597;
    private static final int MOB5 = 20598;
    private static final int JADE_CRYSTAL = 5867;

    public _357_WarehouseKeepersAmbition() {
        super(false);
        addStartNpc(30686);
        addKillId(20594);
        addKillId(20595);
        addKillId(20596);
        addKillId(20597);
        addKillId(20598);
        addQuestItem(5867);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("warehouse_keeper_silva_q0357_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("warehouse_keeper_silva_q0357_08.htm".equalsIgnoreCase(event)) {
            final long count = st.getQuestItemsCount(5867);
            if (count > 0L) {
                long reward = count * 900L;
                if (count >= 100L) {
                    reward += 10000L;
                }
                st.takeItems(5867, -1L);
                st.giveItems(57, reward);
            } else {
                htmltext = "warehouse_keeper_silva_q0357_06.htm";
            }
        } else if ("warehouse_keeper_silva_q0357_11.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        final int cond = st.getCond();
        final long jade = st.getQuestItemsCount(5867);
        if (cond == 0 || id == 1) {
            if (st.getPlayer().getLevel() >= 47) {
                htmltext = "warehouse_keeper_silva_q0357_02.htm";
            } else {
                htmltext = "warehouse_keeper_silva_q0357_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (jade == 0L) {
            htmltext = "warehouse_keeper_silva_q0357_06.htm";
        } else if (jade > 0L) {
            htmltext = "warehouse_keeper_silva_q0357_07.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (Rnd.chance(50)) {
            st.giveItems(5867, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
