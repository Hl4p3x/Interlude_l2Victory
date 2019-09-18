package quests;

import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _351_BlackSwan extends Quest {
    private static final int Gosta = 30916;
    private static final int Heine = 30969;
    private static final int Ferris = 30847;
    private static final int ORDER_OF_GOSTA = 4296;
    private static final int LIZARD_FANG = 4297;
    private static final int BARREL_OF_LEAGUE = 4298;
    private static final int BILL_OF_IASON_HEINE = 4310;
    private static final int CHANCE = 100;
    private static final int CHANCE2 = 5;

    public _351_BlackSwan() {
        super(false);
        addStartNpc(30916);
        addTalkId(30969);
        addTalkId(30847);
        addKillId(20784, 20785, 21639, 21640, 21642, 21643);
        addQuestItem(4296, 4297, 4298);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final long amount = st.getQuestItemsCount(4297);
        final long amount2 = st.getQuestItemsCount(4298);
        if ("30916-03.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.giveItems(4296, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("30969-02a.htm".equalsIgnoreCase(event) && amount > 0L) {
            htmltext = "30969-02.htm";
            st.giveItems(57, amount * 30L, false);
            st.takeItems(4297, -1L);
        } else if ("30969-03a.htm".equalsIgnoreCase(event) && amount2 > 0L) {
            htmltext = "30969-03.htm";
            st.setCond(2);
            st.giveItems(57, amount2 * 500L, false);
            st.giveItems(4310, amount2, false);
            st.takeItems(4298, -1L);
        } else if ("30969-01.htm".equalsIgnoreCase(event) && st.getCond() == 2) {
            htmltext = "30969-04.htm";
        } else if ("5".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
            htmltext = "";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30916) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 32) {
                    htmltext = "30916-01.htm";
                } else {
                    htmltext = "30916-00.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond >= 1) {
                htmltext = "30916-04.htm";
            }
        }
        if (npcId == 30969) {
            if (cond == 1) {
                htmltext = "30969-01.htm";
            }
            if (cond == 2) {
                htmltext = "30969-04.htm";
            }
        }
        if (npcId == 30847) {
            htmltext = "30847.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final double mod = Experience.penaltyModifier((long) st.calculateLevelDiffForDrop(npc.getLevel(), st.getPlayer().getLevel()), 9.0);
        st.rollAndGive(4297, 1, 100.0 * mod);
        st.rollAndGive(4298, 1, 5.0 * mod);
        return null;
    }
}
