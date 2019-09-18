package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _216_TrialoftheGuildsman extends Quest {
    private static final int VALKON = 30103;
    private static final int NORMAN = 30210;
    private static final int ALTRAN = 30283;
    private static final int PINTER = 30298;
    private static final int DUNING = 30688;
    private static final int MARK_OF_GUILDSMAN = 3119;
    private static final int VALKONS_RECOMMEND = 3120;
    private static final int MANDRAGORA_BERRY = 3121;
    private static final int ALLTRANS_INSTRUCTIONS = 3122;
    private static final int ALLTRANS_RECOMMEND1 = 3123;
    private static final int ALLTRANS_RECOMMEND2 = 3124;
    private static final int NORMANS_INSTRUCTIONS = 3125;
    private static final int NORMANS_RECEIPT = 3126;
    private static final int DUNINGS_INSTRUCTIONS = 3127;
    private static final int DUNINGS_KEY = 3128;
    private static final int NORMANS_LIST = 3129;
    private static final int GRAY_BONE_POWDER = 3130;
    private static final int GRANITE_WHETSTONE = 3131;
    private static final int RED_PIGMENT = 3132;
    private static final int BRAIDED_YARN = 3133;
    private static final int JOURNEYMAN_GEM = 3134;
    private static final int PINTERS_INSTRUCTIONS = 3135;
    private static final int AMBER_BEAD = 3136;
    private static final int AMBER_LUMP = 3137;
    private static final int JOURNEYMAN_DECO_BEADS = 3138;
    private static final int JOURNEYMAN_RING = 3139;
    private static final int RP_JOURNEYMAN_RING = 3024;
    private static final int DIMENSION_DIAMOND = 7562;
    private static final int RP_AMBER_BEAD = 3025;
    private static final int[][] DROPLIST_COND = {{3, 4, 20223, 3120, 3121, 1, 20, 1}, {3, 4, 20154, 3120, 3121, 1, 50, 1}, {3, 4, 20155, 3120, 3121, 1, 50, 1}, {3, 4, 20156, 3120, 3121, 1, 50, 1}, {5, 0, 20267, 3127, 3128, 30, 100, 1}, {5, 0, 20268, 3127, 3128, 30, 100, 1}, {5, 0, 20269, 3127, 3128, 30, 100, 1}, {5, 0, 20270, 3127, 3128, 30, 100, 1}, {5, 0, 20271, 3127, 3128, 30, 100, 1}, {5, 0, 20200, 3129, 3130, 70, 100, 2}, {5, 0, 20201, 3129, 3130, 70, 100, 2}, {5, 0, 20202, 3129, 3132, 70, 100, 2}, {5, 0, 20083, 3129, 3131, 70, 100, 2}, {5, 0, 20168, 3129, 3133, 70, 100, 2}};

    public _216_TrialoftheGuildsman() {
        super(false);
        addStartNpc(30103);
        addTalkId(30103);
        addTalkId(30210);
        addTalkId(30283);
        addTalkId(30298);
        addTalkId(30688);
        addKillId(20079);
        addKillId(20080);
        addKillId(20081);
        for (int i = 0; i < _216_TrialoftheGuildsman.DROPLIST_COND.length; ++i) {
            addKillId(_216_TrialoftheGuildsman.DROPLIST_COND[i][2]);
        }
        addQuestItem(3122, 3120, 3123, 3125, 3129, 3126, 3124, 3135, 3127, 3134, 3138, 3139, 3136, 3137, 3121, 3128, 3130, 3132, 3131, 3133);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("valkon_q0216_06.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3120, 1L);
            st.takeItems(57, 2000L);
            if (!st.getPlayer().getVarB("dd1")) {
                st.giveItems(7562, 85L);
                st.getPlayer().setVar("dd1", "1", -1L);
            }
        } else if ("valkon_q0216_07c.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
        } else if ("valkon_q0216_05.htm".equalsIgnoreCase(event) && st.getQuestItemsCount(57) < 2000L) {
            htmltext = "valkon_q0216_05a.htm";
        } else if ("30103_3".equalsIgnoreCase(event) || "30103_4".equalsIgnoreCase(event)) {
            if ("30103_3".equalsIgnoreCase(event)) {
                htmltext = "valkon_q0216_09a.htm";
            } else {
                htmltext = "valkon_q0216_09b.htm";
            }
            st.takeItems(3139, -1L);
            st.takeItems(3122, -1L);
            st.takeItems(3024, -1L);
            st.giveItems(3119, 1L);
            if (!st.getPlayer().getVarB("prof2.1")) {
                st.addExpAndSp(80993L, 12250L);
                st.getPlayer().setVar("prof2.1", "1", -1L);
            }
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_finish");
        } else if ("blacksmith_alltran_q0216_03.htm".equalsIgnoreCase(event)) {
            st.takeItems(3120, -1L);
            st.takeItems(3121, -1L);
            st.giveItems(3122, 1L);
            st.giveItems(3024, 1L);
            st.giveItems(3123, 1L);
            st.giveItems(3124, 1L);
            st.setCond(5);
        } else if ("warehouse_keeper_norman_q0216_04.htm".equalsIgnoreCase(event)) {
            st.takeItems(3123, -1L);
            st.giveItems(3125, 1L);
            st.giveItems(3126, 1L);
        } else if ("warehouse_keeper_norman_q0216_10.htm".equalsIgnoreCase(event)) {
            st.takeItems(3128, -1L);
            st.takeItems(3125, -1L);
            st.giveItems(3129, 1L);
        } else if ("blacksmith_duning_q0216_02.htm".equalsIgnoreCase(event)) {
            st.takeItems(3126, -1L);
            st.giveItems(3127, 1L);
        } else if ("blacksmith_pinter_q0216_04.htm".equalsIgnoreCase(event)) {
            st.takeItems(3124, -1L);
            st.giveItems(3135, 1L);
            if (st.getPlayer().getClassId().getId() == 56) {
                htmltext = "blacksmith_pinter_q0216_05.htm";
                st.giveItems(3025, 1L);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30103) {
            if (st.getQuestItemsCount(3119) > 0L) {
                htmltext = "completed";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                if (st.getPlayer().getClassId().getId() == 54 || st.getPlayer().getClassId().getId() == 56) {
                    if (st.getPlayer().getLevel() >= 35) {
                        htmltext = "valkon_q0216_03.htm";
                    } else {
                        htmltext = "valkon_q0216_02.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "valkon_q0216_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 2 && st.getQuestItemsCount(3120) > 0L) {
                htmltext = "valkon_q0216_07.htm";
            } else if (st.getQuestItemsCount(3122) > 0L) {
                if (st.getQuestItemsCount(3139) < 7L) {
                    htmltext = "valkon_q0216_08.htm";
                } else {
                    htmltext = "valkon_q0216_09.htm";
                }
            }
        } else if (npcId == 30283) {
            if (cond == 1 && st.getQuestItemsCount(3120) > 0L) {
                htmltext = "blacksmith_alltran_q0216_01.htm";
                st.setCond(2);
            } else if (cond == 4 && st.getQuestItemsCount(3120) > 0L && st.getQuestItemsCount(3121) == 1L) {
                htmltext = "blacksmith_alltran_q0216_02.htm";
            } else if (cond < 6 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3139) < 7L) {
                htmltext = "blacksmith_alltran_q0216_04.htm";
            } else if (cond == 6 && st.getQuestItemsCount(3139) == 7L) {
                htmltext = "blacksmith_alltran_q0216_05.htm";
            }
        } else if (npcId == 30210 && cond >= 5) {
            if (st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3123) == 1L) {
                htmltext = "warehouse_keeper_norman_q0216_01.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3126) > 0L) {
                htmltext = "warehouse_keeper_norman_q0216_05.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3127) > 0L) {
                htmltext = "warehouse_keeper_norman_q0216_06.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3128) >= 30L) {
                htmltext = "warehouse_keeper_norman_q0216_07.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3129) > 0L) {
                if (st.getQuestItemsCount(3130) >= 70L && st.getQuestItemsCount(3131) >= 70L && st.getQuestItemsCount(3132) >= 70L && st.getQuestItemsCount(3133) >= 70L) {
                    htmltext = "warehouse_keeper_norman_q0216_12.htm";
                    st.takeItems(3129, -1L);
                    st.takeItems(3130, -1L);
                    st.takeItems(3131, -1L);
                    st.takeItems(3132, -1L);
                    st.takeItems(3133, -1L);
                    st.giveItems(3134, 7L);
                    if (st.getQuestItemsCount(3138) == 7L && st.getQuestItemsCount(3134) == 7L) {
                        st.setCond(6);
                    }
                } else {
                    htmltext = "warehouse_keeper_norman_q0216_11.htm";
                }
            } else if (st.getQuestItemsCount(3125) == 0L && st.getQuestItemsCount(3129) == 0L && st.getQuestItemsCount(3122) == 1L && (st.getQuestItemsCount(3134) > 0L || st.getQuestItemsCount(3139) > 0L)) {
                htmltext = "warehouse_keeper_norman_q0216_13.htm";
            }
        } else if (npcId == 30688 && cond >= 5) {
            if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3126) > 0L) {
                htmltext = "blacksmith_duning_q0216_01.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3127) > 0L) {
                htmltext = "blacksmith_duning_q0216_03.htm";
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3125) > 0L && st.getQuestItemsCount(3128) == 30L) {
                htmltext = "blacksmith_duning_q0216_04.htm";
            } else if (st.getQuestItemsCount(3126) == 0L && st.getQuestItemsCount(3127) == 0L && st.getQuestItemsCount(3128) == 0L && st.getQuestItemsCount(3122) == 1L) {
                htmltext = "blacksmith_duning_q0216_01.htm";
            }
        } else if (npcId == 30298 && cond >= 5) {
            if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3124) > 0L) {
                if (st.getPlayer().getLevel() < 36) {
                    htmltext = "blacksmith_pinter_q0216_01.htm";
                } else {
                    htmltext = "blacksmith_pinter_q0216_02.htm";
                }
            } else if (st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3135) > 0L) {
                if (st.getQuestItemsCount(3136) < 70L) {
                    htmltext = "blacksmith_pinter_q0216_06.htm";
                } else {
                    htmltext = "blacksmith_pinter_q0216_07.htm";
                    st.takeItems(3135, -1L);
                    st.takeItems(3136, -1L);
                    st.takeItems(3025, -1L);
                    st.takeItems(3137, -1L);
                    st.giveItems(3138, 7L);
                    if (st.getQuestItemsCount(3138) == 7L && st.getQuestItemsCount(3134) == 7L) {
                        st.setCond(6);
                    }
                }
            } else if (st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3135) == 0L && (st.getQuestItemsCount(3138) > 0L || st.getQuestItemsCount(3139) > 0L)) {
                htmltext = "blacksmith_pinter_q0216_08.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _216_TrialoftheGuildsman.DROPLIST_COND.length; ++i) {
            if (cond == _216_TrialoftheGuildsman.DROPLIST_COND[i][0] && npcId == _216_TrialoftheGuildsman.DROPLIST_COND[i][2] && (_216_TrialoftheGuildsman.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_216_TrialoftheGuildsman.DROPLIST_COND[i][3]) > 0L)) {
                if (_216_TrialoftheGuildsman.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_216_TrialoftheGuildsman.DROPLIST_COND[i][4], _216_TrialoftheGuildsman.DROPLIST_COND[i][7], (double) _216_TrialoftheGuildsman.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_216_TrialoftheGuildsman.DROPLIST_COND[i][4], _216_TrialoftheGuildsman.DROPLIST_COND[i][7], _216_TrialoftheGuildsman.DROPLIST_COND[i][7], _216_TrialoftheGuildsman.DROPLIST_COND[i][5], (double) _216_TrialoftheGuildsman.DROPLIST_COND[i][6])) {
                    if (_216_TrialoftheGuildsman.DROPLIST_COND[i][4] == 3128) {
                        st.takeItems(3127, -1L);
                    }
                    if (_216_TrialoftheGuildsman.DROPLIST_COND[i][1] != cond && _216_TrialoftheGuildsman.DROPLIST_COND[i][1] != 0) {
                        st.setCond(_216_TrialoftheGuildsman.DROPLIST_COND[i][1]);
                        st.setState(2);
                    }
                }
            }
        }
        if (cond == 5 && (npcId == 20079 || npcId == 20080 || npcId == 20081) && Rnd.chance(33) && st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3135) > 0L) {
            long count = st.getQuestItemsCount(3136) + st.getQuestItemsCount(3137) * 5L;
            if (count < 70L && st.getPlayer().getClassId().getId() == 54) {
                st.giveItems(3136, 5L);
                if (st.getQuestItemsCount(3136) == 70L) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
            if (count < 70L && st.getPlayer().getClassId().getId() == 56) {
                st.giveItems(3137, 5L);
                if (((MonsterInstance) npc).isSpoiled()) {
                    st.giveItems(3137, 5L);
                }
                count = st.getQuestItemsCount(3136) + st.getQuestItemsCount(3137) * 5L;
                if (count == 70L) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
