package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _618_IntoTheFlame extends Quest {
    private static final int KLEIN = 31540;
    private static final int HILDA = 31271;
    private static final int VACUALITE_ORE = 7265;
    private static final int VACUALITE = 7266;
    private static final int FLOATING_STONE = 7267;
    private static final int CHANCE_FOR_QUEST_ITEMS = 50;

    public _618_IntoTheFlame() {
        super(true);
        addStartNpc(31540);
        addTalkId(31271);
        addKillId(21274, 21275, 21276, 21278);
        addKillId(21282, 21283, 21284, 21286);
        addKillId(21290, 21291, 21292, 21294);
        addQuestItem(7265);
        addQuestItem(7266);
        addQuestItem(7267);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("watcher_valakas_klein_q0618_0104.htm".equalsIgnoreCase(event) && cond == 0) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("watcher_valakas_klein_q0618_0401.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7266) > 0L && cond == 4) {
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                st.giveItems(7267, 1L);
            } else {
                htmltext = "watcher_valakas_klein_q0618_0104.htm";
            }
        } else if ("blacksmith_hilda_q0618_0201.htm".equalsIgnoreCase(event) && cond == 1) {
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("blacksmith_hilda_q0618_0301.htm".equalsIgnoreCase(event)) {
            if (cond == 3 && st.getQuestItemsCount(7265) == 50L) {
                st.takeItems(7265, -1L);
                st.giveItems(7266, 1L);
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "blacksmith_hilda_q0618_0203.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31540) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 60) {
                    htmltext = "watcher_valakas_klein_q0618_0103.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "watcher_valakas_klein_q0618_0101.htm";
                }
            } else if (cond == 4 && st.getQuestItemsCount(7266) > 0L) {
                htmltext = "watcher_valakas_klein_q0618_0301.htm";
            } else {
                htmltext = "watcher_valakas_klein_q0618_0104.htm";
            }
        } else if (npcId == 31271) {
            if (cond == 1) {
                htmltext = "blacksmith_hilda_q0618_0101.htm";
            } else if (cond == 3 && st.getQuestItemsCount(7265) >= 50L) {
                htmltext = "blacksmith_hilda_q0618_0202.htm";
            } else if (cond == 4) {
                htmltext = "blacksmith_hilda_q0618_0303.htm";
            } else {
                htmltext = "blacksmith_hilda_q0618_0203.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final long count = st.getQuestItemsCount(7265);
        if (Rnd.chance(50) && count < 50L) {
            st.giveItems(7265, 1L);
            if (count == 49L) {
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
