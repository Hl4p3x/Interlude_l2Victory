package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _258_BringWolfPelts extends Quest {
    int WOLF_PELT;
    int Cotton_Shirt;
    int Leather_Pants;
    int Leather_Shirt;
    int Short_Leather_Gloves;
    int Tunic;

    public _258_BringWolfPelts() {
        super(false);
        WOLF_PELT = 702;
        Cotton_Shirt = 390;
        Leather_Pants = 29;
        Leather_Shirt = 22;
        Short_Leather_Gloves = 1119;
        Tunic = 426;
        addStartNpc(30001);
        addKillId(20120);
        addKillId(20442);
        addQuestItem(WOLF_PELT);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("lector_q0258_03.htm".equalsIgnoreCase(event.intern())) {
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
            if (st.getPlayer().getLevel() >= 3) {
                htmltext = "lector_q0258_02.htm";
                return htmltext;
            }
            htmltext = "lector_q0258_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1 && st.getQuestItemsCount(WOLF_PELT) >= 0L && st.getQuestItemsCount(WOLF_PELT) < 40L) {
            htmltext = "lector_q0258_05.htm";
        } else if (cond == 2 && st.getQuestItemsCount(WOLF_PELT) >= 40L) {
            st.takeItems(WOLF_PELT, 40L);
            final int n = Rnd.get(16);
            if (n == 0) {
                st.giveItems(Cotton_Shirt, 1L);
                st.playSound("ItemSound.quest_jackpot");
            } else if (n < 6) {
                st.giveItems(Leather_Pants, 1L);
            } else if (n < 9) {
                st.giveItems(Leather_Shirt, 1L);
            } else if (n < 13) {
                st.giveItems(Short_Leather_Gloves, 1L);
            } else {
                st.giveItems(Tunic, 1L);
            }
            htmltext = "lector_q0258_06.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final long count = st.getQuestItemsCount(WOLF_PELT);
        if (count < 40L && st.getCond() == 1) {
            st.giveItems(WOLF_PELT, 1L);
            if (count == 39L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
