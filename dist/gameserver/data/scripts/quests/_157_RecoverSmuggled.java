package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _157_RecoverSmuggled extends Quest {
    private static final int wilph = 30005;
    private static final int giant_toad = 20121;
    private static final int adamantite_ore = 1024;
    private static final int buckler = 20;

    public _157_RecoverSmuggled() {
        super(false);
        addStartNpc(30005);
        addTalkId(30005);
        addKillId(20121);
        addQuestItem(1024);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equals(event)) {
            st.setCond(1);
            st.set("recover_smuggled", String.valueOf(1), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "wilph_q0157_05.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("recover_smuggled");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30005) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 5) {
                    htmltext = "wilph_q0157_03.htm";
                    break;
                }
                htmltext = "wilph_q0157_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 30005 || GetMemoState != 1) {
                    break;
                }
                if (st.getQuestItemsCount(1024) < 20L) {
                    htmltext = "wilph_q0157_06.htm";
                    break;
                }
                if (st.getQuestItemsCount(1024) >= 20L) {
                    st.takeItems(1024, -1L);
                    st.giveItems(20, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.unset("recover_smuggled");
                    htmltext = "wilph_q0157_07.htm";
                    st.exitCurrentQuest(false);
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int GetMemoState = st.getInt("recover_smuggled");
        if (npcId == 20121 && GetMemoState == 1 && st.getQuestItemsCount(1024) < 20L && Rnd.get(10) < 4) {
            st.giveItems(1024, 1L);
            if (st.getQuestItemsCount(1024) >= 20L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
