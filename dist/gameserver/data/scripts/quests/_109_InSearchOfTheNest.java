package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _109_InSearchOfTheNest extends Quest {
    private static final int PIERCE = 31553;
    private static final int CORPSE = 32015;
    private static final int KAHMAN = 31554;
    private static final int MEMO = 8083;
    private static final int GOLDEN_BADGE_RECRUIT = 7246;
    private static final int GOLDEN_BADGE_SOLDIER = 7247;

    public _109_InSearchOfTheNest() {
        super(false);
        addStartNpc(31553);
        addTalkId(32015);
        addTalkId(31554);
        addQuestItem(8083);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("Memo".equalsIgnoreCase(event) && cond == 1) {
            st.giveItems(8083, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_itemget");
            htmltext = "You've find something...";
        } else if ("merc_cap_peace_q0109_0301.htm".equalsIgnoreCase(event) && cond == 2) {
            st.takeItems(8083, -1L);
            st.setCond(3);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        if (id == 3) {
            return "completed";
        }
        final int cond = st.getCond();
        String htmltext = "noquest";
        if (id == 1) {
            if (st.getPlayer().getLevel() >= 66 && npcId == 31553 && (st.getQuestItemsCount(7246) > 0L || st.getQuestItemsCount(7247) > 0L)) {
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.setCond(1);
                htmltext = "merc_cap_peace_q0109_0105.htm";
            } else {
                htmltext = "merc_cap_peace_q0109_0103.htm";
                st.exitCurrentQuest(true);
            }
        } else if (id == 2) {
            if (npcId == 32015) {
                if (cond == 1) {
                    htmltext = "corpse_of_scout_q0109_0101.htm";
                } else if (cond == 2) {
                    htmltext = "corpse_of_scout_q0109_0203.htm";
                }
            } else if (npcId == 31553) {
                switch (cond) {
                    case 1:
                        htmltext = "merc_cap_peace_q0109_0304.htm";
                        break;
                    case 2:
                        htmltext = "merc_cap_peace_q0109_0201.htm";
                        break;
                    case 3:
                        htmltext = "merc_cap_peace_q0109_0303.htm";
                        break;
                }
            } else if (npcId == 31554 && cond == 3) {
                htmltext = "merc_kahmun_q0109_0401.htm";
                st.giveItems(57, 5168L);
                st.exitCurrentQuest(false);
                st.playSound("ItemSound.quest_finish");
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        return null;
    }
}
