package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _009_IntoTheCityOfHumans extends Quest {
    public static final int PETUKAI = 30583;
    public static final int TANAPI = 30571;
    public static final int TAMIL = 30576;
    public static final int SCROLL_OF_ESCAPE_GIRAN = 7126;
    public static final int MARK_OF_TRAVELER = 7570;

    public _009_IntoTheCityOfHumans() {
        super(false);
        addStartNpc(PETUKAI);
        addTalkId(PETUKAI, TANAPI, TAMIL);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("centurion_petukai_q0009_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("seer_tanapi_q0009_0201.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("gatekeeper_tamil_q0009_0301.htm".equalsIgnoreCase(event)) {
            st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1L);
            st.giveItems(MARK_OF_TRAVELER, 1L);
            st.unset("cond");
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
        if (npcId == PETUKAI) {
            if (cond == 0) {
                if (st.getPlayer().getRace() == Race.orc && st.getPlayer().getLevel() >= 3) {
                    htmltext = "centurion_petukai_q0009_0101.htm";
                } else {
                    htmltext = "centurion_petukai_q0009_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "centurion_petukai_q0009_0105.htm";
            }
        } else if (npcId == TANAPI) {
            if (cond == 1) {
                htmltext = "seer_tanapi_q0009_0101.htm";
            } else if (cond == 2) {
                htmltext = "seer_tanapi_q0009_0202.htm";
            }
        } else if (npcId == TAMIL && cond == 2) {
            htmltext = "gatekeeper_tamil_q0009_0201.htm";
        }
        return htmltext;
    }
}
