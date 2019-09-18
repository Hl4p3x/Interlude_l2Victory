package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _356_DigUpTheSeaOfSpores extends Quest {
    private static final int GAUEN = 30717;
    private static final int SPORE_ZOMBIE = 20562;
    private static final int ROTTING_TREE = 20558;
    private static final int CARNIVORE_SPORE = 5865;
    private static final int HERBIBOROUS_SPORE = 5866;

    public _356_DigUpTheSeaOfSpores() {
        super(false);
        addStartNpc(30717);
        addKillId(20562);
        addKillId(20558);
        addQuestItem(5865);
        addQuestItem(5866);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final long carn = st.getQuestItemsCount(5865);
        final long herb = st.getQuestItemsCount(5866);
        if ("magister_gauen_q0356_06.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 43) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "magister_gauen_q0356_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (("magister_gauen_q0356_20.htm".equalsIgnoreCase(event) || "magister_gauen_q0356_17.htm".equalsIgnoreCase(event)) && carn >= 50L && herb >= 50L) {
            st.takeItems(5865, -1L);
            st.takeItems(5866, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            if ("magister_gauen_q0356_17.htm".equalsIgnoreCase(event)) {
                st.giveItems(57, 20950L);
            } else {
                st.addExpAndSp(45500L, 2600L);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            htmltext = "magister_gauen_q0356_02.htm";
        } else if (cond != 3) {
            htmltext = "magister_gauen_q0356_07.htm";
        } else if (cond == 3) {
            htmltext = "magister_gauen_q0356_10.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final long carn = st.getQuestItemsCount(5865);
        final long herb = st.getQuestItemsCount(5866);
        if (npcId == 20562) {
            if (carn < 50L) {
                st.giveItems(5865, 1L);
                if (carn == 49L) {
                    st.playSound("ItemSound.quest_middle");
                    if (herb >= 50L) {
                        st.setCond(3);
                        st.setState(2);
                    }
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20558 && herb < 50L) {
            st.giveItems(5866, 1L);
            if (herb == 49L) {
                st.playSound("ItemSound.quest_middle");
                if (carn >= 50L) {
                    st.setCond(3);
                    st.setState(2);
                }
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
