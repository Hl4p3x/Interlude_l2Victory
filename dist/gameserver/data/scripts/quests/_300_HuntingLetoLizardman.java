package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.stream.IntStream;

public class _300_HuntingLetoLizardman extends Quest {
    private static final int RATH = 30126;
    private static final int BRACELET_OF_LIZARDMAN = 7139;
    private static final int ANIMAL_BONE = 1872;
    private static final int ANIMAL_SKIN = 1867;
    private static final int BRACELET_OF_LIZARDMAN_CHANCE = 70;

    public _300_HuntingLetoLizardman() {
        super(false);
        addStartNpc(RATH);
        IntStream.rangeClosed(20577, 20582).forEach(this::addKillId);
        addQuestItem(BRACELET_OF_LIZARDMAN);
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != RATH) {
            return htmltext;
        }
        if (st.getState() == 1) {
            if (st.getPlayer().getLevel() < 34) {
                htmltext = "rarshints_q0300_0103.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "rarshints_q0300_0101.htm";
                st.setCond(0);
            }
        } else if (st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60L) {
            htmltext = "rarshints_q0300_0106.htm";
            st.setCond(1);
        } else {
            htmltext = "rarshints_q0300_0105.htm";
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int _state = st.getState();
        if ("rarshints_q0300_0104.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("rarshints_q0300_0201.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60L) {
                htmltext = "rarshints_q0300_0202.htm";
                st.setCond(1);
            } else {
                st.takeItems(BRACELET_OF_LIZARDMAN, -1L);
                switch (Rnd.get(3)) {
                    case 0: {
                        st.giveItems(57, 30000L, true);
                        break;
                    }
                    case 1: {
                        st.giveItems(ANIMAL_BONE, 50L, true);
                        break;
                    }
                    case 2: {
                        st.giveItems(ANIMAL_SKIN, 50L, true);
                        break;
                    }
                }
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final long _count = qs.getQuestItemsCount(BRACELET_OF_LIZARDMAN);
        if (_count < 60L && Rnd.chance(BRACELET_OF_LIZARDMAN_CHANCE)) {
            qs.giveItems(BRACELET_OF_LIZARDMAN, 1L);
            if (_count == 59L) {
                qs.setCond(2);
                qs.playSound("ItemSound.quest_middle");
            } else {
                qs.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }

    
}
