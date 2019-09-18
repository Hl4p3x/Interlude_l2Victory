package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _688_DefeatTheElrokianRaiders extends Quest {
    private static final int DROP_CHANCE = 50;
    private static final int DINOSAUR_FANG_NECKLACE = 8785;

    public _688_DefeatTheElrokianRaiders() {
        super(false);
        addStartNpc(32105);
        addTalkId(32105);
        addKillId(22214);
        addQuestItem(DINOSAUR_FANG_NECKLACE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
        if ("32105-03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("32105-08.htm".equalsIgnoreCase(event)) {
            if (count > 0L) {
                st.takeItems(DINOSAUR_FANG_NECKLACE, -1L);
                st.giveItems(57, count * 3000L);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("32105-06.htm".equalsIgnoreCase(event)) {
            st.takeItems(DINOSAUR_FANG_NECKLACE, -1L);
            st.giveItems(57, count * 3000L);
        } else if ("32105-07.htm".equalsIgnoreCase(event)) {
            if (count >= 100L) {
                st.takeItems(DINOSAUR_FANG_NECKLACE, 100L);
                st.giveItems(57, 450000L);
            } else {
                htmltext = "32105-04.htm";
            }
        } else if ("None".equalsIgnoreCase(event)) {
            htmltext = null;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        final long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 75) {
                htmltext = "32105-01.htm";
            } else {
                htmltext = "32105-00.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1) {
            if (count == 0L) {
                htmltext = "32105-04.htm";
            } else {
                htmltext = "32105-05.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
        if (st.getCond() == 1 && count < 100L && Rnd.chance(DROP_CHANCE)) {
            long numItems = (int) Config.RATE_QUESTS_REWARD;
            if (count + numItems > 100L) {
                numItems = 100L - count;
            }
            if (count + numItems >= 100L) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
            st.giveItems(DINOSAUR_FANG_NECKLACE, numItems);
        }
        return null;
    }
}
