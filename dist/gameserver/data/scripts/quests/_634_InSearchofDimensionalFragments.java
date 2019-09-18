package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _634_InSearchofDimensionalFragments extends Quest {
    int DIMENSION_FRAGMENT_ID;

    public _634_InSearchofDimensionalFragments() {
        super(true);
        DIMENSION_FRAGMENT_ID = 7079;
        for (int npcId = 31494; npcId < 31508; ++npcId) {
            addTalkId(npcId);
            addStartNpc(npcId);
        }
        for (int mobs = 21208; mobs < 21256; ++mobs) {
            addKillId(mobs);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "dimension_keeper_1_q0634_03.htm";
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
        } else if ("634_2".equalsIgnoreCase(event)) {
            htmltext = "dimension_keeper_1_q0634_06.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            if (st.getPlayer().getLevel() > 20) {
                htmltext = "dimension_keeper_1_q0634_01.htm";
            } else {
                htmltext = "dimension_keeper_1_q0634_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (id == 2) {
            htmltext = "dimension_keeper_1_q0634_04.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cnt = (int) (1.6 + npc.getLevel() * 0.15f);
        st.rollAndGive(DIMENSION_FRAGMENT_ID, cnt, 90.0);
        return null;
    }
}
