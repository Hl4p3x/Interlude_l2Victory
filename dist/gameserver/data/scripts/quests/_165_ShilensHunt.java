package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _165_ShilensHunt extends Quest {
    private static final int DARK_BEZOAR = 1160;
    private static final int LESSER_HEALING_POTION = 1060;

    public _165_ShilensHunt() {
        super(false);
        addStartNpc(30348);
        addTalkId(30348);
        addKillId(20456);
        addKillId(20529);
        addKillId(20532);
        addKillId(20536);
        addQuestItem(1160);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30348-03.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getRace() != Race.darkelf) {
                htmltext = "30348-00.htm";
            } else {
                if (st.getPlayer().getLevel() >= 3) {
                    htmltext = "30348-02.htm";
                    return htmltext;
                }
                htmltext = "30348-01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 || st.getQuestItemsCount(1160) < 13L) {
            htmltext = "30348-04.htm";
        } else if (cond == 2) {
            htmltext = "30348-05.htm";
            st.takeItems(1160, -1L);
            st.giveItems(1060, 5L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 1 && st.getQuestItemsCount(1160) < 13L && Rnd.chance(90)) {
            st.giveItems(1160, 1L);
            if (st.getQuestItemsCount(1160) == 13L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
