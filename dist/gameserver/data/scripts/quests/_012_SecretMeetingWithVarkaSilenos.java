package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _012_SecretMeetingWithVarkaSilenos extends Quest {
    private final int CADMON = 31296;
    private final int HELMUT = 31258;
    private final int NARAN_ASHANUK = 31378;
    private final int MUNITIONS_BOX = 7232;

    public _012_SecretMeetingWithVarkaSilenos() {
        super(false);
        addStartNpc(CADMON);
        addTalkId(HELMUT, NARAN_ASHANUK);
        addTalkId(NARAN_ASHANUK);
        addQuestItem(MUNITIONS_BOX);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("guard_cadmon_q0012_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("trader_helmut_q0012_0201.htm".equalsIgnoreCase(event)) {
            st.giveItems(MUNITIONS_BOX, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("herald_naran_q0012_0301.htm".equalsIgnoreCase(event)) {
            st.takeItems(MUNITIONS_BOX, 1L);
            st.addExpAndSp(79761L, 0L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == CADMON) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    htmltext = "guard_cadmon_q0012_0101.htm";
                } else {
                    htmltext = "guard_cadmon_q0012_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "guard_cadmon_q0012_0105.htm";
            }
        } else if (npcId == HELMUT) {
            if (cond == 1) {
                htmltext = "trader_helmut_q0012_0101.htm";
            } else if (cond == 2) {
                htmltext = "trader_helmut_q0012_0202.htm";
            }
        } else if (npcId == NARAN_ASHANUK && cond == 2 && st.getQuestItemsCount(MUNITIONS_BOX) > 0L) {
            htmltext = "herald_naran_q0012_0201.htm";
        }
        return htmltext;
    }
}
