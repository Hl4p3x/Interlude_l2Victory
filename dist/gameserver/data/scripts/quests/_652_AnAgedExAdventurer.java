package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _652_AnAgedExAdventurer extends Quest {
    private static final int Tantan = 32012;
    private static final int Sara = 30180;
    private static final int SoulshotCgrade = 1464;
    private static final int ScrollEnchantArmorD = 956;

    public _652_AnAgedExAdventurer() {
        super(false);
        addStartNpc(32012);
        addTalkId(30180);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext;
        if ("retired_oldman_tantan_q0652_03.htm".equalsIgnoreCase(event) && st.getQuestItemsCount(1464) >= 100L) {
            st.setCond(1);
            st.setState(2);
            st.takeItems(1464, 100L);
            st.playSound("ItemSound.quest_accept");
            htmltext = "retired_oldman_tantan_q0652_04.htm";
        } else {
            htmltext = "retired_oldman_tantan_q0652_03.htm";
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_giveup");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 32012) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 46) {
                    htmltext = "retired_oldman_tantan_q0652_01a.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "retired_oldman_tantan_q0652_01.htm";
                }
            }
        } else if (npcId == 30180 && cond == 1) {
            htmltext = "sara_q0652_01.htm";
            st.giveItems(57, 5026L, true);
            if (Rnd.chance(50)) {
                st.giveItems(956, 1L, false);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }
}
