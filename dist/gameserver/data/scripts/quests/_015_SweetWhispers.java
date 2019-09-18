package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _015_SweetWhispers extends Quest {
    public _015_SweetWhispers() {
        super(false);
        addStartNpc(31302);
        addTalkId(31517);
        addTalkId(31518);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("trader_vladimir_q0015_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("dark_necromancer_q0015_0201.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
        } else if ("dark_presbyter_q0015_0301.htm".equalsIgnoreCase(event)) {
            st.addExpAndSp(88000L, 0L);
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
        if (npcId == 31302) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 60) {
                    htmltext = "trader_vladimir_q0015_0101.htm";
                } else {
                    htmltext = "trader_vladimir_q0015_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond >= 1) {
                htmltext = "trader_vladimir_q0015_0105.htm";
            }
        } else if (npcId == 31518) {
            if (cond == 1) {
                htmltext = "dark_necromancer_q0015_0101.htm";
            } else if (cond == 2) {
                htmltext = "dark_necromancer_q0015_0202.htm";
            }
        } else if (npcId == 31517 && cond == 2) {
            htmltext = "dark_presbyter_q0015_0201.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        return null;
    }
}
