package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _632_NecromancersRequest extends Quest {
    private static final int WIZARD = 31522;
    private static final int V_HEART = 7542;
    private static final int Z_BRAIN = 7543;
    private static final int ADENA_AMOUNT = 120000;
    private static final int[] VAMPIRES = {21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595};
    private static final int[] UNDEADS = {21547, 21548, 21549, 21550, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579};

    public _632_NecromancersRequest() {
        super(true);
        addStartNpc(31522);
        addKillId(_632_NecromancersRequest.VAMPIRES);
        addKillId(_632_NecromancersRequest.UNDEADS);
        addQuestItem(7542, 7543);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "632_4":
                st.playSound("ItemSound.quest_finish");
                htmltext = "shadow_hardin_q0632_0204.htm";
                st.exitCurrentQuest(true);
                break;
            case "632_1":
                htmltext = "shadow_hardin_q0632_0104.htm";
                break;
            case "632_3":
                if (st.getCond() == 2 && st.getQuestItemsCount(7542) > 199L) {
                    st.takeItems(7542, 200L);
                    st.giveItems(57, 120000L, true);
                    st.playSound("ItemSound.quest_finish");
                    st.setCond(1);
                    htmltext = "shadow_hardin_q0632_0202.htm";
                }
                break;
            case "quest_accept":
                if (st.getPlayer().getLevel() > 62) {
                    htmltext = "shadow_hardin_q0632_0104.htm";
                    st.setCond(1);
                    st.setState(2);
                    st.playSound("ItemSound.quest_accept");
                } else {
                    htmltext = "shadow_hardin_q0632_0103.htm";
                    st.exitCurrentQuest(true);
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 0 && npcId == 31522) {
            htmltext = "shadow_hardin_q0632_0101.htm";
        }
        if (cond == 1) {
            htmltext = "shadow_hardin_q0632_0202.htm";
        }
        if (cond == 2 && st.getQuestItemsCount(7542) > 199L) {
            htmltext = "shadow_hardin_q0632_0105.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        for (final int i : _632_NecromancersRequest.VAMPIRES) {
            if (i == npc.getNpcId()) {
                if (st.getCond() < 2 && Rnd.chance(50)) {
                    st.rollAndGive(7542, 1, 100.0);
                    if (st.getQuestItemsCount(7542) > 199L) {
                        st.setCond(2);
                    }
                }
                return null;
            }
        }
        st.rollAndGive(7543, 1, 33.0);
        return null;
    }
}
