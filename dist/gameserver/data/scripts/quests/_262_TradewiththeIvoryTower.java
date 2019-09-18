package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _262_TradewiththeIvoryTower extends Quest {
    public final int VOLODOS = 30137;
    public final int GREEN_FUNGUS = 20007;
    public final int BLOOD_FUNGUS = 20400;
    public final int FUNGUS_SAC = 707;

    public _262_TradewiththeIvoryTower() {
        super(false);
        addStartNpc(30137);
        addKillId(20400, 20007);
        addQuestItem(707);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("vollodos_q0262_03.htm".equals(event)) {
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
            if (st.getPlayer().getLevel() >= 8) {
                htmltext = "vollodos_q0262_02.htm";
                return htmltext;
            }
            htmltext = "vollodos_q0262_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1 && st.getQuestItemsCount(707) < 10L) {
            htmltext = "vollodos_q0262_04.htm";
        } else if (cond == 2 && st.getQuestItemsCount(707) >= 10L) {
            st.giveItems(57, 3000L);
            st.takeItems(707, -1L);
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            htmltext = "vollodos_q0262_05.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int random = Rnd.get(10);
        if (st.getCond() == 1 && st.getQuestItemsCount(707) < 10L && ((npcId == 20007 && random < 3) || (npcId == 20400 && random < 4))) {
            st.giveItems(707, 1L);
            if (st.getQuestItemsCount(707) == 10L) {
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
