package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _379_FantasyWine extends Quest {
    public final int HARLAN = 30074;
    public final int Enku_Orc_Champion = 20291;
    public final int Enku_Orc_Shaman = 20292;
    public final int LEAF_OF_EUCALYPTUS = 5893;
    public final int STONE_OF_CHILL = 5894;
    public final int[] REWARD;

    public _379_FantasyWine() {
        super(false);
        REWARD = new int[]{5956, 5957, 5958};
        addStartNpc(30074);
        addKillId(20291);
        addKillId(20292);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("hitsran_q0379_06.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("reward".equalsIgnoreCase(event)) {
            st.takeItems(5893, -1L);
            st.takeItems(5894, -1L);
            final int rand = Rnd.get(100);
            if (rand < 25) {
                st.giveItems(REWARD[0], 1L);
                htmltext = "hitsran_q0379_11.htm";
            } else if (rand < 50) {
                st.giveItems(REWARD[1], 1L);
                htmltext = "hitsran_q0379_12.htm";
            } else {
                st.giveItems(REWARD[2], 1L);
                htmltext = "hitsran_q0379_13.htm";
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("hitsran_q0379_05.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30074) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getLevel() < 20) {
                        htmltext = "hitsran_q0379_01.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "hitsran_q0379_02.htm";
                    }
                    break;
                case 1:
                    if (st.getQuestItemsCount(5893) < 80L && st.getQuestItemsCount(5894) < 100L) {
                        htmltext = "hitsran_q0379_07.htm";
                    } else if (st.getQuestItemsCount(5893) == 80L && st.getQuestItemsCount(5894) < 100L) {
                        htmltext = "hitsran_q0379_08.htm";
                    } else if (st.getQuestItemsCount(5893) < 80L && st.getQuestItemsCount(5894) == 100L) {
                        htmltext = "hitsran_q0379_09.htm";
                    } else {
                        htmltext = "hitsran_q0379_02.htm";
                    }
                    break;
                case 2:
                    if (st.getQuestItemsCount(5893) >= 80L && st.getQuestItemsCount(5894) >= 100L) {
                        htmltext = "hitsran_q0379_10.htm";
                    } else {
                        st.setCond(1);
                        if (st.getQuestItemsCount(5893) < 80L && st.getQuestItemsCount(5894) < 100L) {
                            htmltext = "hitsran_q0379_07.htm";
                        } else if (st.getQuestItemsCount(5893) >= 80L && st.getQuestItemsCount(5894) < 100L) {
                            htmltext = "hitsran_q0379_08.htm";
                        } else if (st.getQuestItemsCount(5893) < 80L && st.getQuestItemsCount(5894) >= 100L) {
                            htmltext = "hitsran_q0379_09.htm";
                        }
                    }
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getCond() == 1) {
            if (npcId == 20291 && st.getQuestItemsCount(5893) < 80L) {
                st.giveItems(5893, 1L);
            } else if (npcId == 20292 && st.getQuestItemsCount(5894) < 100L) {
                st.giveItems(5894, 1L);
            }
            if (st.getQuestItemsCount(5893) >= 80L && st.getQuestItemsCount(5894) >= 100L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
