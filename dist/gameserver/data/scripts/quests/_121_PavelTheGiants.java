package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _121_PavelTheGiants extends Quest {
    private static final int NEWYEAR = 31961;
    private static final int YUMI = 32041;

    public _121_PavelTheGiants() {
        super(false);
        addStartNpc(NEWYEAR);
        addTalkId(NEWYEAR, YUMI);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("collecter_yumi_q0121_0201.htm".equals(event)) {
            st.playSound("ItemSound.quest_finish");
            st.addExpAndSp(10000L, 0L);
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1 && npcId == NEWYEAR) {
            if (st.getPlayer().getLevel() >= 46) {
                htmltext = "head_blacksmith_newyear_q0121_0101.htm";
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "head_blacksmith_newyear_q0121_0103.htm";
                st.exitCurrentQuest(false);
            }
        } else if (id == 2) {
            if (npcId == YUMI && cond == 1) {
                htmltext = "collecter_yumi_q0121_0101.htm";
            } else {
                htmltext = "head_blacksmith_newyear_q0121_0105.htm";
            }
        }
        return htmltext;
    }
}
