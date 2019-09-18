package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _277_GatekeepersOffering extends Quest {
    private static final int STARSTONE1_ID = 1572;
    private static final int GATEKEEPER_CHARM_ID = 1658;

    public _277_GatekeepersOffering() {
        super(false);
        addStartNpc(30576);
        addKillId(20333);
        addQuestItem(1572);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            if (st.getPlayer().getLevel() >= 15) {
                htmltext = "gatekeeper_tamil_q0277_03.htm";
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "gatekeeper_tamil_q0277_01.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30576 && cond == 0) {
            htmltext = "gatekeeper_tamil_q0277_02.htm";
        } else if (npcId == 30576 && cond == 1 && st.getQuestItemsCount(1572) < 20L) {
            htmltext = "gatekeeper_tamil_q0277_04.htm";
        } else if (npcId == 30576 && cond == 2 && st.getQuestItemsCount(1572) < 20L) {
            htmltext = "gatekeeper_tamil_q0277_04.htm";
        } else if (npcId == 30576 && cond == 2 && st.getQuestItemsCount(1572) >= 20L) {
            htmltext = "gatekeeper_tamil_q0277_05.htm";
            st.takeItems(1572, -1L);
            st.giveItems(1658, 2L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        st.rollAndGive(1572, 1, 1, 20, 33.0);
        if (st.getQuestItemsCount(1572) >= 20L) {
            st.setCond(2);
        }
        return null;
    }
}
