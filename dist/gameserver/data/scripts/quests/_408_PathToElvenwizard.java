package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _408_PathToElvenwizard extends Quest {
    public final int GREENIS = 30157;
    public final int THALIA = 30371;
    public final int ROSELLA = 30414;
    public final int NORTHWIND = 30423;
    public final int DRYAD_ELDER = 20019;
    public final int PINCER_SPIDER = 20466;
    public final int SUKAR_WERERAT_LEADER = 20047;
    public final int ROGELLIAS_LETTER_ID = 1218;
    public final int RED_DOWN_ID = 1219;
    public final int MAGICAL_POWERS_RUBY_ID = 1220;
    public final int PURE_AQUAMARINE_ID = 1221;
    public final int APPETIZING_APPLE_ID = 1222;
    public final int GOLD_LEAVES_ID = 1223;
    public final int IMMORTAL_LOVE_ID = 1224;
    public final int AMETHYST_ID = 1225;
    public final int NOBILITY_AMETHYST_ID = 1226;
    public final int FERTILITY_PERIDOT_ID = 1229;
    public final int ETERNITY_DIAMOND_ID = 1230;
    public final int CHARM_OF_GRAIN_ID = 1272;
    public final int SAP_OF_WORLD_TREE_ID = 1273;
    public final int LUCKY_POTPOURI_ID = 1274;

    public _408_PathToElvenwizard() {
        super(false);
        addStartNpc(30414);
        addTalkId(30157);
        addTalkId(30371);
        addTalkId(30423);
        addKillId(20019);
        addKillId(20466);
        addKillId(20047);
        addQuestItem(1218, 1229, 1224, 1222, 1272, 1220, 1273, 1221, 1274, 1226, 1223, 1219, 1225);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            if (st.getPlayer().getClassId().getId() != 25) {
                if (st.getPlayer().getClassId().getId() == 26) {
                    htmltext = "rogellia_q0408_02a.htm";
                } else {
                    htmltext = "rogellia_q0408_03.htm";
                }
            } else if (st.getPlayer().getLevel() < 18) {
                htmltext = "rogellia_q0408_04.htm";
            } else if (st.getQuestItemsCount(1230) > 0L) {
                htmltext = "rogellia_q0408_05.htm";
            } else {
                st.setState(2);
                st.setCond(1);
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1229, 1L);
                htmltext = "rogellia_q0408_06.htm";
            }
        } else if ("408_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1220) > 0L) {
                htmltext = "rogellia_q0408_10.htm";
            } else if (st.getQuestItemsCount(1220) < 1L && st.getQuestItemsCount(1229) > 0L) {
                st.giveItems(1218, 1L);
                htmltext = "rogellia_q0408_07.htm";
            }
        } else if ("408_4".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1218) > 0L) {
                st.takeItems(1218, -1L);
                st.giveItems(1272, 1L);
                htmltext = "grain_q0408_02.htm";
            }
        } else if ("408_2".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1221) > 0L) {
                htmltext = "rogellia_q0408_13.htm";
            } else if (st.getQuestItemsCount(1221) < 1L && st.getQuestItemsCount(1229) > 0L) {
                st.giveItems(1222, 1L);
                htmltext = "rogellia_q0408_14.htm";
            }
        } else if ("408_5".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1222) > 0L) {
                st.takeItems(1222, -1L);
                st.giveItems(1273, 1L);
                htmltext = "thalya_q0408_02.htm";
            }
        } else if ("408_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1226) > 0L) {
                htmltext = "rogellia_q0408_17.htm";
            } else if (st.getQuestItemsCount(1226) < 1L && st.getQuestItemsCount(1229) > 0L) {
                st.giveItems(1224, 1L);
                htmltext = "rogellia_q0408_18.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30414) {
            if (cond < 1) {
                htmltext = "rogellia_q0408_01.htm";
            } else if (st.getQuestItemsCount(1272) > 0L) {
                if (st.getQuestItemsCount(1219) < 5L) {
                    htmltext = "rogellia_q0408_09.htm";
                } else if (st.getQuestItemsCount(1219) > 4L) {
                    htmltext = "rogellia_q0408_25.htm";
                } else if (st.getQuestItemsCount(1223) > 4L) {
                    htmltext = "rogellia_q0408_26.htm";
                }
            } else if (st.getQuestItemsCount(1222) > 0L) {
                htmltext = "rogellia_q0408_15.htm";
            } else if (st.getQuestItemsCount(1224) > 0L) {
                htmltext = "rogellia_q0408_19.htm";
            } else if (st.getQuestItemsCount(1273) > 0L && st.getQuestItemsCount(1223) < 5L) {
                htmltext = "rogellia_q0408_16.htm";
            } else if (st.getQuestItemsCount(1274) > 0L) {
                if (st.getQuestItemsCount(1225) < 2L) {
                    htmltext = "rogellia_q0408_20.htm";
                } else {
                    htmltext = "rogellia_q0408_27.htm";
                }
            } else if (st.getQuestItemsCount(1218) > 0L) {
                htmltext = "rogellia_q0408_08.htm";
            } else if (st.getQuestItemsCount(1218) < 1L && st.getQuestItemsCount(1222) < 1L && st.getQuestItemsCount(1224) < 1L && st.getQuestItemsCount(1272) < 1L && st.getQuestItemsCount(1273) < 1L && st.getQuestItemsCount(1274) < 1L && st.getQuestItemsCount(1229) > 0L) {
                if (st.getQuestItemsCount(1220) < 1L | st.getQuestItemsCount(1226) < 1L | st.getQuestItemsCount(1221) < 1L) {
                    htmltext = "rogellia_q0408_11.htm";
                } else if (st.getQuestItemsCount(1220) > 0L && st.getQuestItemsCount(1226) > 0L && st.getQuestItemsCount(1221) > 0L) {
                    st.takeItems(1220, -1L);
                    st.takeItems(1221, st.getQuestItemsCount(1221));
                    st.takeItems(1226, st.getQuestItemsCount(1226));
                    st.takeItems(1229, st.getQuestItemsCount(1229));
                    htmltext = "rogellia_q0408_24.htm";
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(1230, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 1890L);
                        }
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                }
            }
        } else if (npcId == 30157 && cond > 0) {
            if (st.getQuestItemsCount(1218) > 0L) {
                htmltext = "grain_q0408_01.htm";
            } else if (st.getQuestItemsCount(1272) > 0L) {
                if (st.getQuestItemsCount(1219) < 5L) {
                    htmltext = "grain_q0408_03.htm";
                } else {
                    st.takeItems(1219, -1L);
                    st.takeItems(1272, -1L);
                    st.giveItems(1220, 1L);
                    htmltext = "grain_q0408_04.htm";
                }
            }
        } else if (npcId == 30371 && cond > 0) {
            if (st.getQuestItemsCount(1222) > 0L) {
                htmltext = "thalya_q0408_01.htm";
            } else if (st.getQuestItemsCount(1273) > 0L) {
                if (st.getQuestItemsCount(1223) < 5L) {
                    htmltext = "thalya_q0408_03.htm";
                } else {
                    st.takeItems(1223, -1L);
                    st.takeItems(1273, -1L);
                    st.giveItems(1221, 1L);
                    htmltext = "thalya_q0408_04.htm";
                }
            }
        } else if (npcId == 30423 && cond > 0) {
            if (st.getQuestItemsCount(1224) > 0L) {
                st.takeItems(1224, -1L);
                st.giveItems(1274, 1L);
                htmltext = "northwindel_q0408_01.htm";
            } else if (st.getQuestItemsCount(1274) > 0L) {
                if (st.getQuestItemsCount(1225) < 2L) {
                    htmltext = "northwindel_q0408_02.htm";
                } else {
                    st.takeItems(1225, -1L);
                    st.takeItems(1274, -1L);
                    st.giveItems(1226, 1L);
                    htmltext = "northwindel_q0408_03.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20466) {
            if (cond > 0 && st.getQuestItemsCount(1272) > 0L && st.getQuestItemsCount(1219) < 5L && Rnd.chance(70)) {
                st.giveItems(1219, 1L);
                if (st.getQuestItemsCount(1219) < 5L) {
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    st.playSound("ItemSound.quest_middle");
                }
            }
        } else if (npcId == 20019) {
            if (cond > 0 && st.getQuestItemsCount(1273) > 0L && st.getQuestItemsCount(1223) < 5L && Rnd.chance(40)) {
                st.giveItems(1223, 1L);
                if (st.getQuestItemsCount(1223) < 5L) {
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    st.playSound("ItemSound.quest_middle");
                }
            }
        } else if (npcId == 20047 && cond > 0 && st.getQuestItemsCount(1274) > 0L && st.getQuestItemsCount(1225) < 2L && Rnd.chance(40)) {
            st.giveItems(1225, 1L);
            if (st.getQuestItemsCount(1225) < 2L) {
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }
}
