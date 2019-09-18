package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _033_MakeAPairOfDressShoes extends Quest {
    int LEATHER;
    int THREAD;
    int DRESS_SHOES_BOX;

    public _033_MakeAPairOfDressShoes() {
        super(false);
        LEATHER = 1882;
        THREAD = 1868;
        DRESS_SHOES_BOX = 7113;
        addStartNpc(30838);
        addTalkId(30838);
        addTalkId(30838);
        addTalkId(30164);
        addTalkId(31520);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "30838-1.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "31520-1.htm":
                st.setCond(2);
                break;
            case "30838-3.htm":
                st.setCond(3);
                break;
            case "30838-5.htm":
                if (st.getQuestItemsCount(LEATHER) >= 200L && st.getQuestItemsCount(THREAD) >= 600L && st.getQuestItemsCount(57) >= 200000L) {
                    st.takeItems(LEATHER, 200L);
                    st.takeItems(THREAD, 600L);
                    st.takeItems(57, 200000L);
                    st.setCond(4);
                } else {
                    htmltext = "You don't have enough materials";
                }
                break;
            case "30164-1.htm":
                if (st.getQuestItemsCount(57) >= 300000L) {
                    st.takeItems(57, 300000L);
                    st.setCond(5);
                } else {
                    htmltext = "30164-havent.htm";
                }
                break;
            case "30838-7.htm":
                st.giveItems(DRESS_SHOES_BOX, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case 30838:
                if (cond == 0 && st.getQuestItemsCount(DRESS_SHOES_BOX) == 0L) {
                    if (st.getPlayer().getLevel() >= 60) {
                        final QuestState fwear = st.getPlayer().getQuestState(_037_PleaseMakeMeFormalWear.class);
                        if (fwear != null && fwear.getCond() == 7) {
                            htmltext = "30838-0.htm";
                        } else {
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "30838-00.htm";
                    }
                } else if (cond == 1) {
                    htmltext = "30838-1.htm";
                } else if (cond == 2) {
                    htmltext = "30838-2.htm";
                } else if (cond == 3 && st.getQuestItemsCount(LEATHER) >= 200L && st.getQuestItemsCount(THREAD) >= 600L && st.getQuestItemsCount(57) >= 200000L) {
                    htmltext = "30838-4.htm";
                } else if (cond == 3 && (st.getQuestItemsCount(LEATHER) < 200L || st.getQuestItemsCount(THREAD) < 600L || st.getQuestItemsCount(57) < 200000L)) {
                    htmltext = "30838-4r.htm";
                } else if (cond == 4) {
                    htmltext = "30838-5r.htm";
                } else if (cond == 5) {
                    htmltext = "30838-6.htm";
                }
                break;
            case 31520:
                if (cond == 1) {
                    htmltext = "31520-0.htm";
                } else if (cond == 2) {
                    htmltext = "31520-1r.htm";
                }
                break;
            case 30164:
                if (cond == 4) {
                    htmltext = "30164-0.htm";
                } else if (cond == 5) {
                    htmltext = "30164-2.htm";
                }
                break;
        }
        return htmltext;
    }
}
