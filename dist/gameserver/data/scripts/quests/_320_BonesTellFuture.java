package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _320_BonesTellFuture extends Quest {
    public final int BONE_FRAGMENT = 809;

    public _320_BonesTellFuture() {
        super(false);
        addStartNpc(30359);
        addTalkId(30359);
        addKillId(20517);
        addKillId(20518);
        addQuestItem(809);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("tetrarch_kaitar_q0320_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getRace() != Race.darkelf) {
                htmltext = "tetrarch_kaitar_q0320_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() >= 10) {
                htmltext = "tetrarch_kaitar_q0320_03.htm";
            } else {
                htmltext = "tetrarch_kaitar_q0320_02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (st.getQuestItemsCount(809) < 10L) {
            htmltext = "tetrarch_kaitar_q0320_05.htm";
        } else {
            htmltext = "tetrarch_kaitar_q0320_06.htm";
            st.giveItems(57, 8470L, true);
            st.takeItems(809, -1L);
            st.exitCurrentQuest(true);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        st.rollAndGive(809, 1, 1, 10, 10.0);
        if (st.getQuestItemsCount(809) >= 10L) {
            st.setCond(2);
        }
        st.setState(2);
        return null;
    }
}
