package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _331_ArrowForVengeance extends Quest {
    private static final int HARPY_FEATHER = 1452;
    private static final int MEDUSA_VENOM = 1453;
    private static final int WYRMS_TOOTH = 1454;

    public _331_ArrowForVengeance() {
        super(false);
        addStartNpc(30125);
        addKillId(20145, 20158, 20176);
        addQuestItem(1452, 1453, 1454);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("beltkem_q0331_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("beltkem_q0331_06.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 32) {
                htmltext = "beltkem_q0331_02.htm";
                return htmltext;
            }
            htmltext = "beltkem_q0331_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1) {
            if (st.getQuestItemsCount(1452) + st.getQuestItemsCount(1453) + st.getQuestItemsCount(1454) > 0L) {
                st.giveItems(57, 80L * st.getQuestItemsCount(1452) + 90L * st.getQuestItemsCount(1453) + 100L * st.getQuestItemsCount(1454), false);
                st.takeItems(1452, -1L);
                st.takeItems(1453, -1L);
                st.takeItems(1454, -1L);
                htmltext = "beltkem_q0331_05.htm";
            } else {
                htmltext = "beltkem_q0331_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() > 0) {
            switch (npc.getNpcId()) {
                case 20145: {
                    st.rollAndGive(1452, 1, 33.0);
                    break;
                }
                case 20158: {
                    st.rollAndGive(1453, 1, 33.0);
                    break;
                }
                case 20176: {
                    st.rollAndGive(1454, 1, 33.0);
                    break;
                }
            }
        }
        return null;
    }
}
