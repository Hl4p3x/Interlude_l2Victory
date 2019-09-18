package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _629_CleanUpTheSwampOfScreams extends Quest {
    private static final int CAPTAIN = 31553;
    private static final int CLAWS = 7250;
    private static final int COIN = 7251;
    private static final int[][] CHANCE = {{21508, 50}, {21509, 43}, {21510, 52}, {21511, 57}, {21512, 74}, {21513, 53}, {21514, 53}, {21515, 54}, {21516, 55}, {21517, 56}};

    public _629_CleanUpTheSwampOfScreams() {
        super(false);
        addStartNpc(CAPTAIN);
        for (int npcId = 21508; npcId < 21518; ++npcId) {
            addKillId(npcId);
        }
        addQuestItem(CLAWS);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("merc_cap_peace_q0629_0104.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("merc_cap_peace_q0629_0202.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(CLAWS) >= 100L) {
                st.takeItems(CLAWS, 100L);
                st.giveItems(COIN, 20L, false);
            } else {
                htmltext = "merc_cap_peace_q0629_0203.htm";
            }
        } else if ("merc_cap_peace_q0629_0204.htm".equalsIgnoreCase(event)) {
            st.takeItems(CLAWS, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (st.getQuestItemsCount(7246) > 0L || st.getQuestItemsCount(7247) > 0L) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 66) {
                    htmltext = "merc_cap_peace_q0629_0101.htm";
                } else {
                    htmltext = "merc_cap_peace_q0629_0103.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (st.getState() == 2) {
                if (st.getQuestItemsCount(CLAWS) >= 100L) {
                    htmltext = "merc_cap_peace_q0629_0105.htm";
                } else {
                    htmltext = "merc_cap_peace_q0629_0106.htm";
                }
            }
        } else {
            htmltext = "merc_cap_peace_q0629_0205.htm";
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() == 2) {
            st.rollAndGive(CLAWS, 1, (double) CHANCE[npc.getNpcId() - 21508][1]);
        }
        return null;
    }
}
