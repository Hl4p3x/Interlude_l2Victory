package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _168_DeliverSupplies extends Quest {
    int JENNIES_LETTER_ID;
    int SENTRY_BLADE1_ID;
    int SENTRY_BLADE2_ID;
    int SENTRY_BLADE3_ID;
    int OLD_BRONZE_SWORD_ID;

    public _168_DeliverSupplies() {
        super(false);
        JENNIES_LETTER_ID = 1153;
        SENTRY_BLADE1_ID = 1154;
        SENTRY_BLADE2_ID = 1155;
        SENTRY_BLADE3_ID = 1156;
        OLD_BRONZE_SWORD_ID = 1157;
        addStartNpc(30349);
        addTalkId(30349);
        addTalkId(30355);
        addTalkId(30357);
        addTalkId(30360);
        addQuestItem(SENTRY_BLADE1_ID, OLD_BRONZE_SWORD_ID, JENNIES_LETTER_ID, SENTRY_BLADE2_ID, SENTRY_BLADE3_ID);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.set("id", "0");
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30349-03.htm";
            st.giveItems(JENNIES_LETTER_ID, 1L);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30349 && cond == 0) {
            if (cond < 15) {
                if (st.getPlayer().getRace() != Race.darkelf) {
                    htmltext = "30349-00.htm";
                } else if (st.getPlayer().getLevel() >= 3) {
                    htmltext = "30349-02.htm";
                } else {
                    htmltext = "30349-01.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30349-01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30349 && cond == 1 && st.getQuestItemsCount(JENNIES_LETTER_ID) > 0L) {
            htmltext = "30349-04.htm";
        } else if (npcId == 30349 && cond == 2 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 1L && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1L && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1L) {
            htmltext = "30349-05.htm";
            st.takeItems(SENTRY_BLADE1_ID, 1L);
            st.setCond(3);
        } else if (npcId == 30349 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0L && (st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1L || st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1L)) {
            htmltext = "30349-07.htm";
        } else if (npcId == 30349 && cond == 4 && st.getQuestItemsCount(OLD_BRONZE_SWORD_ID) == 2L) {
            htmltext = "30349-06.htm";
            st.takeItems(OLD_BRONZE_SWORD_ID, 2L);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.giveItems(57, 820L);
            st.exitCurrentQuest(false);
        } else if (npcId == 30360 && cond == 1 && st.getQuestItemsCount(JENNIES_LETTER_ID) == 1L) {
            htmltext = "30360-01.htm";
            st.takeItems(JENNIES_LETTER_ID, 1L);
            st.giveItems(SENTRY_BLADE1_ID, 1L);
            st.giveItems(SENTRY_BLADE2_ID, 1L);
            st.giveItems(SENTRY_BLADE3_ID, 1L);
            st.setCond(2);
        } else if (npcId == 30360 && (cond == 2 || cond == 3) && st.getQuestItemsCount(SENTRY_BLADE1_ID) + st.getQuestItemsCount(SENTRY_BLADE2_ID) + st.getQuestItemsCount(SENTRY_BLADE3_ID) > 0L) {
            htmltext = "30360-02.htm";
        } else if (npcId == 30355 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1L && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0L) {
            htmltext = "30355-01.htm";
            st.takeItems(SENTRY_BLADE2_ID, 1L);
            st.giveItems(OLD_BRONZE_SWORD_ID, 1L);
            if (st.getQuestItemsCount(SENTRY_BLADE3_ID) == 0L) {
                st.setCond(4);
            }
        } else if (npcId == 30355 && (cond == 4 || cond == 3) && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 0L) {
            htmltext = "30355-02.htm";
        } else if (npcId == 30357 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1L && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0L) {
            htmltext = "30357-01.htm";
            st.takeItems(SENTRY_BLADE3_ID, 1L);
            st.giveItems(OLD_BRONZE_SWORD_ID, 1L);
            if (st.getQuestItemsCount(SENTRY_BLADE2_ID) == 0L) {
                st.setCond(4);
            }
        } else if (npcId == 30357 && (cond == 4 || cond == 5) && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 0L) {
            htmltext = "30357-02.htm";
        }
        return htmltext;
    }
}
