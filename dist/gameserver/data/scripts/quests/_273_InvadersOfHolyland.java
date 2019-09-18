package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _273_InvadersOfHolyland extends Quest {
    public final int BLACK_SOULSTONE = 1475;
    public final int RED_SOULSTONE = 1476;

    public _273_InvadersOfHolyland() {
        super(false);
        addStartNpc(30566);
        addKillId(20311, 20312, 20313);
        addQuestItem(1475, 1476);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "atuba_chief_varkees_q0273_03.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "atuba_chief_varkees_q0273_07.htm":
                st.setCond(0);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
            case "atuba_chief_varkees_q0273_08.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getRace() != Race.orc) {
                htmltext = "atuba_chief_varkees_q0273_00.htm";
                st.exitCurrentQuest(true);
            } else {
                if (st.getPlayer().getLevel() >= 6) {
                    htmltext = "atuba_chief_varkees_q0273_02.htm";
                    return htmltext;
                }
                htmltext = "atuba_chief_varkees_q0273_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond > 0) {
            if (st.getQuestItemsCount(1475) == 0L && st.getQuestItemsCount(1476) == 0L) {
                htmltext = "atuba_chief_varkees_q0273_04.htm";
            } else {
                long adena = 0L;
                if (st.getQuestItemsCount(1475) > 0L) {
                    htmltext = "atuba_chief_varkees_q0273_05.htm";
                    adena += st.getQuestItemsCount(1475) * 5L;
                }
                if (st.getQuestItemsCount(1476) > 0L) {
                    htmltext = "atuba_chief_varkees_q0273_06.htm";
                    adena += st.getQuestItemsCount(1476) * 50L;
                }
                st.takeAllItems(1475, 1476);
                st.giveItems(57, adena);
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
                }
                st.exitCurrentQuest(true);
                st.playSound("ItemSound.quest_finish");
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20311) {
            if (cond == 1) {
                if (Rnd.chance(90)) {
                    st.giveItems(1475, 1L);
                } else {
                    st.giveItems(1476, 1L);
                }
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20312) {
            if (cond == 1) {
                if (Rnd.chance(87)) {
                    st.giveItems(1475, 1L);
                } else {
                    st.giveItems(1476, 1L);
                }
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == 20313 && cond == 1) {
            if (Rnd.chance(77)) {
                st.giveItems(1475, 1L);
            } else {
                st.giveItems(1476, 1L);
            }
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
