package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

public class _235_MimirsElixir extends Quest {
    private static final int magister_ladd = 30721;
    private static final int magister_joan = 30718;
    private static final int alchemical_mixing_jar = 31149;
    private static final int chimera_piece = 20965;
    private static final int bloody_guardian = 21090;
    private static final int q_star_of_destiny = 5011;
    private static final int magisters_mixing_stone = 5905;
    private static final int bloodfire = 6318;
    private static final int mimirs_elixir = 6319;
    private static final int pure_silver = 6320;
    private static final int true_gold = 6321;
    private static final int philosophers_stone = 6322;
    private static final int scrl_of_ench_wp_a = 729;

    public _235_MimirsElixir() {
        super(false);
        addStartNpc(30721);
        addTalkId(30721, 30718, 31149);
        addKillId(20965, 21090);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("water_of_mimir");
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 30721:
                if ("quest_accept".equalsIgnoreCase(event)) {
                    st.setCond(1);
                    st.set("water_of_mimir", String.valueOf(1), true);
                    st.setState(2);
                    st.playSound("ItemSound.quest_accept");
                    htmltext = "magister_ladd_q0235_06.htm";
                } else if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "magister_ladd_q0235_02.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    htmltext = "magister_ladd_q0235_03.htm";
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    htmltext = "magister_ladd_q0235_04.htm";
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    htmltext = "magister_ladd_q0235_05.htm";
                } else if ("reply_5".equalsIgnoreCase(event) && GetMemoState == 1) {
                    st.setCond(2);
                    st.set("water_of_mimir", String.valueOf(2), true);
                    htmltext = "magister_ladd_q0235_09.htm";
                } else if ("reply_6".equalsIgnoreCase(event) && GetMemoState == 5) {
                    st.setCond(6);
                    st.set("water_of_mimir", String.valueOf(6), true);
                    st.giveItems(5905, 1L);
                    htmltext = "magister_ladd_q0235_12.htm";
                } else if ("reply_7".equalsIgnoreCase(event) && GetMemoState == 8) {
                    htmltext = "magister_ladd_q0235_15.htm";
                } else if ("reply_8".equalsIgnoreCase(event) && GetMemoState == 8 && st.getQuestItemsCount(5905) >= 1L && st.getQuestItemsCount(6319) >= 1L) {
                    npc.doCast(SkillTable.getInstance().getInfo(4339, 1), st.getPlayer(), true);
                    st.takeItems(5905, -1L);
                    st.takeItems(6319, -1L);
                    st.takeItems(5011, -1L);
                    st.giveItems(729, 1L);
                    st.unset("water_of_mimir");
                    st.exitCurrentQuest(false);
                    st.playSound("ItemSound.quest_finish");
                    htmltext = "magister_ladd_q0235_16.htm";
                }
                break;
            case 30718:
                if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "magister_joan_q0235_02.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    st.setCond(3);
                    st.set("water_of_mimir", String.valueOf(3), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "magister_joan_q0235_03.htm";
                } else if ("reply_3".equalsIgnoreCase(event) && GetMemoState == 4 && st.getQuestItemsCount(6322) >= 1L) {
                    st.setCond(5);
                    st.set("water_of_mimir", String.valueOf(5), true);
                    st.giveItems(6321, 1L);
                    st.takeItems(6322, 1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "magister_joan_q0235_06.htm";
                }
                break;
            case 31149:
                if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "alchemical_mixing_jar_q0235_02.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(6320) < 1L) {
                        htmltext = "alchemical_mixing_jar_q0235_03.htm";
                    } else {
                        htmltext = "alchemical_mixing_jar_q0235_04.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    htmltext = "alchemical_mixing_jar_q0235_05.htm";
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(6321) >= 1L) {
                        htmltext = "alchemical_mixing_jar_q0235_06.htm";
                    } else {
                        htmltext = "alchemical_mixing_jar_q0235_06a.htm";
                    }
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    htmltext = "alchemical_mixing_jar_q0235_07.htm";
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(6318) < 1L) {
                        htmltext = "alchemical_mixing_jar_q0235_08.htm";
                    } else {
                        htmltext = "alchemical_mixing_jar_q0235_09.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    htmltext = "alchemical_mixing_jar_q0235_10.htm";
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    htmltext = "alchemical_mixing_jar_q0235_11.htm";
                } else if ("reply_9".equalsIgnoreCase(event) && st.getQuestItemsCount(6318) >= 1L && st.getQuestItemsCount(6320) >= 1L && st.getQuestItemsCount(6321) >= 1L && GetMemoState == 7) {
                    st.setCond(8);
                    st.set("water_of_mimir", String.valueOf(8), true);
                    st.giveItems(6319, 1L);
                    st.takeItems(6320, -1L);
                    st.takeItems(6321, -1L);
                    st.takeItems(6318, -1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "alchemical_mixing_jar_q0235_12.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("water_of_mimir");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30721) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 75 && st.getQuestItemsCount(5011) >= 1L) {
                    htmltext = "magister_ladd_q0235_01.htm";
                    break;
                }
                if (st.getPlayer().getLevel() >= 75 && st.getQuestItemsCount(5011) < 1L) {
                    htmltext = "magister_ladd_q0235_01b.htm";
                    break;
                }
                if (st.getPlayer().getLevel() < 75) {
                    htmltext = "magister_ladd_q0235_01a.htm";
                    break;
                }
                break;
            }
            case 2: {
                switch (npcId) {
                    case 30721:
                        if (GetMemoState == 1 && st.getQuestItemsCount(6320) < 1L) {
                            htmltext = "magister_ladd_q0235_07.htm";
                            break;
                        }
                        if (GetMemoState == 1 && st.getQuestItemsCount(6320) >= 1L) {
                            htmltext = "magister_ladd_q0235_08.htm";
                            break;
                        }
                        if (GetMemoState >= 2 && GetMemoState < 5) {
                            htmltext = "magister_ladd_q0235_10.htm";
                            break;
                        }
                        if (GetMemoState == 5) {
                            htmltext = "magister_ladd_q0235_11.htm";
                            break;
                        }
                        if (GetMemoState >= 6 && GetMemoState < 8) {
                            htmltext = "magister_ladd_q0235_13.htm";
                            break;
                        }
                        if (GetMemoState == 8) {
                            htmltext = "magister_ladd_q0235_14.htm";
                            break;
                        }
                        break;
                    case 30718:
                        if (GetMemoState == 2) {
                            htmltext = "magister_joan_q0235_01.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "magister_joan_q0235_04.htm";
                            break;
                        }
                        if (GetMemoState == 4) {
                            htmltext = "magister_joan_q0235_05.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId == 31149 && GetMemoState == 7 && st.getQuestItemsCount(5905) >= 1L) {
                            htmltext = "alchemical_mixing_jar_q0235_01.htm";
                            break;
                        }
                        break;
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("water_of_mimir");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 3 && npcId == 20965 && st.getQuestItemsCount(6322) == 0L) {
            if (Rnd.get(5) == 0) {
                st.setCond(4);
                st.set("water_of_mimir", String.valueOf(4), true);
                st.giveItems(6322, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (GetMemoState == 6 && npcId == 21090 && st.getQuestItemsCount(6318) == 0L && Rnd.get(5) == 0) {
            st.setCond(7);
            st.set("water_of_mimir", String.valueOf(7), true);
            st.giveItems(6318, 1L);
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
