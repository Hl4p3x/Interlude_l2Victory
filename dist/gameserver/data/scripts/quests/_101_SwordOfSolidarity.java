package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _101_SwordOfSolidarity extends Quest {
    int ROIENS_LETTER;
    int HOWTOGO_RUINS;
    int BROKEN_SWORD_HANDLE;
    int BROKEN_BLADE_BOTTOM;
    int BROKEN_BLADE_TOP;
    int ALLTRANS_NOTE;
    int SWORD_OF_SOLIDARITY;

    public _101_SwordOfSolidarity() {
        super(false);
        ROIENS_LETTER = 796;
        HOWTOGO_RUINS = 937;
        BROKEN_SWORD_HANDLE = 739;
        BROKEN_BLADE_BOTTOM = 740;
        BROKEN_BLADE_TOP = 741;
        ALLTRANS_NOTE = 742;
        SWORD_OF_SOLIDARITY = 738;
        addStartNpc(30008);
        addTalkId(30283);
        addKillId(20361);
        addKillId(20362);
        addQuestItem(ALLTRANS_NOTE, HOWTOGO_RUINS, BROKEN_BLADE_TOP, BROKEN_BLADE_BOTTOM, ROIENS_LETTER, BROKEN_SWORD_HANDLE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("roien_q0101_04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(ROIENS_LETTER, 1L);
        } else if ("blacksmith_alltran_q0101_02.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.takeItems(ROIENS_LETTER, -1L);
            st.giveItems(HOWTOGO_RUINS, 1L);
        } else if ("blacksmith_alltran_q0101_07.htm".equalsIgnoreCase(event)) {
            st.takeItems(BROKEN_SWORD_HANDLE, -1L);
            st.giveItems(SWORD_OF_SOLIDARITY, 1L);
            st.giveItems(57, 10981L, false);
            st.getPlayer().addExpAndSp(25747L, 2171L);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3")) {
                st.getPlayer().setVar("p1q3", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                st.giveItems(1060, 100L);
                for (int item = 4412; item <= 4417; ++item) {
                    st.giveItems(item, 10L);
                }
                if (st.getPlayer().getClassId().isMage()) {
                    st.playTutorialVoice("tutorial_voice_027");
                    st.giveItems(5790, 3000L);
                } else {
                    st.playTutorialVoice("tutorial_voice_026");
                    st.giveItems(5789, 6000L);
                }
            }
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
        if (npcId == 30008) {
            if (cond == 0) {
                if (st.getPlayer().getRace() != Race.human) {
                    htmltext = "roien_q0101_00.htm";
                } else {
                    if (st.getPlayer().getLevel() >= 9) {
                        htmltext = "roien_q0101_02.htm";
                        return htmltext;
                    }
                    htmltext = "roien_q0101_08.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(ROIENS_LETTER) == 1L) {
                htmltext = "roien_q0101_05.htm";
            } else if (cond >= 2 && st.getQuestItemsCount(ROIENS_LETTER) == 0L && st.getQuestItemsCount(ALLTRANS_NOTE) == 0L) {
                if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0L && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0L) {
                    htmltext = "roien_q0101_12.htm";
                }
                if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) <= 1L) {
                    htmltext = "roien_q0101_11.htm";
                }
                if (st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0L) {
                    htmltext = "roien_q0101_07.htm";
                }
                if (st.getQuestItemsCount(HOWTOGO_RUINS) == 1L) {
                    htmltext = "roien_q0101_10.htm";
                }
            } else if (cond == 4 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0L) {
                htmltext = "roien_q0101_06.htm";
                st.setCond(5);
                st.takeItems(ALLTRANS_NOTE, -1L);
                st.giveItems(BROKEN_SWORD_HANDLE, 1L);
            }
        } else if (npcId == 30283) {
            if (cond == 1 && st.getQuestItemsCount(ROIENS_LETTER) > 0L) {
                htmltext = "blacksmith_alltran_q0101_01.htm";
            } else if (cond >= 2 && st.getQuestItemsCount(HOWTOGO_RUINS) == 1L) {
                if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 1L) {
                    htmltext = "blacksmith_alltran_q0101_08.htm";
                } else if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0L) {
                    htmltext = "blacksmith_alltran_q0101_03.htm";
                } else if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0L && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0L) {
                    htmltext = "blacksmith_alltran_q0101_04.htm";
                    st.setCond(4);
                    st.takeItems(HOWTOGO_RUINS, -1L);
                    st.takeItems(BROKEN_BLADE_TOP, -1L);
                    st.takeItems(BROKEN_BLADE_BOTTOM, -1L);
                    st.giveItems(ALLTRANS_NOTE, 1L);
                } else if (cond == 4 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0L) {
                    htmltext = "blacksmith_alltran_q0101_05.htm";
                }
            } else if (cond == 5 && st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0L) {
                htmltext = "blacksmith_alltran_q0101_06.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if ((npcId == 20361 || npcId == 20362) && st.getQuestItemsCount(HOWTOGO_RUINS) > 0L) {
            if (st.getQuestItemsCount(BROKEN_BLADE_TOP) == 0L && Rnd.chance(60)) {
                st.giveItems(BROKEN_BLADE_TOP, 1L);
                st.playSound("ItemSound.quest_middle");
            } else if (st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0L && Rnd.chance(60)) {
                st.giveItems(BROKEN_BLADE_BOTTOM, 1L);
                st.playSound("ItemSound.quest_middle");
            }
            if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0L && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0L) {
                st.setCond(3);
            }
        }
        return null;
    }
}
