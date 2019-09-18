package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _298_LizardmensConspiracy extends Quest {
    public final int PRAGA = 30333;
    public final int ROHMER = 30344;
    public final int MAILLE_LIZARDMAN_WARRIOR = 20922;
    public final int MAILLE_LIZARDMAN_SHAMAN = 20923;
    public final int MAILLE_LIZARDMAN_MATRIARCH = 20924;
    public final int POISON_ARANEID = 20926;
    public final int KING_OF_THE_ARANEID = 20927;
    public final int REPORT = 7182;
    public final int SHINING_GEM = 7183;
    public final int SHINING_RED_GEM = 7184;
    public final int[][] MobsTable;

    public _298_LizardmensConspiracy() {
        super(false);
        MobsTable = new int[][]{{20922, 7183}, {20923, 7183}, {20924, 7183}, {20926, 7184}, {20927, 7184}};
        addStartNpc(30333);
        addTalkId(30333);
        addTalkId(30344);
        for (final int[] element : MobsTable) {
            addKillId(element[0]);
        }
        addQuestItem(7182, 7183, 7184);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("guard_praga_q0298_0104.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.giveItems(7182, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("magister_rohmer_q0298_0201.htm".equalsIgnoreCase(event)) {
            st.takeItems(7182, -1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("magister_rohmer_q0298_0301.htm".equalsIgnoreCase(event) && st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99L) {
            st.takeItems(7183, -1L);
            st.takeItems(7184, -1L);
            st.addExpAndSp(0L, 42000L);
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30333) {
            if (cond < 1) {
                if (st.getPlayer().getLevel() < 25) {
                    htmltext = "guard_praga_q0298_0102.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "guard_praga_q0298_0101.htm";
                }
            }
            if (cond == 1) {
                htmltext = "guard_praga_q0298_0105.htm";
            }
        } else if (npcId == 30344) {
            if (cond < 1) {
                htmltext = "magister_rohmer_q0298_0202.htm";
            } else if (cond == 1) {
                htmltext = "magister_rohmer_q0298_0101.htm";
            } else if (cond == 2 | st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) < 100L) {
                htmltext = "magister_rohmer_q0298_0204.htm";
                st.setCond(2);
            } else if (cond == 3 && st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99L) {
                htmltext = "magister_rohmer_q0298_0203.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int rand = Rnd.get(10);
        if (st.getCond() == 2) {
            for (final int[] element : MobsTable) {
                if (npcId == element[0] && rand < 6 && st.getQuestItemsCount(element[1]) < 50L) {
                    if (rand < 2 && element[1] == 7183) {
                        st.giveItems(element[1], 2L);
                    } else {
                        st.giveItems(element[1], 1L);
                    }
                    if (st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99L) {
                        st.setCond(3);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        }
        return null;
    }
}
