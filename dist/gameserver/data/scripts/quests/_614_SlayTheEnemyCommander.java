package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _614_SlayTheEnemyCommander extends Quest {
    private static final int DURAI = 31377;
    private static final int KETRAS_COMMANDER_TAYR = 25302;
    private static final int MARK_OF_VARKA_ALLIANCE1 = 7221;
    private static final int MARK_OF_VARKA_ALLIANCE2 = 7222;
    private static final int MARK_OF_VARKA_ALLIANCE3 = 7223;
    private static final int MARK_OF_VARKA_ALLIANCE4 = 7224;
    private static final int MARK_OF_VARKA_ALLIANCE5 = 7225;
    private static final int HEAD_OF_TAYR = 7241;
    private static final int FEATHER_OF_WISDOM = 7230;

    public _614_SlayTheEnemyCommander() {
        super(true);
        addStartNpc(31377);
        addKillId(25302);
        addQuestItem(7241);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "elder_ashas_barka_durai_q0614_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("614_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7241) >= 1L) {
                htmltext = "elder_ashas_barka_durai_q0614_0201.htm";
                st.takeItems(7241, -1L);
                st.giveItems(7230, 1L);
                st.addExpAndSp(0L, 10000L);
                st.unset("cond");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 75) {
                if (st.getQuestItemsCount(7224) == 1L || st.getQuestItemsCount(7225) == 1L) {
                    htmltext = "elder_ashas_barka_durai_q0614_0101.htm";
                } else {
                    htmltext = "elder_ashas_barka_durai_q0614_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "elder_ashas_barka_durai_q0614_0103.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 && st.getQuestItemsCount(7241) == 0L) {
            htmltext = "elder_ashas_barka_durai_q0614_0106.htm";
        } else if (cond == 2 && st.getQuestItemsCount(7241) >= 1L) {
            htmltext = "elder_ashas_barka_durai_q0614_0105.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            st.giveItems(7241, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
