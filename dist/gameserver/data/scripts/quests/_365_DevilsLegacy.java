package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _365_DevilsLegacy extends Quest {
    private static final int RANDOLF = 30095;
    private static final int CHANCE_OF_DROP = 25;
    private static final int REWARD_PER_ONE = 5070;
    private static final int TREASURE_CHEST = 5873;
    int[] MOBS;

    public _365_DevilsLegacy() {
        super(false);
        MOBS = new int[]{20836, 29027, 20845, 21629, 21630, 29026};
        addStartNpc(30095);
        addKillId(MOBS);
        addQuestItem(5873);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30095-1.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30095-5.htm".equalsIgnoreCase(event)) {
            final long count = st.getQuestItemsCount(5873);
            if (count > 0L) {
                final long reward = count * 5070L;
                st.takeItems(5873, -1L);
                st.giveItems(57, reward);
            } else {
                htmltext = "You don't have required items";
            }
        } else if ("30095-6.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 39) {
                htmltext = "30095-0.htm";
            } else {
                htmltext = "30095-0a.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1) {
            if (st.getQuestItemsCount(5873) == 0L) {
                htmltext = "30095-2.htm";
            } else {
                htmltext = "30095-4.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (Rnd.chance(25)) {
            st.giveItems(5873, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
