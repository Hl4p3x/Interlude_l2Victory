package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _654_JourneytoaSettlement extends Quest {
    private static final int printessa_spirit = 31453;
    private static final int valley_antelope = 21294;
    private static final int antelope_slave = 21295;
    private static final int q_antelope_leather = 8072;
    private static final int q_ticket_to_frintessa = 8073;

    public _654_JourneytoaSettlement() {
        super(false);
        addStartNpc(31453);
        addKillId(21294, 21295);
        addQuestItem(8072);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("to_reach_an_ending");
        final int npcId = npc.getNpcId();
        if (npcId == 31453) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("to_reach_an_ending", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "printessa_spirit_q0654_03.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                if (GetMemoState == 1) {
                    st.setCond(2);
                    st.set("to_reach_an_ending", String.valueOf(2), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "printessa_spirit_q0654_04.htm";
                }
            } else if ("reply_2".equalsIgnoreCase(event) && GetMemoState == 2 && st.getQuestItemsCount(8072) >= 1L) {
                st.giveItems(8073, 1L);
                st.takeItems(8072, -1L);
                st.unset("to_reach_an_ending");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "printessa_spirit_q0654_07.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final QuestState qs = st.getPlayer().getQuestState(_119_LastImperialPrince.class);
        final int GetMemoState = st.getInt("to_reach_an_ending");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31453) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 74 && qs != null && qs.isCompleted()) {
                    htmltext = "printessa_spirit_q0654_01.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "printessa_spirit_q0654_02.htm";
                break;
            }
            case 2: {
                if (npcId != 31453) {
                    break;
                }
                if (GetMemoState == 1) {
                    st.setCond(2);
                    st.set("to_reach_an_ending", String.valueOf(2), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "printessa_spirit_q0654_04.htm";
                    break;
                }
                if (GetMemoState == 2 && st.getQuestItemsCount(8072) == 0L) {
                    htmltext = "printessa_spirit_q0654_05.htm";
                    break;
                }
                if (GetMemoState == 2 && st.getQuestItemsCount(8072) >= 1L) {
                    htmltext = "printessa_spirit_q0654_06.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("to_reach_an_ending");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 2 && st.getQuestItemsCount(8072) == 0L) {
            if (npcId == 21294) {
                if (Rnd.get(100) < 84) {
                    st.setCond(3);
                    st.giveItems(8072, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            } else if (npcId == 21295 && Rnd.get(1000) < 893) {
                st.setCond(3);
                st.giveItems(8072, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
