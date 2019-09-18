package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _261_CollectorsDream extends Quest {
    int GIANT_SPIDER_LEG;

    public _261_CollectorsDream() {
        super(false);
        GIANT_SPIDER_LEG = 1087;
        addStartNpc(30222);
        addTalkId(30222);
        addKillId(20308);
        addKillId(20460);
        addKillId(20466);
        addQuestItem(GIANT_SPIDER_LEG);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("moneylender_alshupes_q0261_03.htm".equalsIgnoreCase(event.intern())) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 15) {
                htmltext = "moneylender_alshupes_q0261_02.htm";
                return htmltext;
            }
            htmltext = "moneylender_alshupes_q0261_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1 || st.getQuestItemsCount(GIANT_SPIDER_LEG) < 8L) {
            htmltext = "moneylender_alshupes_q0261_04.htm";
        } else if (cond == 2 && st.getQuestItemsCount(GIANT_SPIDER_LEG) >= 8L) {
            st.takeItems(GIANT_SPIDER_LEG, -1L);
            st.giveItems(57, 1000L);
            st.addExpAndSp(2000L, 0L);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4")) {
                st.getPlayer().setVar("p1q4", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
            }
            htmltext = "moneylender_alshupes_q0261_05.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(GIANT_SPIDER_LEG) < 8L) {
            st.giveItems(GIANT_SPIDER_LEG, 1L);
            if (st.getQuestItemsCount(GIANT_SPIDER_LEG) == 8L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
