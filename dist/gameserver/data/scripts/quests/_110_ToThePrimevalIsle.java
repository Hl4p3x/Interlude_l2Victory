package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _110_ToThePrimevalIsle extends Quest {
    int ANTON;
    int MARQUEZ;
    int ANCIENT_BOOK;

    public _110_ToThePrimevalIsle() {
        super(false);
        ANTON = 31338;
        MARQUEZ = 32113;
        ANCIENT_BOOK = 8777;
        addStartNpc(ANTON);
        addTalkId(ANTON);
        addTalkId(MARQUEZ);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            htmltext = "scroll_seller_anton_q0110_05.htm";
            st.setCond(1);
            st.giveItems(ANCIENT_BOOK, 1L);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("2".equals(event) && st.getQuestItemsCount(ANCIENT_BOOK) > 0L) {
            htmltext = "marquez_q0110_05.htm";
            st.playSound("ItemSound.quest_finish");
            st.giveItems(57, 169380L);
            st.takeItems(ANCIENT_BOOK, -1L);
            st.exitCurrentQuest(false);
        } else if ("3".equals(event)) {
            htmltext = "marquez_q0110_06.htm";
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            if (st.getPlayer().getLevel() >= 75) {
                htmltext = "scroll_seller_anton_q0110_01.htm";
            } else {
                st.exitCurrentQuest(true);
                htmltext = "scroll_seller_anton_q0110_02.htm";
            }
        } else if (npcId == ANTON) {
            if (cond == 1) {
                htmltext = "scroll_seller_anton_q0110_07.htm";
            }
        } else if (id == 2 && npcId == MARQUEZ && cond == 1) {
            if (st.getQuestItemsCount(ANCIENT_BOOK) == 0L) {
                htmltext = "marquez_q0110_07.htm";
            } else {
                htmltext = "marquez_q0110_01.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        return null;
    }
}
