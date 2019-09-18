package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _037_PleaseMakeMeFormalWear extends Quest {
    private static final int MYSTERIOUS_CLOTH = 7076;
    private static final int JEWEL_BOX = 7077;
    private static final int SEWING_KIT = 7078;
    private static final int DRESS_SHOES_BOX = 7113;
    private static final int SIGNET_RING = 7164;
    private static final int ICE_WINE = 7160;
    private static final int BOX_OF_COOKIES = 7159;

    public _037_PleaseMakeMeFormalWear() {
        super(false);
        addStartNpc(30842);
        addTalkId(30842);
        addTalkId(31520);
        addTalkId(31521);
        addTalkId(31627);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "30842-1.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "31520-1.htm":
                st.giveItems(7164, 1L);
                st.setCond(2);
                break;
            case "31521-1.htm":
                st.takeItems(7164, 1L);
                st.giveItems(7160, 1L);
                st.setCond(3);
                break;
            case "31627-1.htm":
                if (st.getQuestItemsCount(7160) > 0L) {
                    st.takeItems(7160, 1L);
                    st.setCond(4);
                } else {
                    htmltext = "You don't have enough materials";
                }
                break;
            case "31521-3.htm":
                st.giveItems(7159, 1L);
                st.setCond(5);
                break;
            case "31520-3.htm":
                st.takeItems(7159, 1L);
                st.setCond(6);
                break;
            case "31520-5.htm":
                st.takeItems(7076, 1L);
                st.takeItems(7077, 1L);
                st.takeItems(7078, 1L);
                st.setCond(7);
                break;
            case "31520-7.htm":
                if (st.getQuestItemsCount(7113) > 0L) {
                    st.takeItems(7113, 1L);
                    st.giveItems(6408, 1L);
                    st.unset("cond");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                } else {
                    htmltext = "You don't have enough materials";
                }
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
            case 30842:
                if (cond == 0) {
                    if (st.getPlayer().getLevel() >= 60) {
                        htmltext = "30842-0.htm";
                    } else {
                        htmltext = "30842-2.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1) {
                    htmltext = "30842-1.htm";
                }
                break;
            case 31520:
                switch (cond) {
                    case 1:
                        htmltext = "31520-0.htm";
                        break;
                    case 2:
                        htmltext = "31520-1.htm";
                        break;
                    case 5:
                    case 6:
                        if (st.getQuestItemsCount(7076) > 0L && st.getQuestItemsCount(7077) > 0L && st.getQuestItemsCount(7078) > 0L) {
                            htmltext = "31520-4.htm";
                        } else if (st.getQuestItemsCount(7159) > 0L) {
                            htmltext = "31520-2.htm";
                        } else {
                            htmltext = "31520-3.htm";
                        }
                        break;
                    case 7:
                        if (st.getQuestItemsCount(7113) > 0L) {
                            htmltext = "31520-6.htm";
                        } else {
                            htmltext = "31520-5.htm";
                        }
                        break;
                }
                break;
            case 31521:
                if (st.getQuestItemsCount(7164) > 0L) {
                    htmltext = "31521-0.htm";
                } else if (cond == 3) {
                    htmltext = "31521-1.htm";
                } else if (cond == 4) {
                    htmltext = "31521-2.htm";
                } else if (cond == 5) {
                    htmltext = "31521-3.htm";
                }
                break;
            case 31627:
                htmltext = "31627-0.htm";
                break;
        }
        return htmltext;
    }
}
