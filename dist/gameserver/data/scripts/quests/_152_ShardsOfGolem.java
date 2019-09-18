package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _152_ShardsOfGolem extends Quest {
    int HARRYS_RECEIPT1;
    int HARRYS_RECEIPT2;
    int GOLEM_SHARD;
    int TOOL_BOX;
    int WOODEN_BP;

    public _152_ShardsOfGolem() {
        super(false);
        HARRYS_RECEIPT1 = 1008;
        HARRYS_RECEIPT2 = 1009;
        GOLEM_SHARD = 1010;
        TOOL_BOX = 1011;
        WOODEN_BP = 23;
        addStartNpc(30035);
        addTalkId(30035);
        addTalkId(30035);
        addTalkId(30283);
        addTalkId(30035);
        addKillId(20016);
        addKillId(20101);
        addQuestItem(HARRYS_RECEIPT1, GOLEM_SHARD, TOOL_BOX, HARRYS_RECEIPT2);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30035-04.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(HARRYS_RECEIPT1) == 0L) {
                st.giveItems(HARRYS_RECEIPT1, 1L);
            }
        } else if ("152_2".equals(event)) {
            st.takeItems(HARRYS_RECEIPT1, -1L);
            if (st.getQuestItemsCount(HARRYS_RECEIPT2) == 0L) {
                st.giveItems(HARRYS_RECEIPT2, 1L);
                st.setCond(2);
            }
            htmltext = "30283-02.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30035) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 10) {
                    htmltext = "30035-03.htm";
                    return htmltext;
                }
                htmltext = "30035-02.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 1 && st.getQuestItemsCount(HARRYS_RECEIPT1) != 0L) {
                htmltext = "30035-05.htm";
            } else if (cond == 2 && st.getQuestItemsCount(HARRYS_RECEIPT2) != 0L) {
                htmltext = "30035-05.htm";
            } else if (cond == 4 && st.getQuestItemsCount(TOOL_BOX) != 0L) {
                st.takeItems(TOOL_BOX, -1L);
                st.takeItems(HARRYS_RECEIPT2, -1L);
                st.setCond(0);
                st.playSound("ItemSound.quest_finish");
                st.giveItems(WOODEN_BP, 1L);
                st.addExpAndSp(5000L, 0L);
                htmltext = "30035-06.htm";
                st.exitCurrentQuest(false);
            }
        } else if (npcId == 30283) {
            if (cond == 1 && st.getQuestItemsCount(HARRYS_RECEIPT1) != 0L) {
                htmltext = "30283-01.htm";
            } else if (cond == 2 && st.getQuestItemsCount(HARRYS_RECEIPT2) != 0L && st.getQuestItemsCount(GOLEM_SHARD) < 5L) {
                htmltext = "30283-03.htm";
            } else if (cond == 3 && st.getQuestItemsCount(HARRYS_RECEIPT2) != 0L && st.getQuestItemsCount(GOLEM_SHARD) == 5L) {
                st.takeItems(GOLEM_SHARD, -1L);
                if (st.getQuestItemsCount(TOOL_BOX) == 0L) {
                    st.giveItems(TOOL_BOX, 1L);
                    st.setCond(4);
                }
                htmltext = "30283-04.htm";
            }
        } else if (cond == 4 && st.getQuestItemsCount(HARRYS_RECEIPT2) != 0L && st.getQuestItemsCount(TOOL_BOX) != 0L) {
            htmltext = "30283-05.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 2 && Rnd.chance(30) && st.getQuestItemsCount(GOLEM_SHARD) < 5L) {
            st.giveItems(GOLEM_SHARD, 1L);
            if (st.getQuestItemsCount(GOLEM_SHARD) == 5L) {
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
