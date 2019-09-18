package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _410_PathToPalusKnight extends Quest {
    public final int VIRGIL = 30329;
    public final int KALINTA = 30422;
    public final int POISON_SPIDER = 20038;
    public final int ARACHNID_TRACKER = 20043;
    public final int LYCANTHROPE = 20049;
    public final int PALLUS_TALISMAN_ID = 1237;
    public final int LYCANTHROPE_SKULL_ID = 1238;
    public final int VIRGILS_LETTER_ID = 1239;
    public final int MORTE_TALISMAN_ID = 1240;
    public final int PREDATOR_CARAPACE_ID = 1241;
    public final int TRIMDEN_SILK_ID = 1242;
    public final int COFFIN_ETERNAL_REST_ID = 1243;
    public final int GAZE_OF_ABYSS_ID = 1244;

    public _410_PathToPalusKnight() {
        super(false);
        addStartNpc(30329);
        addTalkId(30422);
        addKillId(20038);
        addKillId(20043);
        addKillId(20049);
        addQuestItem(1237, 1239, 1243, 1240, 1241, 1242, 1238);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "master_virgil_q0410_06.htm";
            st.giveItems(1237, 1L);
        } else if ("410_1".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1244) == 0L) {
                htmltext = "master_virgil_q0410_05.htm";
            } else if (st.getPlayer().getClassId().getId() != 31) {
                if (st.getPlayer().getClassId().getId() == 32) {
                    htmltext = "master_virgil_q0410_02a.htm";
                } else {
                    htmltext = "master_virgil_q0410_03.htm";
                }
            } else if (st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 31) {
                htmltext = "master_virgil_q0410_02.htm";
            } else if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1244) == 1L) {
                htmltext = "master_virgil_q0410_04.htm";
            }
        } else if ("30329_2".equalsIgnoreCase(event)) {
            htmltext = "master_virgil_q0410_10.htm";
            st.takeItems(1237, -1L);
            st.takeItems(1238, -1L);
            st.giveItems(1239, 1L);
            st.setCond(3);
        } else if ("30422_1".equalsIgnoreCase(event)) {
            htmltext = "kalinta_q0410_02.htm";
            st.takeItems(1239, -1L);
            st.giveItems(1240, 1L);
            st.setCond(4);
        } else if ("30422_2".equalsIgnoreCase(event)) {
            htmltext = "kalinta_q0410_06.htm";
            st.takeItems(1240, -1L);
            st.takeItems(1242, -1L);
            st.takeItems(1241, -1L);
            st.giveItems(1243, 1L);
            st.setCond(6);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30329) {
            if (cond < 1) {
                htmltext = "master_virgil_q0410_01.htm";
            } else if (st.getQuestItemsCount(1237) > 0L) {
                if (st.getQuestItemsCount(1238) < 1L) {
                    htmltext = "master_virgil_q0410_07.htm";
                } else if (st.getQuestItemsCount(1238) > 0L && st.getQuestItemsCount(1238) < 13L) {
                    htmltext = "master_virgil_q0410_08.htm";
                } else if (st.getQuestItemsCount(1238) > 12L) {
                    htmltext = "master_virgil_q0410_09.htm";
                }
            } else if (st.getQuestItemsCount(1243) > 0L) {
                htmltext = "master_virgil_q0410_11.htm";
                st.takeItems(1243, -1L);
                if (st.getPlayer().getClassId().getLevel() == 1) {
                    st.giveItems(1244, 1L);
                    if (!st.getPlayer().getVarB("prof1")) {
                        st.getPlayer().setVar("prof1", "1", -1L);
                        st.addExpAndSp(3200L, 3050L);
                    }
                }
                st.exitCurrentQuest(true);
                st.playSound("ItemSound.quest_finish");
            } else if (st.getQuestItemsCount(1240) > 0L | st.getQuestItemsCount(1239) > 0L) {
                htmltext = "master_virgil_q0410_12.htm";
            }
        } else if (npcId == 30422 && cond > 0) {
            if (st.getQuestItemsCount(1239) > 0L) {
                htmltext = "kalinta_q0410_01.htm";
            } else if (st.getQuestItemsCount(1240) > 0L) {
                if (st.getQuestItemsCount(1242) < 1L && st.getQuestItemsCount(1241) < 1L) {
                    htmltext = "kalinta_q0410_03.htm";
                } else if (st.getQuestItemsCount(1242) < 1L | st.getQuestItemsCount(1241) < 1L) {
                    htmltext = "kalinta_q0410_04.htm";
                } else if (st.getQuestItemsCount(1242) > 4L && st.getQuestItemsCount(1241) > 0L) {
                    htmltext = "kalinta_q0410_05.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20049) {
            if (cond == 1 && st.getQuestItemsCount(1237) > 0L && st.getQuestItemsCount(1238) < 13L) {
                st.giveItems(1238, 1L);
                if (st.getQuestItemsCount(1238) > 12L) {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(2);
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20038) {
            if (cond == 4 && st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1241) < 1L) {
                st.giveItems(1241, 1L);
                st.playSound("ItemSound.quest_middle");
                if (st.getQuestItemsCount(1242) > 4L) {
                    st.setCond(5);
                }
            }
        } else if (npcId == 20043 && cond == 4 && st.getQuestItemsCount(1240) > 0L && st.getQuestItemsCount(1242) < 5L) {
            st.giveItems(1242, 1L);
            if (st.getQuestItemsCount(1242) > 4L) {
                st.playSound("ItemSound.quest_middle");
                if (st.getQuestItemsCount(1241) > 0L) {
                    st.setCond(5);
                }
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
