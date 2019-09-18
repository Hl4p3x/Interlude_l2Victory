package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _265_ChainsOfSlavery extends Quest {
    private static final int KRISTIN = 30357;
    private static final int IMP = 20004;
    private static final int IMP_ELDER = 20005;
    private static final int IMP_SHACKLES = 1368;

    public _265_ChainsOfSlavery() {
        super(false);
        addStartNpc(30357);
        addKillId(20004);
        addKillId(20005);
        addQuestItem(1368);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("sentry_krpion_q0265_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("sentry_krpion_q0265_06.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        if (st.getCond() == 0) {
            if (st.getPlayer().getRace() != Race.darkelf) {
                htmltext = "sentry_krpion_q0265_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 6) {
                htmltext = "sentry_krpion_q0265_01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "sentry_krpion_q0265_02.htm";
            }
        } else {
            final long count = st.getQuestItemsCount(1368);
            if (count > 0L) {
                if (count >= 10L) {
                    st.giveItems(57, 13L * count + 500L, true);
                } else {
                    st.giveItems(57, 13L * count, true);
                }
            }
            st.takeItems(1368, -1L);
            htmltext = "sentry_krpion_q0265_05.htm";
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q2")) {
                st.getPlayer().setVar("p1q2", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                final QuestState qs = st.getPlayer().getQuestState(_255_Tutorial.class);
                if (qs != null && qs.getInt("Ex") != 10) {
                    st.showQuestionMark(26);
                    qs.set("Ex", "10");
                    if (st.getPlayer().getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                    } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                    }
                }
            } else {
                htmltext = "sentry_krpion_q0265_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getCond() == 1 && Rnd.chance(5 + npcId - 20004)) {
            st.giveItems(1368, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
