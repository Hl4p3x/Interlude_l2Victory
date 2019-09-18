package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _297_GateKeepersFavor extends Quest {
    private static final int STARSTONE = 1573;
    private static final int GATEKEEPER_TOKEN = 1659;

    public _297_GateKeepersFavor() {
        super(false);
        addStartNpc(30540);
        addTalkId(30540);
        addKillId(20521);
        addQuestItem(1573);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("gatekeeper_wirphy_q0297_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30540) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 15) {
                    htmltext = "gatekeeper_wirphy_q0297_02.htm";
                } else {
                    htmltext = "gatekeeper_wirphy_q0297_01.htm";
                }
            } else if (cond == 1 && st.getQuestItemsCount(1573) < 20L) {
                htmltext = "gatekeeper_wirphy_q0297_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(1573) < 20L) {
                htmltext = "gatekeeper_wirphy_q0297_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(1573) >= 20L) {
                htmltext = "gatekeeper_wirphy_q0297_05.htm";
                st.takeItems(1573, -1L);
                st.giveItems(1659, 2L);
                st.exitCurrentQuest(true);
                st.playSound("ItemSound.quest_finish");
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        st.rollAndGive(1573, 1, 1, 20, 33.0);
        if (st.getQuestItemsCount(1573) >= 20L) {
            st.setCond(2);
        }
        return null;
    }
}
