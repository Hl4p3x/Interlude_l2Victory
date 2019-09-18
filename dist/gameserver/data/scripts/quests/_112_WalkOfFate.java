package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _112_WalkOfFate extends Quest {
    private static final int Livina = 30572;
    private static final int Karuda = 32017;
    private static final int EnchantD = 956;

    public _112_WalkOfFate() {
        super(false);
        addStartNpc(30572);
        addTalkId(32017);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("karuda_q0112_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(57, 4665L, true);
            st.giveItems(956, 1L, false);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("seer_livina_q0112_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30572) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 20) {
                    htmltext = "seer_livina_q0112_0101.htm";
                } else {
                    htmltext = "seer_livina_q0112_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "seer_livina_q0112_0105.htm";
            }
        } else if (npcId == 32017 && cond == 1) {
            htmltext = "karuda_q0112_0101.htm";
        }
        return htmltext;
    }
}
