package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;

public class _024_InhabitantsOfTheForestOfTheDead extends Quest {
    private static final int day_dorian = 31389;
    private static final int q_forest_stone2 = 31531;
    private static final int maid_of_ridia = 31532;
    private static final int shadow_hardin = 31522;
    private static final int q_letter_of_ridia = 7065;
    private static final int q_ridia_hairpin = 7148;
    private static final int q_triols_totem1 = 7151;
    private static final int q_lost_flower = 7152;
    private static final int q_silver_cross = 7153;
    private static final int q_broken_silver_cross = 7154;
    private static final int q_triols_totem2 = 7156;
    private static final int bone_snatcher = 21557;
    private static final int bone_snatcher_a = 21558;
    private static final int bone_shaper = 21560;
    private static final int bone_collector = 21563;
    private static final int skull_collector = 21564;
    private static final int bone_animator = 21565;
    private static final int skull_animator = 21566;
    private static final int bone_slayer = 21567;

    public _024_InhabitantsOfTheForestOfTheDead() {
        super(false);
        addStartNpc(31389);
        addTalkId(31531, 31532, 31522);
        addKillId(21557, 21558, 21560, 21563, 21564, 21565, 21566, 21567);
        addQuestItem(7151);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("people_of_lost_forest");
        if (event.startsWith("see_creature")) {
            if (st.getQuestItemsCount(7153) > 0L) {
                st.setCond(4);
                st.takeItems(7153, -1L);
                st.giveItems(7154, 1L);
                Functions.npcSay(npc, "That sign!");
            }
            return null;
        }
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("people_of_lost_forest", String.valueOf(1), true);
            st.giveItems(7152, 1L);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "day_dorian_q0024_03.htm";
        } else if ("reply_1".equalsIgnoreCase(event)) {
            st.set("people_of_lost_forest", String.valueOf(3), true);
            htmltext = "day_dorian_q0024_08.htm";
        } else if ("reply_3".equalsIgnoreCase(event)) {
            if (GetMemoState == 3) {
                st.setCond(3);
                st.set("people_of_lost_forest", String.valueOf(4), true);
                st.giveItems(7153, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "day_dorian_q0024_13.htm";
            }
        } else if ("reply_5".equalsIgnoreCase(event)) {
            st.playSound("InterfaceSound.charstat_open_01");
            htmltext = "day_dorian_q0024_18.htm";
        } else if ("reply_6".equalsIgnoreCase(event)) {
            if (GetMemoState == 4 && st.getQuestItemsCount(7154) >= 1L) {
                st.setCond(5);
                st.set("people_of_lost_forest", String.valueOf(5), true);
                st.takeItems(7154, -1L);
                htmltext = "day_dorian_q0024_19.htm";
            }
        } else if ("reply_1a".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.set("people_of_lost_forest", String.valueOf(2), true);
            st.takeItems(7152, -1L);
            st.playSound("ItemSound.quest_middle");
            htmltext = "q_forest_stone2_q0024_02.htm";
        } else if ("reply_7".equalsIgnoreCase(event)) {
            if (GetMemoState == 5) {
                st.setCond(6);
                st.set("people_of_lost_forest", String.valueOf(6), true);
                st.giveItems(7065, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "maid_of_ridia_q0024_04.htm";
            }
        } else if ("reply_8".equalsIgnoreCase(event)) {
            if ((GetMemoState == 6 || GetMemoState == 7) && st.getQuestItemsCount(7148) >= 1L) {
                st.takeItems(7065, -1L);
                st.takeItems(7148, -1L);
                st.set("people_of_lost_forest", String.valueOf(8), true);
                htmltext = "maid_of_ridia_q0024_06.htm";
            } else if ((GetMemoState == 6 || GetMemoState == 7) && st.getQuestItemsCount(7148) == 0L) {
                st.setCond(7);
                st.set("people_of_lost_forest", String.valueOf(7), true);
                st.playSound("ItemSound.quest_middle");
                htmltext = "maid_of_ridia_q0024_07.htm";
            }
        } else if ("reply_10".equalsIgnoreCase(event)) {
            if (GetMemoState == 8) {
                st.set("people_of_lost_forest", String.valueOf(9), true);
                htmltext = "maid_of_ridia_q0024_10.htm";
            }
        } else if ("reply_11".equalsIgnoreCase(event)) {
            if (GetMemoState == 9) {
                st.set("people_of_lost_forest", String.valueOf(10), true);
                htmltext = "maid_of_ridia_q0024_14.htm";
            }
        } else if ("reply_12".equalsIgnoreCase(event)) {
            if (GetMemoState == 10) {
                st.setCond(9);
                st.set("people_of_lost_forest", String.valueOf(11), true);
                st.playSound("ItemSound.quest_middle");
                htmltext = "maid_of_ridia_q0024_19.htm";
            }
        } else if ("reply_14".equalsIgnoreCase(event)) {
            if (GetMemoState == 11 && st.getQuestItemsCount(7151) >= 1L) {
                st.set("people_of_lost_forest", String.valueOf(12), true);
                st.takeItems(7151, -1L);
                htmltext = "shadow_hardin_q0024_03.htm";
            }
        } else if ("reply_16".equalsIgnoreCase(event)) {
            if (GetMemoState == 12) {
                st.setCond(11);
                st.set("people_of_lost_forest", String.valueOf(13), true);
                htmltext = "shadow_hardin_q0024_08.htm";
            }
        } else if ("reply_18".equalsIgnoreCase(event)) {
            if (GetMemoState == 13) {
                st.set("people_of_lost_forest", String.valueOf(14), true);
                htmltext = "shadow_hardin_q0024_17.htm";
            }
        } else if ("reply_19".equalsIgnoreCase(event) && GetMemoState == 14) {
            st.giveItems(7156, 1L);
            st.addExpAndSp(242105L, 22529L);
            st.unset("people_of_lost_forest");
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
            htmltext = "shadow_hardin_q0024_21.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final QuestState qs = st.getPlayer().getQuestState(_023_LidiasHeart.class);
        final int GetMemoState = st.getInt("people_of_lost_forest");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31389) {
                    break;
                }
                if (qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 65) {
                    htmltext = "day_dorian_q0024_01.htm";
                    break;
                }
                htmltext = "day_dorian_q0024_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 31389:
                        if (GetMemoState == 1) {
                            htmltext = "day_dorian_q0024_04.htm";
                            break;
                        }
                        if (GetMemoState == 2) {
                            htmltext = "day_dorian_q0024_05.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "day_dorian_q0024_09.htm";
                            break;
                        }
                        if (GetMemoState == 4 && st.getQuestItemsCount(7154) >= 1L) {
                            htmltext = "day_dorian_q0024_15.htm";
                            break;
                        }
                        if (GetMemoState == 5) {
                            htmltext = "day_dorian_q0024_20.htm";
                            break;
                        }
                        if (GetMemoState == 7 && st.getQuestItemsCount(7148) == 0L) {
                            st.setCond(8);
                            st.giveItems(7148, 1L);
                            st.playSound("ItemSound.quest_middle");
                            htmltext = "day_dorian_q0024_21.htm";
                            break;
                        }
                        if ((GetMemoState == 7 && st.getQuestItemsCount(7148) >= 1L) || GetMemoState == 6) {
                            htmltext = "day_dorian_q0024_22.htm";
                            break;
                        }
                        break;
                    case 31531:
                        if (GetMemoState == 1 && st.getQuestItemsCount(7152) >= 1L) {
                            st.playSound("AmdSound.d_wind_loot_02");
                            htmltext = "q_forest_stone2_q0024_01.htm";
                            break;
                        }
                        if (GetMemoState == 2) {
                            htmltext = "q_forest_stone2_q0024_03.htm";
                            break;
                        }
                        break;
                    case 31532:
                        if (GetMemoState == 5) {
                            htmltext = "maid_of_ridia_q0024_01.htm";
                            break;
                        }
                        if (GetMemoState == 6 && st.getQuestItemsCount(7065) >= 1L) {
                            htmltext = "maid_of_ridia_q0024_05.htm";
                            break;
                        }
                        if (GetMemoState == 7) {
                            htmltext = "maid_of_ridia_q0024_07a.htm";
                            break;
                        }
                        if (GetMemoState == 8) {
                            htmltext = "maid_of_ridia_q0024_08.htm";
                            break;
                        }
                        if (GetMemoState == 9) {
                            htmltext = "maid_of_ridia_q0024_11.htm";
                            break;
                        }
                        if (GetMemoState == 11) {
                            htmltext = "maid_of_ridia_q0024_20.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 31522) {
                            break;
                        }
                        if (GetMemoState == 11 && st.getQuestItemsCount(7151) >= 1L) {
                            htmltext = "shadow_hardin_q0024_01.htm";
                            break;
                        }
                        if (GetMemoState == 12) {
                            htmltext = "shadow_hardin_q0024_04.htm";
                            break;
                        }
                        if (GetMemoState == 13) {
                            htmltext = "shadow_hardin_q0024_09.htm";
                            break;
                        }
                        if (GetMemoState == 14) {
                            htmltext = "shadow_hardin_q0024_18.htm";
                            break;
                        }
                        break;
                }
            }
            case 3: {
                if (npcId == 31522) {
                    htmltext = "shadow_hardin_q0024_22.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int GetMemoState = st.getInt("people_of_lost_forest");
        if ((npcId == 21557 || npcId == 21558 || npcId == 21560 || npcId == 21563 || npcId == 21564 || npcId == 21565 || npcId == 21566 || npcId == 21567) && GetMemoState == 11 && Rnd.get(100) < 10) {
            st.setCond(10);
            st.giveItems(7151, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
