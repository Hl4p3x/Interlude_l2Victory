package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _624_TheFinestIngredientsPart1 extends Quest {
    private static final int JEREMY = 31521;
    private static final int HOT_SPRINGS_ATROX = 21321;
    private static final int HOT_SPRINGS_NEPENTHES = 21319;
    private static final int HOT_SPRINGS_ATROXSPAWN = 21317;
    private static final int HOT_SPRINGS_BANDERSNATCHLING = 21314;
    private static final int SECRET_SPICE = 7204;
    private static final int TRUNK_OF_NEPENTHES = 7202;
    private static final int FOOT_OF_BANDERSNATCHLING = 7203;
    private static final int CRYOLITE = 7080;
    private static final int SAUCE = 7205;

    public _624_TheFinestIngredientsPart1() {
        super(true);
        addStartNpc(JEREMY);
        addKillId(HOT_SPRINGS_ATROX);
        addKillId(HOT_SPRINGS_NEPENTHES);
        addKillId(HOT_SPRINGS_ATROXSPAWN);
        addKillId(HOT_SPRINGS_BANDERSNATCHLING);
        addQuestItem(TRUNK_OF_NEPENTHES);
        addQuestItem(FOOT_OF_BANDERSNATCHLING);
        addQuestItem(SECRET_SPICE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("jeremy_q0624_0104.htm".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 73) {
                st.setState(2);
                st.setCond(1);
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "jeremy_q0624_0103.htm";
                st.exitCurrentQuest(true);
            }
        } else if ("jeremy_q0624_0201.htm".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) == 50L && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) == 50L && st.getQuestItemsCount(SECRET_SPICE) == 50L) {
                st.takeItems(TRUNK_OF_NEPENTHES, -1L);
                st.takeItems(FOOT_OF_BANDERSNATCHLING, -1L);
                st.takeItems(SECRET_SPICE, -1L);
                st.playSound("ItemSound.quest_finish");
                st.giveItems(SAUCE, 1L);
                st.giveItems(CRYOLITE, 1L);
                htmltext = "jeremy_q0624_0201.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "jeremy_q0624_0202.htm";
                st.setCond(1);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int cond = st.getCond();
        if (cond == 0) {
            htmltext = "jeremy_q0624_0101.htm";
        } else if (cond != 3) {
            htmltext = "jeremy_q0624_0106.htm";
        } else {
            htmltext = "jeremy_q0624_0105.htm";
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
            if (npcId == HOT_SPRINGS_NEPENTHES && st.getQuestItemsCount(TRUNK_OF_NEPENTHES) < 50L) {
                st.rollAndGive(TRUNK_OF_NEPENTHES, 1, 1, 50, 100.0);
            } else if (npcId == HOT_SPRINGS_BANDERSNATCHLING && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) < 50L) {
                st.rollAndGive(FOOT_OF_BANDERSNATCHLING, 1, 1, 50, 100.0);
            } else if ((npcId == HOT_SPRINGS_ATROX || npcId == HOT_SPRINGS_ATROXSPAWN) && st.getQuestItemsCount(SECRET_SPICE) < 50L) {
                st.rollAndGive(SECRET_SPICE, 1, 1, 50, 100.0);
            }
            onKillCheck(st);
        }
        return null;
    }

    private void onKillCheck(final QuestState st) {
        if (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) == 50L && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) == 50L && st.getQuestItemsCount(SECRET_SPICE) == 50L) {
            st.playSound("ItemSound.quest_middle");
            st.setCond(3);
        } else {
            st.playSound("ItemSound.quest_itemget");
        }
    }
}
