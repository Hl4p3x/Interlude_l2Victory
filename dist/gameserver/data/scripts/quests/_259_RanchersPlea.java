package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _259_RanchersPlea extends Quest {
    private static final int GIANT_SPIDER_SKIN_ID = 1495;
    private static final int HEALING_POTION_ID = 1061;
    private static final int WOODEN_ARROW_ID = 17;
    private static final int SSNG_ID = 1835;
    private static final int SPSSNG_ID = 2509;

    public _259_RanchersPlea() {
        super(false);
        addStartNpc(30497);
        addTalkId(30405);
        addKillId(20103, 20106, 20108);
        addQuestItem(1495);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "1":
                st.set("id", "0");
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "edmond_q0259_03.htm";
                break;
            case "30497_1":
                htmltext = "edmond_q0259_06.htm";
                st.setCond(0);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
            case "30497_2":
                htmltext = "edmond_q0259_07.htm";
                break;
            case "30405_1":
                htmltext = "marius_q0259_03.htm";
                break;
            case "30405_8a":
                htmltext = "marius_q0259_05b.htm";
                break;
            case "30405_2":
                htmltext = "marius_q0259_04.htm";
                st.giveItems(1061, 2L, false);
                st.takeItems(1495, 10L);
                break;
            case "30405_3":
                htmltext = "marius_q0259_05.htm";
                st.giveItems(17, 50L, false);
                st.takeItems(1495, 10L);
                break;
            case "30405_8":
                htmltext = "marius_q0259_05a.htm";
                st.giveItems(1835, 60L, false);
                st.takeItems(1495, 10L);
                break;
            case "30405_9":
                htmltext = "marius_q0259_05c.htm";
                st.giveItems(2509, 30L, false);
                st.takeItems(1495, 10L);
                break;
            case "30405_9a":
                htmltext = "marius_q0259_05d.htm";
                break;
            case "30405_4":
                if (st.getQuestItemsCount(1495) >= 10L) {
                    htmltext = "marius_q0259_06.htm";
                } else if (st.getQuestItemsCount(1495) < 10L) {
                    htmltext = "marius_q0259_07.htm";
                }
                break;
            default:
                htmltext = "marius_q0259_05a.htm";
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        if (npcId == 30497 && st.getCond() == 0) {
            if (st.getCond() < 15) {
                if (st.getPlayer().getLevel() >= 15) {
                    htmltext = "edmond_q0259_02.htm";
                    return htmltext;
                }
                htmltext = "edmond_q0259_01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "edmond_q0259_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30497 && st.getCond() == 1 && st.getQuestItemsCount(1495) < 1L) {
            htmltext = "edmond_q0259_04.htm";
        } else if (npcId == 30497 && st.getCond() == 1 && st.getQuestItemsCount(1495) >= 1L) {
            htmltext = "edmond_q0259_05.htm";
            st.giveItems(57, st.getQuestItemsCount(1495) * 25L, false);
            st.takeItems(1495, st.getQuestItemsCount(1495));
        } else if (npcId == 30405 && st.getCond() == 1 && st.getQuestItemsCount(1495) < 10L) {
            htmltext = "marius_q0259_01.htm";
        } else if (npcId == 30405 && st.getCond() == 1 && st.getQuestItemsCount(1495) >= 10L) {
            htmltext = "marius_q0259_02.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() > 0) {
            st.rollAndGive(1495, 1, 100.0);
        }
        return null;
    }
}
