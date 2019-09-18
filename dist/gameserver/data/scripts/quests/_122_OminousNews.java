package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _122_OminousNews extends Quest {
    int MOIRA;
    int KARUDA;

    public _122_OminousNews() {
        super(false);
        MOIRA = 31979;
        KARUDA = 32017;
        addStartNpc(MOIRA);
        addTalkId(KARUDA);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        String htmltext = event;
        if ("seer_moirase_q0122_0104.htm".equalsIgnoreCase(htmltext) && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("karuda_q0122_0201.htm".equalsIgnoreCase(htmltext)) {
            if (cond == 1) {
                st.giveItems(57, 1695L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            } else {
                htmltext = "noquest";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == MOIRA) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 20) {
                    htmltext = "seer_moirase_q0122_0101.htm";
                } else {
                    htmltext = "seer_moirase_q0122_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "seer_moirase_q0122_0104.htm";
            }
        } else if (npcId == KARUDA && cond == 1) {
            htmltext = "karuda_q0122_0101.htm";
        }
        return htmltext;
    }
}
