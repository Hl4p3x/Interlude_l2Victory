package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _155_FindSirWindawood extends Quest {
    int OFFICIAL_LETTER;
    int HASTE_POTION;

    public _155_FindSirWindawood() {
        super(false);
        OFFICIAL_LETTER = 1019;
        HASTE_POTION = 734;
        addStartNpc(30042);
        addTalkId(30042);
        addTalkId(30311);
        addQuestItem(OFFICIAL_LETTER);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30042-04.htm".equals(event)) {
            st.giveItems(OFFICIAL_LETTER, 1L);
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30042) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 3) {
                    htmltext = "30042-03.htm";
                    return htmltext;
                }
                htmltext = "30042-02.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 1 && st.getQuestItemsCount(OFFICIAL_LETTER) == 1L) {
                htmltext = "30042-05.htm";
            }
        } else if (npcId == 30311 && cond == 1 && st.getQuestItemsCount(OFFICIAL_LETTER) == 1L) {
            htmltext = "30311-01.htm";
            st.takeItems(OFFICIAL_LETTER, -1L);
            st.giveItems(HASTE_POTION, 1L);
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }
}
