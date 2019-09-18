package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _329_CuriosityOfDwarf extends Quest {
    private final int GOLEM_HEARTSTONE = 1346;
    private final int BROKEN_HEARTSTONE = 1365;

    public _329_CuriosityOfDwarf() {
        super(false);
        addStartNpc(30437);
        addKillId(20083);
        addKillId(20085);
        addQuestItem(BROKEN_HEARTSTONE);
        addQuestItem(GOLEM_HEARTSTONE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("trader_rolento_q0329_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("trader_rolento_q0329_06.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int id = st.getState();
        if (id == 1) {
            st.setCond(0);
        }
        String htmltext;
        if (st.getCond() == 0) {
            if (st.getPlayer().getLevel() >= 33) {
                htmltext = "trader_rolento_q0329_02.htm";
            } else {
                htmltext = "trader_rolento_q0329_01.htm";
                st.exitCurrentQuest(true);
            }
        } else {
            final long heart = st.getQuestItemsCount(GOLEM_HEARTSTONE);
            final long broken = st.getQuestItemsCount(BROKEN_HEARTSTONE);
            if (broken + heart > 0L) {
                st.giveItems(57, 50L * broken + 1000L * heart);
                st.takeItems(BROKEN_HEARTSTONE, -1L);
                st.takeItems(GOLEM_HEARTSTONE, -1L);
                htmltext = "trader_rolento_q0329_05.htm";
            } else {
                htmltext = "trader_rolento_q0329_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int n = Rnd.get(1, 100);
        if (npcId == 20085) {
            if (n < 5) {
                st.giveItems(GOLEM_HEARTSTONE, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 58) {
                st.giveItems(BROKEN_HEARTSTONE, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20083) {
            if (n < 6) {
                st.giveItems(GOLEM_HEARTSTONE, 1L);
                st.playSound("ItemSound.quest_itemget");
            } else if (n < 56) {
                st.giveItems(BROKEN_HEARTSTONE, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
