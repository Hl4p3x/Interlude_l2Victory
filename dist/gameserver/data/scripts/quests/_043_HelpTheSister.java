package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _043_HelpTheSister extends Quest {
    private static final int COOPER = 30829;
    private static final int GALLADUCCI = 30097;
    private static final int CRAFTED_DAGGER = 220;
    private static final int MAP_PIECE = 7550;
    private static final int MAP = 7551;
    private static final int PET_TICKET = 7584;
    private static final int SPECTER = 20171;
    private static final int SORROW_MAIDEN = 20197;
    private static final int MAX_COUNT = 30;

    public _043_HelpTheSister() {
        super(false);
        addStartNpc(30829);
        addTalkId(30097);
        addKillId(20171);
        addKillId(20197);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            htmltext = "pet_manager_cooper_q0043_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("3".equals(event) && st.getQuestItemsCount(220) > 0L) {
            htmltext = "pet_manager_cooper_q0043_0201.htm";
            st.takeItems(220, 1L);
            st.setCond(2);
        } else if ("4".equals(event) && st.getQuestItemsCount(7550) >= 30L) {
            htmltext = "pet_manager_cooper_q0043_0301.htm";
            st.takeItems(7550, 30L);
            st.giveItems(7551, 1L);
            st.setCond(4);
        } else if ("5".equals(event) && st.getQuestItemsCount(7551) > 0L) {
            htmltext = "galladuchi_q0043_0401.htm";
            st.takeItems(7551, 1L);
            st.setCond(5);
        } else if ("7".equals(event)) {
            htmltext = "pet_manager_cooper_q0043_0501.htm";
            st.giveItems(7584, 1L);
            st.setCond(0);
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            if (st.getPlayer().getLevel() >= 26) {
                htmltext = "pet_manager_cooper_q0043_0101.htm";
            } else {
                st.exitCurrentQuest(true);
                htmltext = "pet_manager_cooper_q0043_0103.htm";
            }
        } else if (id == 2) {
            final int cond = st.getCond();
            if (npcId == 30829) {
                switch (cond) {
                    case 1:
                        if (st.getQuestItemsCount(220) == 0L) {
                            htmltext = "pet_manager_cooper_q0043_0106.htm";
                        } else {
                            htmltext = "pet_manager_cooper_q0043_0105.htm";
                        }
                        break;
                    case 2:
                        htmltext = "pet_manager_cooper_q0043_0204.htm";
                        break;
                    case 3:
                        htmltext = "pet_manager_cooper_q0043_0203.htm";
                        break;
                    case 4:
                        htmltext = "pet_manager_cooper_q0043_0303.htm";
                        break;
                    case 5:
                        htmltext = "pet_manager_cooper_q0043_0401.htm";
                        break;
                }
            } else if (npcId == 30097 && cond == 4 && st.getQuestItemsCount(7551) > 0L) {
                htmltext = "galladuchi_q0043_0301.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 2) {
            final long pieces = st.getQuestItemsCount(7550);
            if (pieces < 30L) {
                st.giveItems(7550, 1L);
                if (pieces < 29L) {
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(3);
                }
            }
        }
        return null;
    }
}
