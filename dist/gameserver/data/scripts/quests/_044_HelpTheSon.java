package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _044_HelpTheSon extends Quest {
    private static final int LUNDY = 30827;
    private static final int DRIKUS = 30505;
    private static final int WORK_HAMMER = 168;
    private static final int GEMSTONE_FRAGMENT = 7552;
    private static final int GEMSTONE = 7553;
    private static final int PET_TICKET = 7585;
    private static final int MAILLE_GUARD = 20921;
    private static final int MAILLE_SCOUT = 20920;
    private static final int MAILLE_LIZARDMAN = 20919;

    public _044_HelpTheSon() {
        super(false);
        addStartNpc(30827);
        addTalkId(30505);
        addKillId(20921);
        addKillId(20920);
        addKillId(20919);
        addQuestItem(7552);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            htmltext = "pet_manager_lundy_q0044_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("3".equals(event) && st.getQuestItemsCount(168) > 0L) {
            htmltext = "pet_manager_lundy_q0044_0201.htm";
            st.takeItems(168, 1L);
            st.setCond(2);
        } else if ("4".equals(event) && st.getQuestItemsCount(7552) >= 30L) {
            htmltext = "pet_manager_lundy_q0044_0301.htm";
            st.takeItems(7552, -1L);
            st.giveItems(7553, 1L);
            st.setCond(4);
        } else if ("5".equals(event) && st.getQuestItemsCount(7553) > 0L) {
            htmltext = "high_prefect_drikus_q0044_0401.htm";
            st.takeItems(7553, 1L);
            st.setCond(5);
        } else if ("7".equals(event)) {
            htmltext = "pet_manager_lundy_q0044_0501.htm";
            st.giveItems(7585, 1L);
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
            if (st.getPlayer().getLevel() >= 24) {
                htmltext = "pet_manager_lundy_q0044_0101.htm";
            } else {
                st.exitCurrentQuest(true);
                htmltext = "pet_manager_lundy_q0044_0103.htm";
            }
        } else if (id == 2) {
            final int cond = st.getCond();
            if (npcId == 30827) {
                switch (cond) {
                    case 1:
                        if (st.getQuestItemsCount(168) == 0L) {
                            htmltext = "pet_manager_lundy_q0044_0106.htm";
                        } else {
                            htmltext = "pet_manager_lundy_q0044_0105.htm";
                        }
                        break;
                    case 2:
                        htmltext = "pet_manager_lundy_q0044_0204.htm";
                        break;
                    case 3:
                        htmltext = "pet_manager_lundy_q0044_0203.htm";
                        break;
                    case 4:
                        htmltext = "pet_manager_lundy_q0044_0303.htm";
                        break;
                    case 5:
                        htmltext = "pet_manager_lundy_q0044_0401.htm";
                        break;
                }
            } else if (npcId == 30505) {
                if (cond == 4 && st.getQuestItemsCount(7553) > 0L) {
                    htmltext = "high_prefect_drikus_q0044_0301.htm";
                } else if (cond == 5) {
                    htmltext = "high_prefect_drikus_q0044_0403.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 2 && st.getQuestItemsCount(7552) < 30L) {
            st.giveItems(7552, 1L);
            if (st.getQuestItemsCount(7552) >= 30L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(3);
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
