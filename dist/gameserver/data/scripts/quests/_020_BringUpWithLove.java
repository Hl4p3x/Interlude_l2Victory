package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _020_BringUpWithLove extends Quest {
    private static final int beast_herder_tunatun = 31537;
    private static final int q_jewel_of_innocent = 7185;

    public _020_BringUpWithLove() {
        super(false);
        addStartNpc(31537);
        addTalkId(31537);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("givemelove");
        final int npcId = npc.getNpcId();
        if (npcId == 31537) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("givemelove", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "beast_herder_tunatun_q0020_09.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "beast_herder_tunatun_q0020_03.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "beast_herder_tunatun_q0020_04.htm";
            } else if ("reply_4".equalsIgnoreCase(event)) {
                htmltext = "beast_herder_tunatun_q0020_05.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                htmltext = "beast_herder_tunatun_q0020_06.htm";
            } else if ("reply_5".equalsIgnoreCase(event)) {
                htmltext = "beast_herder_tunatun_q0020_08.htm";
            } else if ("reply_6".equalsIgnoreCase(event) && GetMemoState == 1 && st.getQuestItemsCount(7185) >= 1L) {
                st.takeItems(7185, -1L);
                st.giveItems(57, 68500L);
                st.unset("givemelove");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
                htmltext = "beast_herder_tunatun_q0020_12.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("givemelove");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31537) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 65) {
                    htmltext = "beast_herder_tunatun_q0020_01.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "beast_herder_tunatun_q0020_02.htm";
                break;
            }
            case 2: {
                if (npcId != 31537) {
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(7185) == 0L) {
                    htmltext = "beast_herder_tunatun_q0020_10.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(7185) >= 1L) {
                    htmltext = "beast_herder_tunatun_q0020_11.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }
}
