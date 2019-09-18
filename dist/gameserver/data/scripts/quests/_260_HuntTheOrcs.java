package quests;

import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _260_HuntTheOrcs extends Quest {
    private static final int ORC_AMULET = 1114;
    private static final int ORC_NECKLACE = 1115;

    public _260_HuntTheOrcs() {
        super(false);
        addStartNpc(30221);
        addKillId(20468, 20469, 20470, 20471, 20472, 20473);
        addQuestItem(1114, 1115);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("sentinel_rayjien_q0260_03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("sentinel_rayjien_q0260_06.htm".equals(event)) {
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30221) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 6 && st.getPlayer().getRace() == Race.elf) {
                    htmltext = "sentinel_rayjien_q0260_02.htm";
                    return htmltext;
                }
                if (st.getPlayer().getRace() != Race.elf) {
                    htmltext = "sentinel_rayjien_q0260_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() < 6) {
                    htmltext = "sentinel_rayjien_q0260_01.htm";
                    st.exitCurrentQuest(true);
                } else if (cond == 1 && st.getQuestItemsCount(1114) == 0L && st.getQuestItemsCount(1115) == 0L) {
                    htmltext = "sentinel_rayjien_q0260_04.htm";
                }
            } else if (cond == 1 && (st.getQuestItemsCount(1114) > 0L || st.getQuestItemsCount(1115) > 0L)) {
                htmltext = "sentinel_rayjien_q0260_05.htm";
                int adenaPay = 0;
                if (st.getQuestItemsCount(1114) >= 40L) {
                    adenaPay += (int) (st.getQuestItemsCount(1114) * 14L);
                } else {
                    adenaPay += (int) (st.getQuestItemsCount(1114) * 12L);
                }
                if (st.getQuestItemsCount(1115) >= 40L) {
                    adenaPay += (int) (st.getQuestItemsCount(1115) * 40L);
                } else {
                    adenaPay += (int) (st.getQuestItemsCount(1115) * 30L);
                }
                st.giveItems(57, (long) adenaPay, false);
                st.takeItems(1114, -1L);
                st.takeItems(1115, -1L);
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
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getCond() > 0) {
            if (npcId == 20468 || npcId == 20469 || npcId == 20470) {
                st.rollAndGive(1114, 1, 14.0);
            } else if (npcId == 20471 || npcId == 20472 || npcId == 20473) {
                st.rollAndGive(1115, 1, 14.0);
            }
        }
        return null;
    }
}
