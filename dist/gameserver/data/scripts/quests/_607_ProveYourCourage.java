package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _607_ProveYourCourage extends Quest {
    private static final int KADUN_ZU_KETRA = 31370;
    private static final int VARKAS_HERO_SHADITH = 25309;
    private static final int HEAD_OF_SHADITH = 7235;
    private static final int TOTEM_OF_VALOR = 7219;
    private static final int MARK_OF_KETRA_ALLIANCE1 = 7211;
    private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
    private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
    private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
    private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;

    public _607_ProveYourCourage() {
        super(true);
        addStartNpc(31370);
        addKillId(25309);
        addQuestItem(7235);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equals(event)) {
            htmltext = "elder_kadun_zu_ketra_q0607_0104.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("607_3".equals(event)) {
            if (st.getQuestItemsCount(7235) >= 1L) {
                htmltext = "elder_kadun_zu_ketra_q0607_0201.htm";
                st.takeItems(7235, -1L);
                st.giveItems(7219, 1L);
                st.addExpAndSp(0L, 10000L);
                st.unset("cond");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            } else {
                htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
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
                if (st.getQuestItemsCount(7213) == 1L || st.getQuestItemsCount(7214) == 1L || st.getQuestItemsCount(7215) == 1L) {
                    htmltext = "elder_kadun_zu_ketra_q0607_0101.htm";
                } else {
                    htmltext = "elder_kadun_zu_ketra_q0607_0102.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "elder_kadun_zu_ketra_q0607_0103.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 && st.getQuestItemsCount(7235) == 0L) {
            htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
        } else if (cond == 2 && st.getQuestItemsCount(7235) >= 1L) {
            htmltext = "elder_kadun_zu_ketra_q0607_0105.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == 25309 && st.getCond() == 1) {
            st.giveItems(7235, 1L);
            st.setCond(2);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
