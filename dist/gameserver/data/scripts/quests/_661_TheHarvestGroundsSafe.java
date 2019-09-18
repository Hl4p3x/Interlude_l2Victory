package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _661_TheHarvestGroundsSafe extends Quest {
    private static final int NORMAN = 30210;
    private static final int GIANT_POISON_BEE = 21095;
    private static final int CLOYDY_BEAST = 21096;
    private static final int YOUNG_ARANEID = 21097;
    private static final int STING_OF_GIANT_POISON = 8283;
    private static final int TALON_OF_YOUNG_ARANEID = 8285;
    private static final int CLOUDY_GEM = 8284;

    public _661_TheHarvestGroundsSafe() {
        super(false);
        addStartNpc(NORMAN);
        addKillId(GIANT_POISON_BEE);
        addKillId(CLOYDY_BEAST);
        addKillId(YOUNG_ARANEID);
        addQuestItem(STING_OF_GIANT_POISON);
        addQuestItem(TALON_OF_YOUNG_ARANEID);
        addQuestItem(CLOUDY_GEM);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("warehouse_keeper_norman_q0661_0103.htm".equalsIgnoreCase(event) || "warehouse_keeper_norman_q0661_0201.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("warehouse_keeper_norman_q0661_0205.htm".equalsIgnoreCase(event)) {
            final long STING = st.getQuestItemsCount(STING_OF_GIANT_POISON);
            final long TALON = st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID);
            final long GEM = st.getQuestItemsCount(CLOUDY_GEM);
            if (STING + GEM + TALON >= 10L) {
                st.giveItems(57, STING * 57L + GEM * 56L + TALON * 60L + 2800L);
                st.takeItems(STING_OF_GIANT_POISON, -1L);
                st.takeItems(TALON_OF_YOUNG_ARANEID, -1L);
                st.takeItems(CLOUDY_GEM, -1L);
            } else {
                st.giveItems(57, STING * 57L + GEM * 56L + TALON * 60L);
                st.takeItems(STING_OF_GIANT_POISON, -1L);
                st.takeItems(TALON_OF_YOUNG_ARANEID, -1L);
                st.takeItems(CLOUDY_GEM, -1L);
            }
            st.playSound("ItemSound.quest_middle");
        } else if ("warehouse_keeper_norman_q0661_0204.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 21) {
                htmltext = "warehouse_keeper_norman_q0661_0101.htm";
            } else {
                htmltext = "warehouse_keeper_norman_q0661_0102.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1) {
            if (st.getQuestItemsCount(STING_OF_GIANT_POISON) + st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID) + st.getQuestItemsCount(CLOUDY_GEM) > 0L) {
                htmltext = "warehouse_keeper_norman_q0661_0105.htm";
            } else {
                htmltext = "warehouse_keeper_norman_q0661_0206.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if (st.getCond() == 1) {
            if (npcId == GIANT_POISON_BEE && Rnd.chance(75)) {
                st.giveItems(STING_OF_GIANT_POISON, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            if (npcId == CLOYDY_BEAST && Rnd.chance(71)) {
                st.giveItems(CLOUDY_GEM, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            if (npcId == YOUNG_ARANEID && Rnd.chance(67)) {
                st.giveItems(TALON_OF_YOUNG_ARANEID, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
