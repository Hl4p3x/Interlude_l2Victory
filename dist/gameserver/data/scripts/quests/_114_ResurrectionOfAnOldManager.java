package quests;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.scripts.Functions;

public class _114_ResurrectionOfAnOldManager extends Quest {
    private static final int NEWYEAR = 31961;
    private static final int YUMI = 32041;
    private static final int STONES = 32046;
    private static final int WENDY = 32047;
    private static final int BOX = 32050;
    private static final int GUARDIAN = 27318;
    private static final int DETECTOR = 8090;
    private static final int DETECTOR2 = 8091;
    private static final int STARSTONE = 8287;
    private static final int LETTER = 8288;
    private static final int STARSTONE2 = 8289;
    private NpcInstance GUARDIAN_SPAWN;

    public _114_ResurrectionOfAnOldManager() {
        super(false);
        GUARDIAN_SPAWN = null;
        addStartNpc(32041);
        addTalkId(32047);
        addTalkId(32050);
        addTalkId(32046);
        addTalkId(31961);
        addFirstTalkId(32046);
        addKillId(27318);
        addQuestItem(8090);
        addQuestItem(8091);
        addQuestItem(8287);
        addQuestItem(8288);
        addQuestItem(8289);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("head_blacksmith_newyear_q0114_02.htm".equalsIgnoreCase(event)) {
            st.setCond(22);
            st.takeItems(8288, 1L);
            st.giveItems(8289, 1L);
            st.playSound("ItemSound.quest_middle");
        }
        if ("collecter_yumi_q0114_04.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
            st.set("talk", "0");
        } else if ("collecter_yumi_q0114_08.htm".equalsIgnoreCase(event)) {
            st.set("talk", "1");
        } else if ("collecter_yumi_q0114_09.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
        } else if ("collecter_yumi_q0114_12.htm".equalsIgnoreCase(event)) {
            final int choice = st.getInt("choice");
            switch (choice) {
                case 1:
                    htmltext = "collecter_yumi_q0114_12.htm";
                    break;
                case 2:
                    htmltext = "collecter_yumi_q0114_13.htm";
                    break;
                case 3:
                    htmltext = "collecter_yumi_q0114_14.htm";
                    break;
            }
        } else if ("collecter_yumi_q0114_15.htm".equalsIgnoreCase(event)) {
            st.set("talk", "1");
        } else if ("collecter_yumi_q0114_23.htm".equalsIgnoreCase(event)) {
            st.set("talk", "2");
        } else if ("collecter_yumi_q0114_26.htm".equalsIgnoreCase(event)) {
            st.setCond(6);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
        } else if ("collecter_yumi_q0114_31.htm".equalsIgnoreCase(event)) {
            st.setCond(17);
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8090, 1L);
        } else if ("collecter_yumi_q0114_34.htm".equalsIgnoreCase(event)) {
            st.takeItems(8091, 1L);
            st.set("talk", "1");
        } else if ("collecter_yumi_q0114_38.htm".equalsIgnoreCase(event)) {
            final int choice = st.getInt("choice");
            if (choice > 1) {
                htmltext = "collecter_yumi_q0114_37.htm";
            }
        } else if ("collecter_yumi_q0114_40.htm".equalsIgnoreCase(event)) {
            st.setCond(21);
            st.giveItems(8288, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("collecter_yumi_q0114_39.htm".equalsIgnoreCase(event)) {
            st.setCond(20);
            st.playSound("ItemSound.quest_middle");
        } else if ("pavel_atlanta_q0114_03.htm".equalsIgnoreCase(event)) {
            st.setCond(19);
            st.playSound("ItemSound.quest_middle");
        } else if ("pavel_atlanta_q0114_07.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("chaos_secretary_wendy_q0114_01.htm".equalsIgnoreCase(event)) {
            if (st.getInt("talk") + st.getInt("talk1") == 2) {
                htmltext = "chaos_secretary_wendy_q0114_05.htm";
            } else if (st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6) {
                htmltext = "chaos_secretary_wendy_q0114_06a.htm";
            }
        } else if ("chaos_secretary_wendy_q0114_02.htm".equalsIgnoreCase(event)) {
            if (st.getInt("talk") == 0) {
                st.set("talk", "1");
            }
        } else if ("chaos_secretary_wendy_q0114_03.htm".equalsIgnoreCase(event)) {
            if (st.getInt("talk1") == 0) {
                st.set("talk1", "1");
            }
        } else if ("chaos_secretary_wendy_q0114_06.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "1");
            st.unset("talk1");
        } else if ("chaos_secretary_wendy_q0114_07.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "2");
            st.unset("talk1");
        } else if ("chaos_secretary_wendy_q0114_09.htm".equalsIgnoreCase(event)) {
            st.setCond(5);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
            st.set("choice", "3");
            st.unset("talk1");
        } else if ("chaos_secretary_wendy_q0114_14ab.htm".equalsIgnoreCase(event)) {
            st.setCond(7);
            st.playSound("ItemSound.quest_middle");
        } else if ("chaos_secretary_wendy_q0114_14b.htm".equalsIgnoreCase(event)) {
            st.setCond(10);
            st.playSound("ItemSound.quest_middle");
        } else if ("chaos_secretary_wendy_q0114_12c.htm".equalsIgnoreCase(event)) {
            if (st.getInt("talk") == 0) {
                st.set("talk", "1");
            }
        } else if ("chaos_secretary_wendy_q0114_15b.htm".equalsIgnoreCase(event)) {
            if (GUARDIAN_SPAWN == null || !st.getPlayer().knowsObject(GUARDIAN_SPAWN) || !GUARDIAN_SPAWN.isVisible()) {
                Functions.npcSay(GUARDIAN_SPAWN = st.addSpawn(27318, 96977, -110625, -3280, 900000), "You, " + st.getPlayer().getName() + ", you attacked Wendy. Prepare to die!");
                GUARDIAN_SPAWN.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 999);
            } else {
                htmltext = "chaos_secretary_wendy_q0114_17b.htm";
            }
        } else if ("chaos_secretary_wendy_q0114_20b.htm".equalsIgnoreCase(event)) {
            st.setCond(12);
            st.playSound("ItemSound.quest_middle");
        } else if ("chaos_secretary_wendy_q0114_17c.htm".equalsIgnoreCase(event)) {
            st.set("talk", "2");
        } else if ("chaos_secretary_wendy_q0114_20c.htm".equalsIgnoreCase(event)) {
            st.setCond(13);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
        } else if ("chaos_secretary_wendy_q0114_23c.htm".equalsIgnoreCase(event)) {
            st.setCond(15);
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8287, 1L);
        } else if ("chaos_secretary_wendy_q0114_16a.htm".equalsIgnoreCase(event)) {
            st.set("talk", "2");
        } else if ("chaos_secretary_wendy_q0114_20a.htm".equalsIgnoreCase(event)) {
            if (st.getCond() == 7) {
                st.setCond(8);
                st.set("talk", "0");
                st.playSound("ItemSound.quest_middle");
            } else if (st.getCond() == 8) {
                st.setCond(9);
                st.playSound("ItemSound.quest_middle");
                htmltext = "chaos_secretary_wendy_q0114_21a.htm";
            }
        } else if ("chaos_secretary_wendy_q0114_21a.htm".equalsIgnoreCase(event)) {
            st.setCond(9);
            st.playSound("ItemSound.quest_middle");
        } else if ("chaos_secretary_wendy_q0114_29c.htm".equalsIgnoreCase(event)) {
            st.giveItems(8289, 1L);
            st.takeItems(57, 3000L);
            st.setCond(26);
            st.playSound("ItemSound.quest_middle");
        } else if ("chaos_box2_q0114_01r.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.armor_wood_3");
            st.set("talk", "1");
        } else if ("chaos_box2_q0114_03.htm".equalsIgnoreCase(event)) {
            st.setCond(14);
            st.giveItems(8287, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("talk", "0");
        }
        return htmltext;
    }

    @Override
    public String onFirstTalk(final NpcInstance npc, final Player player) {
        final QuestState st = player.getQuestState(getName());
        if (st == null || st.isCompleted()) {
            return "";
        }
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 32046 && cond == 17) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8090, 1L);
            st.giveItems(8091, 1L);
            st.setCond(18);
            player.sendPacket(new ExShowScreenMessage("THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE", 4500, ScreenMessageAlign.TOP_CENTER, true));
        }
        return "";
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final int talk = st.getInt("talk");
        final int talk2 = st.getInt("talk1");
        switch (npcId) {
            case 32041:
                if (id == 1) {
                    final QuestState Pavel = st.getPlayer().getQuestState(_121_PavelTheGiants.class);
                    if (Pavel == null) {
                        return "collecter_yumi_q0114_01.htm";
                    }
                    if (st.getPlayer().getLevel() >= 49 && Pavel.getState() == 3) {
                        htmltext = "collecter_yumi_q0114_02.htm";
                    } else {
                        htmltext = "collecter_yumi_q0114_01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 1) {
                    if (talk == 0) {
                        htmltext = "collecter_yumi_q0114_04.htm";
                    } else {
                        htmltext = "collecter_yumi_q0114_08.htm";
                    }
                } else if (cond == 2) {
                    htmltext = "collecter_yumi_q0114_10.htm";
                } else if (cond == 3 || cond == 4 || cond == 5) {
                    switch (talk) {
                        case 0:
                            htmltext = "collecter_yumi_q0114_11.htm";
                            break;
                        case 1:
                            htmltext = "collecter_yumi_q0114_15.htm";
                            break;
                        default:
                            htmltext = "collecter_yumi_q0114_23.htm";
                            break;
                    }
                } else if (cond == 6) {
                    htmltext = "collecter_yumi_q0114_27.htm";
                } else if (cond == 9 || cond == 12 || cond == 16) {
                    htmltext = "collecter_yumi_q0114_28.htm";
                } else if (cond == 17) {
                    htmltext = "collecter_yumi_q0114_32.htm";
                } else if (cond == 19) {
                    if (talk == 0) {
                        htmltext = "collecter_yumi_q0114_33.htm";
                    } else {
                        htmltext = "collecter_yumi_q0114_34.htm";
                    }
                } else if (cond == 20) {
                    htmltext = "collecter_yumi_q0114_39.htm";
                } else if (cond == 21) {
                    htmltext = "collecter_yumi_q0114_40z.htm";
                } else if (cond == 22 || cond == 26) {
                    htmltext = "collecter_yumi_q0114_41.htm";
                    st.setCond(27);
                    st.playSound("ItemSound.quest_middle");
                } else if (cond == 27) {
                    htmltext = "collecter_yumi_q0114_42.htm";
                }
                break;
            case 32047:
                switch (cond) {
                    case 2:
                        if (talk + talk2 < 2) {
                            htmltext = "chaos_secretary_wendy_q0114_01.htm";
                        } else if (talk + talk2 == 2) {
                            htmltext = "chaos_secretary_wendy_q0114_05.htm";
                        }
                        break;
                    case 3:
                        htmltext = "chaos_secretary_wendy_q0114_06b.htm";
                        break;
                    case 4:
                    case 5:
                        htmltext = "chaos_secretary_wendy_q0114_08.htm";
                        break;
                    case 6:
                        final int choice = st.getInt("choice");
                        switch (choice) {
                            case 1:
                                switch (talk) {
                                    case 0:
                                        htmltext = "chaos_secretary_wendy_q0114_11a.htm";
                                        break;
                                    case 1:
                                        htmltext = "chaos_secretary_wendy_q0114_17c.htm";
                                        break;
                                    default:
                                        htmltext = "chaos_secretary_wendy_q0114_16a.htm";
                                        break;
                                }
                                break;
                            case 2:
                                htmltext = "chaos_secretary_wendy_q0114_11b.htm";
                                break;
                            case 3:
                                switch (talk) {
                                    case 0:
                                        htmltext = "chaos_secretary_wendy_q0114_11c.htm";
                                        break;
                                    case 1:
                                        htmltext = "chaos_secretary_wendy_q0114_12c.htm";
                                        break;
                                    default:
                                        htmltext = "chaos_secretary_wendy_q0114_17c.htm";
                                        break;
                                }
                                break;
                        }
                        break;
                    case 7:
                        switch (talk) {
                            case 0:
                                htmltext = "chaos_secretary_wendy_q0114_11c.htm";
                                break;
                            case 1:
                                htmltext = "chaos_secretary_wendy_q0114_12c.htm";
                                break;
                            default:
                                htmltext = "chaos_secretary_wendy_q0114_17c.htm";
                                break;
                        }
                        break;
                    case 8:
                        htmltext = "chaos_secretary_wendy_q0114_16a.htm";
                        break;
                    case 9:
                        htmltext = "chaos_secretary_wendy_q0114_25c.htm";
                        break;
                    case 10:
                        htmltext = "chaos_secretary_wendy_q0114_18b.htm";
                        break;
                    case 11:
                        htmltext = "chaos_secretary_wendy_q0114_19b.htm";
                        break;
                    case 12:
                        htmltext = "chaos_secretary_wendy_q0114_25c.htm";
                        break;
                    case 13:
                        htmltext = "chaos_secretary_wendy_q0114_20c.htm";
                        break;
                    case 14:
                        htmltext = "chaos_secretary_wendy_q0114_22c.htm";
                        break;
                    case 15:
                        htmltext = "chaos_secretary_wendy_q0114_24c.htm";
                        st.setCond(16);
                        st.playSound("ItemSound.quest_middle");
                        break;
                    case 16:
                        htmltext = "chaos_secretary_wendy_q0114_25c.htm";
                        break;
                    case 20:
                        htmltext = "chaos_secretary_wendy_q0114_26c.htm";
                        break;
                    case 26:
                        htmltext = "chaos_secretary_wendy_q0114_32c.htm";
                        break;
                }
                break;
            case 32050:
                if (cond == 13) {
                    if (talk == 0) {
                        htmltext = "chaos_box2_q0114_01.htm";
                    } else {
                        htmltext = "chaos_box2_q0114_02.htm";
                    }
                } else if (cond == 14) {
                    htmltext = "chaos_box2_q0114_04.htm";
                }
                break;
            case 32046:
                switch (cond) {
                    case 18:
                        htmltext = "pavel_atlanta_q0114_02.htm";
                        break;
                    case 19:
                        htmltext = "pavel_atlanta_q0114_03.htm";
                        break;
                    case 27:
                        htmltext = "pavel_atlanta_q0114_04.htm";
                        break;
                }
                break;
            case 31961:
                if (cond == 21) {
                    htmltext = "head_blacksmith_newyear_q0114_01.htm";
                } else if (cond == 22) {
                    htmltext = "head_blacksmith_newyear_q0114_03.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if (st.getCond() == 10 && npcId == 27318) {
            Functions.npcSay(npc, "This enemy is far too powerful for me to fight. I must withdraw");
            st.setCond(11);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
