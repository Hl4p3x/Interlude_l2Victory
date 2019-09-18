package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _368_TrespassingIntoTheSacredArea extends Quest {
    private static final int RESTINA = 30926;
    private static final int BLADE_STAKATO_FANG = 5881;
    private static final int BLADE_STAKATO_FANG_BASECHANCE = 10;

    public _368_TrespassingIntoTheSacredArea() {
        super(false);
        addStartNpc(RESTINA);
        for (int Blade_Stakato_id = 20794; Blade_Stakato_id <= 20797; ++Blade_Stakato_id) {
            addKillId(Blade_Stakato_id);
        }
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != RESTINA) {
            return htmltext;
        }
        if (st.getState() == 1) {
            if (st.getPlayer().getLevel() < 36) {
                htmltext = "30926-00.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30926-01.htm";
                st.setCond(0);
            }
        } else {
            final long _count = st.getQuestItemsCount(BLADE_STAKATO_FANG);
            if (_count > 0L) {
                htmltext = "30926-04.htm";
                st.takeItems(BLADE_STAKATO_FANG, -1L);
                st.giveItems(57, _count * 2250L);
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "30926-03.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("30926-02.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30926-05.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (Rnd.chance(npc.getNpcId() - 20794 + BLADE_STAKATO_FANG_BASECHANCE)) {
            qs.giveItems(BLADE_STAKATO_FANG, 1L);
            qs.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
