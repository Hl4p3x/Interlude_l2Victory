package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _360_PlunderTheirSupplies extends Quest {
    private static final int COLEMAN = 30873;
    private static final int TAIK_SEEKER = 20666;
    private static final int TAIK_LEADER = 20669;
    private static final int SUPPLY_ITEM = 5872;
    private static final int SUSPICIOUS_DOCUMENT = 5871;
    private static final int RECIPE_OF_SUPPLY = 5870;
    private static final int ITEM_DROP_SEEKER = 50;
    private static final int ITEM_DROP_LEADER = 65;
    private static final int DOCUMENT_DROP = 5;

    public _360_PlunderTheirSupplies() {
        super(false);
        addStartNpc(30873);
        addKillId(20666);
        addKillId(20669);
        addQuestItem(5872);
        addQuestItem(5871);
        addQuestItem(5870);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("guard_coleman_q0360_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("guard_coleman_q0360_10.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int id = st.getState();
        final long docs = st.getQuestItemsCount(5870);
        final long supplies = st.getQuestItemsCount(5872);
        if (id != 2) {
            if (st.getPlayer().getLevel() >= 52) {
                htmltext = "guard_coleman_q0360_02.htm";
            } else {
                htmltext = "guard_coleman_q0360_01.htm";
            }
        } else if (docs > 0L || supplies > 0L) {
            final long reward = 6000L + supplies * 100L + docs * 6000L;
            st.takeItems(5872, -1L);
            st.takeItems(5870, -1L);
            st.giveItems(57, reward);
            htmltext = "guard_coleman_q0360_08.htm";
        } else {
            htmltext = "guard_coleman_q0360_05.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if ((npcId == 20666 && Rnd.chance(50)) || (npcId == 20669 && Rnd.chance(65))) {
            st.giveItems(5872, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        if (Rnd.chance(5)) {
            if (st.getQuestItemsCount(5871) < 4L) {
                st.giveItems(5871, 1L);
            } else {
                st.takeItems(5871, -1L);
                st.giveItems(5870, 1L);
            }
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
