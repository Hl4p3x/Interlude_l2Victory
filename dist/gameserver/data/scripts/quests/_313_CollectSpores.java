package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _313_CollectSpores extends Quest {
    public final int Herbiel = 30150;
    public final int SporeFungus = 20509;
    public final int SporeSac = 1118;

    public _313_CollectSpores() {
        super(false);
        addStartNpc(30150);
        addTalkId(30150);
        addKillId(20509);
        addQuestItem(1118);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("green_q0313_05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (cond) {
            case 0:
                if (st.getPlayer().getLevel() >= 8) {
                    htmltext = "green_q0313_03.htm";
                } else {
                    htmltext = "green_q0313_02.htm";
                    st.exitCurrentQuest(true);
                }
                break;
            case 1:
                htmltext = "green_q0313_06.htm";
                break;
            case 2:
                if (st.getQuestItemsCount(1118) < 10L) {
                    st.setCond(1);
                    htmltext = "green_q0313_06.htm";
                } else {
                    st.takeItems(1118, -1L);
                    st.giveItems(57, 3500L, true);
                    st.playSound("ItemSound.quest_finish");
                    htmltext = "green_q0313_07.htm";
                    st.exitCurrentQuest(true);
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 1 && npcId == 20509 && Rnd.chance(70)) {
            st.giveItems(1118, 1L);
            if (st.getQuestItemsCount(1118) < 10L) {
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
                st.setState(2);
            }
        }
        return null;
    }
}
