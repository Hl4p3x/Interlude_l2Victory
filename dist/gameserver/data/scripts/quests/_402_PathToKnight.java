package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _402_PathToKnight extends Quest {
    public final int SIR_KLAUS_VASPER = 30417;
    public final int BIOTIN = 30031;
    public final int LEVIAN = 30037;
    public final int GILBERT = 30039;
    public final int RAYMOND = 30289;
    public final int SIR_COLLIN_WINDAWOOD = 30311;
    public final int BATHIS = 30332;
    public final int BEZIQUE = 30379;
    public final int SIR_ARON_TANFORD = 30653;
    public final int BUGBEAR_RAIDER = 20775;
    public final int UNDEAD_PRIEST = 27024;
    public final int POISON_SPIDER = 20038;
    public final int ARACHNID_TRACKER = 20043;
    public final int ARACHNID_PREDATOR = 20050;
    public final int LANGK_LIZARDMAN = 20030;
    public final int LANGK_LIZARDMAN_SCOUT = 20027;
    public final int LANGK_LIZARDMAN_WARRIOR = 20024;
    public final int GIANT_SPIDER = 20103;
    public final int TALON_SPIDER = 20106;
    public final int BLADE_SPIDER = 20108;
    public final int SILENT_HORROR = 20404;
    public final int SWORD_OF_RITUAL = 1161;
    public final int COIN_OF_LORDS1 = 1162;
    public final int COIN_OF_LORDS2 = 1163;
    public final int COIN_OF_LORDS3 = 1164;
    public final int COIN_OF_LORDS4 = 1165;
    public final int COIN_OF_LORDS5 = 1166;
    public final int COIN_OF_LORDS6 = 1167;
    public final int GLUDIO_GUARDS_MARK1 = 1168;
    public final int BUGBEAR_NECKLACE = 1169;
    public final int EINHASAD_CHURCH_MARK1 = 1170;
    public final int EINHASAD_CRUCIFIX = 1171;
    public final int GLUDIO_GUARDS_MARK2 = 1172;
    public final int POISON_SPIDER_LEG1 = 1173;
    public final int EINHASAD_CHURCH_MARK2 = 1174;
    public final int LIZARDMAN_TOTEM = 1175;
    public final int GLUDIO_GUARDS_MARK3 = 1176;
    public final int GIANT_SPIDER_HUSK = 1177;
    public final int EINHASAD_CHURCH_MARK3 = 1178;
    public final int HORRIBLE_SKULL = 1179;
    public final int MARK_OF_ESQUIRE = 1271;
    public final int[][] DROPLIST;

    public _402_PathToKnight() {
        super(false);
        DROPLIST = new int[][]{{20775, 1168, 1169, 10, 100}, {27024, 1170, 1171, 12, 100}, {20038, 1172, 1173, 20, 100}, {20043, 1172, 1173, 20, 100}, {20050, 1172, 1173, 20, 100}, {20030, 1174, 1175, 20, 50}, {20027, 1174, 1175, 20, 100}, {20024, 1174, 1175, 20, 100}, {20103, 1176, 1177, 20, 40}, {20106, 1176, 1177, 20, 40}, {20108, 1176, 1177, 20, 40}, {20404, 1178, 1179, 10, 100}};
        addStartNpc(30417);
        addTalkId(30031);
        addTalkId(30037);
        addTalkId(30039);
        addTalkId(30289);
        addTalkId(30311);
        addTalkId(30332);
        addTalkId(30379);
        addTalkId(30653);
        for (final int[] element : DROPLIST) {
            addKillId(element[0]);
        }
        addQuestItem(1169, 1171, 1173, 1175, 1177, 1179);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final Integer classid = st.getPlayer().getClassId().getId();
        final int level = st.getPlayer().getLevel();
        final long squire = st.getQuestItemsCount(1271);
        final long coin1 = st.getQuestItemsCount(1162);
        final long coin2 = st.getQuestItemsCount(1163);
        final long coin3 = st.getQuestItemsCount(1164);
        final long coin4 = st.getQuestItemsCount(1165);
        final long coin5 = st.getQuestItemsCount(1166);
        final long coin6 = st.getQuestItemsCount(1167);
        final long guards_mark1 = st.getQuestItemsCount(1168);
        final long guards_mark2 = st.getQuestItemsCount(1172);
        final long guards_mark3 = st.getQuestItemsCount(1176);
        final long church_mark1 = st.getQuestItemsCount(1170);
        final long church_mark2 = st.getQuestItemsCount(1174);
        final long church_mark3 = st.getQuestItemsCount(1178);
        if ("sir_karrel_vasper_q0402_02a.htm".equalsIgnoreCase(event)) {
            if (classid != 0 || level < 18) {
                htmltext = "sir_karrel_vasper_q0402_02.htm";
                st.exitCurrentQuest(true);
            } else if (st.getQuestItemsCount(1161) > 0L) {
                htmltext = "sir_karrel_vasper_q0402_04.htm";
            } else {
                htmltext = "sir_karrel_vasper_q0402_05.htm";
            }
        } else if ("sir_karrel_vasper_q0402_08.htm".equalsIgnoreCase(event)) {
            if (st.getCond() == 0 && classid == 0 && level >= 18) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1271, 1L);
            }
        } else if ("captain_bathia_q0402_02.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && guards_mark1 < 1L && coin1 < 1L) {
                st.giveItems(1168, 1L);
            }
        } else if ("bishop_raimund_q0402_03.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && church_mark1 < 1L && coin2 < 1L) {
                st.giveItems(1170, 1L);
            }
        } else if ("captain_bezique_q0402_02.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && guards_mark2 < 1L && coin3 < 1L) {
                st.giveItems(1172, 1L);
            }
        } else if ("levian_q0402_02.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && church_mark2 < 1L && coin4 < 1L) {
                st.giveItems(1174, 1L);
            }
        } else if ("gilbert_q0402_02.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && guards_mark3 < 1L && coin5 < 1L) {
                st.giveItems(1176, 1L);
            }
        } else if ("quilt_q0402_02.htm".equalsIgnoreCase(event)) {
            if (squire > 0L && church_mark3 < 1L && coin6 < 1L) {
                st.giveItems(1178, 1L);
            }
        } else if (("sir_karrel_vasper_q0402_13.htm".equalsIgnoreCase(event) | "sir_karrel_vasper_q0402_14.htm".equalsIgnoreCase(event)) && squire > 0L && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 >= 3L) {
            for (int i = 1162; i < 1179; ++i) {
                st.takeItems(i, -1L);
            }
            st.takeItems(1271, -1L);
            if (st.getPlayer().getClassId().getLevel() == 1) {
                st.giveItems(1161, 1L);
                if (!st.getPlayer().getVarB("prof1")) {
                    st.getPlayer().setVar("prof1", "1", -1L);
                    st.addExpAndSp(3200L, 1500L);
                }
            }
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final long squire = st.getQuestItemsCount(1271);
        final long coin1 = st.getQuestItemsCount(1162);
        final long coin2 = st.getQuestItemsCount(1163);
        final long coin3 = st.getQuestItemsCount(1164);
        final long coin4 = st.getQuestItemsCount(1165);
        final long coin5 = st.getQuestItemsCount(1166);
        final long coin6 = st.getQuestItemsCount(1167);
        final long guards_mark1 = st.getQuestItemsCount(1168);
        final long guards_mark2 = st.getQuestItemsCount(1172);
        final long guards_mark3 = st.getQuestItemsCount(1176);
        final long church_mark1 = st.getQuestItemsCount(1170);
        final long church_mark2 = st.getQuestItemsCount(1174);
        final long church_mark3 = st.getQuestItemsCount(1178);
        if (npcId == 30417) {
            if (cond == 0) {
                htmltext = "sir_karrel_vasper_q0402_01.htm";
            } else if (cond == 1 && squire > 0L) {
                if (coin1 + coin2 + coin3 + coin4 + coin5 + coin6 < 3L) {
                    htmltext = "sir_karrel_vasper_q0402_09.htm";
                } else if (coin1 + coin2 + coin3 + coin4 + coin5 + coin6 == 3L) {
                    htmltext = "sir_karrel_vasper_q0402_10.htm";
                } else if (coin1 + coin2 + coin3 + coin4 + coin5 + coin6 > 3L && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 < 6L) {
                    htmltext = "sir_karrel_vasper_q0402_11.htm";
                } else if (coin1 + coin2 + coin3 + coin4 + coin5 + coin6 == 6L) {
                    htmltext = "sir_karrel_vasper_q0402_12.htm";
                    for (int i = 1162; i < 1179; ++i) {
                        st.takeItems(i, -1L);
                    }
                    st.takeItems(1271, -1L);
                    st.giveItems(1161, 1L);
                    st.unset("cond");
                    st.exitCurrentQuest(true);
                    st.playSound("ItemSound.quest_finish");
                }
            }
        } else if (npcId == 30332 && cond == 1 && squire > 0L) {
            if (guards_mark1 < 1L && coin1 < 1L) {
                htmltext = "captain_bathia_q0402_01.htm";
            } else if (guards_mark1 > 0L) {
                if (st.getQuestItemsCount(1169) < 10L) {
                    htmltext = "captain_bathia_q0402_03.htm";
                } else {
                    htmltext = "captain_bathia_q0402_04.htm";
                    st.takeItems(1169, -1L);
                    st.takeItems(1168, 1L);
                    st.giveItems(1162, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin1 > 0L) {
                htmltext = "captain_bathia_q0402_05.htm";
            }
        } else if (npcId == 30289 && cond == 1 && squire > 0L) {
            if (church_mark1 < 1L && coin2 < 1L) {
                htmltext = "bishop_raimund_q0402_01.htm";
            } else if (church_mark1 > 0L) {
                if (st.getQuestItemsCount(1171) < 12L) {
                    htmltext = "bishop_raimund_q0402_04.htm";
                } else {
                    htmltext = "bishop_raimund_q0402_05.htm";
                    st.takeItems(1171, -1L);
                    st.takeItems(1170, 1L);
                    st.giveItems(1163, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin2 > 0L) {
                htmltext = "bishop_raimund_q0402_06.htm";
            }
        } else if (npcId == 30379 && cond == 1 && squire > 0L) {
            if (coin3 < 1L && guards_mark2 < 1L) {
                htmltext = "captain_bezique_q0402_01.htm";
            } else if (guards_mark2 > 0L) {
                if (st.getQuestItemsCount(1173) < 20L) {
                    htmltext = "captain_bezique_q0402_03.htm";
                } else {
                    htmltext = "captain_bezique_q0402_04.htm";
                    st.takeItems(1173, -1L);
                    st.takeItems(1172, 1L);
                    st.giveItems(1164, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin3 > 0L) {
                htmltext = "captain_bezique_q0402_05.htm";
            }
        } else if (npcId == 30037 && cond == 1 && squire > 0L) {
            if (coin4 < 1L && church_mark2 < 1L) {
                htmltext = "levian_q0402_01.htm";
            } else if (church_mark2 > 0L) {
                if (st.getQuestItemsCount(1175) < 20L) {
                    htmltext = "levian_q0402_03.htm";
                } else {
                    htmltext = "levian_q0402_04.htm";
                    st.takeItems(1175, -1L);
                    st.takeItems(1174, 1L);
                    st.giveItems(1165, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin4 > 0L) {
                htmltext = "levian_q0402_05.htm";
            }
        } else if (npcId == 30039 && cond == 1 && squire > 0L) {
            if (guards_mark3 < 1L && coin5 < 1L) {
                htmltext = "gilbert_q0402_01.htm";
            } else if (guards_mark3 > 0L) {
                if (st.getQuestItemsCount(1177) < 20L) {
                    htmltext = "gilbert_q0402_03.htm";
                } else {
                    htmltext = "gilbert_q0402_04.htm";
                    st.takeItems(1177, -1L);
                    st.takeItems(1176, 1L);
                    st.giveItems(1166, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin5 > 0L) {
                htmltext = "gilbert_q0402_05.htm";
            }
        } else if (npcId == 30031 && cond == 1 && squire > 0L) {
            if (church_mark3 < 1L && coin6 < 1L) {
                htmltext = "quilt_q0402_01.htm";
            } else if (church_mark3 > 0L) {
                if (st.getQuestItemsCount(1179) < 10L) {
                    htmltext = "quilt_q0402_03.htm";
                } else {
                    htmltext = "quilt_q0402_04.htm";
                    st.takeItems(1179, -1L);
                    st.takeItems(1178, 1L);
                    st.giveItems(1167, 1L);
                    st.playSound("ItemSound.quest_middle");
                }
            } else if (coin6 > 0L) {
                htmltext = "quilt_q0402_05.htm";
            }
        } else if (npcId == 30311 && cond == 1 && squire > 0L) {
            htmltext = "sir_collin_windawood_q0402_01.htm";
        } else if (npcId == 30653 && cond == 1 && squire > 0L) {
            htmltext = "sir_aron_tanford_q0402_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        for (final int[] element : DROPLIST) {
            if (st.getCond() > 0 && npcId == element[0] && st.getQuestItemsCount(element[1]) > 0L && st.getQuestItemsCount(element[2]) < element[3] && Rnd.chance(element[4])) {
                st.giveItems(element[2], 1L);
                if (st.getQuestItemsCount(element[2]) == element[3]) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
