package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _431_WeddingMarch extends Quest {
    private static final int MELODY_MAESTRO_KANTABILON = 31042;
    private static final int SILVER_CRYSTAL = 7540;
    private static final int WEDDING_ECHO_CRYSTAL = 7062;

    public _431_WeddingMarch() {
        super(false);
        addStartNpc(MELODY_MAESTRO_KANTABILON);
        addKillId(20786);
        addKillId(20787);
        addQuestItem(SILVER_CRYSTAL);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "muzyk_q0431_0104.htm";
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("431_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(SILVER_CRYSTAL) == 50L) {
                htmltext = "muzyk_q0431_0201.htm";
                st.takeItems(SILVER_CRYSTAL, -1L);
                st.giveItems(WEDDING_ECHO_CRYSTAL, 25L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "muzyk_q0431_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int condition = st.getCond();
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        if (npcId == MELODY_MAESTRO_KANTABILON) {
            if (id != 2) {
                if (st.getPlayer().getLevel() < 38) {
                    htmltext = "muzyk_q0431_0103.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "muzyk_q0431_0101.htm";
                }
            } else if (condition == 1) {
                htmltext = "muzyk_q0431_0106.htm";
            } else if (condition == 2 && st.getQuestItemsCount(SILVER_CRYSTAL) == 50L) {
                htmltext = "muzyk_q0431_0105.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if ((npcId == 20786 || npcId == 20787) && st.getCond() == 1 && st.getQuestItemsCount(SILVER_CRYSTAL) < 50L) {
            st.giveItems(SILVER_CRYSTAL, 1L);
            if (st.getQuestItemsCount(SILVER_CRYSTAL) == 50L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
