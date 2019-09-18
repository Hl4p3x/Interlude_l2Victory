package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _636_TruthBeyond extends Quest {
    private static final int priest_eliyah = 31329;
    private static final int falsepriest_flauron = 32010;
    private static final int q_mark_of_heresy = 8067;
    private static final int q_mark_of_sacrifice = 8064;
    private static final int q_faded_mark_of_sac = 8065;

    public _636_TruthBeyond() {
        super(false);
        addStartNpc(31329);
        addTalkId(32010);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 31329) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("truth_behind_the_door", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "priest_eliyah_q0636_05.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "priest_eliyah_q0636_04.htm";
            }
        } else if (npcId == 32010 && "reply_1".equalsIgnoreCase(event)) {
            st.giveItems(8064, 1L);
            st.unset("truth_behind_the_door");
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            htmltext = "falsepriest_flauron_q0636_02.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("truth_behind_the_door");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31329) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 73 && st.getQuestItemsCount(8064) == 0L && st.getQuestItemsCount(8065) == 0L && st.getQuestItemsCount(8067) == 0L) {
                    htmltext = "priest_eliyah_q0636_01.htm";
                    break;
                }
                if (st.getPlayer().getLevel() >= 73 && (st.getQuestItemsCount(8064) >= 1L || st.getQuestItemsCount(8065) >= 1L || st.getQuestItemsCount(8067) >= 1L)) {
                    htmltext = "priest_eliyah_q0636_02.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() < 73) {
                    htmltext = "priest_eliyah_q0636_03.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                break;
            }
            case 2: {
                if (npcId == 31329) {
                    if (GetMemoState == 1) {
                        htmltext = "priest_eliyah_q0636_06.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId == 32010 && GetMemoState == 1) {
                        htmltext = "falsepriest_flauron_q0636_01.htm";
                        break;
                    }
                    break;
                }
            }
        }
        return htmltext;
    }
}
