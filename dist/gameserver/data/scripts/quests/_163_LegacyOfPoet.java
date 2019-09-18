package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _163_LegacyOfPoet extends Quest {
    int RUMIELS_POEM_1_ID;
    int RUMIELS_POEM_3_ID;
    int RUMIELS_POEM_4_ID;
    int RUMIELS_POEM_5_ID;

    public _163_LegacyOfPoet() {
        super(false);
        RUMIELS_POEM_1_ID = 1038;
        RUMIELS_POEM_3_ID = 1039;
        RUMIELS_POEM_4_ID = 1040;
        RUMIELS_POEM_5_ID = 1041;
        addStartNpc(30220);
        addTalkId(30220);
        addTalkId(30220);
        addKillId(20372);
        addKillId(20373);
        addQuestItem(RUMIELS_POEM_1_ID, RUMIELS_POEM_3_ID, RUMIELS_POEM_4_ID, RUMIELS_POEM_5_ID);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.set("id", "0");
            htmltext = "30220-07.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.setCond(0);
            st.set("id", "0");
        }
        if (npcId == 30220 && st.getCond() == 0) {
            if (st.getCond() < 15) {
                if (st.getPlayer().getRace() == Race.darkelf) {
                    htmltext = "30220-00.htm";
                } else {
                    if (st.getPlayer().getLevel() >= 11) {
                        htmltext = "30220-03.htm";
                        return htmltext;
                    }
                    htmltext = "30220-02.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30220-02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30220 && st.getCond() == 0) {
            htmltext = "completed";
        } else if (npcId == 30220 && st.getCond() > 0) {
            if (st.getQuestItemsCount(RUMIELS_POEM_1_ID) == 1L && st.getQuestItemsCount(RUMIELS_POEM_3_ID) == 1L && st.getQuestItemsCount(RUMIELS_POEM_4_ID) == 1L && st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 1L) {
                if (st.getInt("id") != 163) {
                    st.set("id", "163");
                    htmltext = "30220-09.htm";
                    st.takeItems(RUMIELS_POEM_1_ID, 1L);
                    st.takeItems(RUMIELS_POEM_3_ID, 1L);
                    st.takeItems(RUMIELS_POEM_4_ID, 1L);
                    st.takeItems(RUMIELS_POEM_5_ID, 1L);
                    st.giveItems(57, 13890L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
            } else {
                htmltext = "30220-08.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == 20372 || npcId == 20373) {
            st.set("id", "0");
            if (st.getCond() == 1) {
                if (Rnd.chance(10) && st.getQuestItemsCount(RUMIELS_POEM_1_ID) == 0L) {
                    st.giveItems(RUMIELS_POEM_1_ID, 1L);
                    if (st.getQuestItemsCount(RUMIELS_POEM_1_ID) + st.getQuestItemsCount(RUMIELS_POEM_3_ID) + st.getQuestItemsCount(RUMIELS_POEM_4_ID) + st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 4L) {
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                if (Rnd.chance(70) && st.getQuestItemsCount(RUMIELS_POEM_3_ID) == 0L) {
                    st.giveItems(RUMIELS_POEM_3_ID, 1L);
                    if (st.getQuestItemsCount(RUMIELS_POEM_1_ID) + st.getQuestItemsCount(RUMIELS_POEM_3_ID) + st.getQuestItemsCount(RUMIELS_POEM_4_ID) + st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 4L) {
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                if (Rnd.chance(70) && st.getQuestItemsCount(RUMIELS_POEM_4_ID) == 0L) {
                    st.giveItems(RUMIELS_POEM_4_ID, 1L);
                    if (st.getQuestItemsCount(RUMIELS_POEM_1_ID) + st.getQuestItemsCount(RUMIELS_POEM_3_ID) + st.getQuestItemsCount(RUMIELS_POEM_4_ID) + st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 4L) {
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                if (Rnd.chance(50) && st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 0L) {
                    st.giveItems(RUMIELS_POEM_5_ID, 1L);
                    if (st.getQuestItemsCount(RUMIELS_POEM_1_ID) + st.getQuestItemsCount(RUMIELS_POEM_3_ID) + st.getQuestItemsCount(RUMIELS_POEM_4_ID) + st.getQuestItemsCount(RUMIELS_POEM_5_ID) == 4L) {
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        }
        return null;
    }
}
