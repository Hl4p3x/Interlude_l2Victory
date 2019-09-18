package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _603_DaimontheWhiteEyedPart1 extends Quest {
    private static final int eye_of_argos = 31683;
    private static final int ancient_lithography1 = 31548;
    private static final int ancient_lithography2 = 31549;
    private static final int ancient_lithography3 = 31550;
    private static final int ancient_lithography4 = 31551;
    private static final int ancient_lithography5 = 31552;
    private static final int buffalo_slave = 21299;
    private static final int bandersnatch_slave = 21297;
    private static final int grendel_slave = 21304;
    private static final int q_dark_evil_spirit = 7190;
    private static final int q_broken_crystal = 7191;
    private static final int q_unfinished_s_crystal = 7192;

    public _603_DaimontheWhiteEyedPart1() {
        super(true);
        addStartNpc(31683);
        addTalkId(31548, 31549, 31550, 31551, 31552);
        addKillId(21299, 21297, 21304);
        addQuestItem(7190);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 31683) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("daemon_of_hundred_eyes_first", String.valueOf(11), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "eye_of_argos_q0603_0104.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(7191) >= 5L) {
                    st.setCond(7);
                    st.set("daemon_of_hundred_eyes_first", String.valueOf(71), true);
                    st.takeItems(7191, 5L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "eye_of_argos_q0603_0701.htm";
                } else {
                    htmltext = "eye_of_argos_q0603_0702.htm";
                }
            } else if ("reply_3".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(7190) >= 200L) {
                    st.takeItems(7190, -1L);
                    st.giveItems(7192, 1L);
                    st.unset("daemon_of_hundred_eyes_first");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    htmltext = "eye_of_argos_q0603_0801.htm";
                } else {
                    htmltext = "eye_of_argos_q0603_0802.htm";
                }
            }
        } else if (npcId == 31548) {
            if ("reply_1".equalsIgnoreCase(event)) {
                st.setCond(2);
                st.set("daemon_of_hundred_eyes_first", String.valueOf(21), true);
                st.giveItems(7191, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "ancient_lithography1_q0603_0201.htm";
            }
        } else if (npcId == 31549) {
            if ("reply_1".equalsIgnoreCase(event)) {
                st.setCond(3);
                st.set("daemon_of_hundred_eyes_first", String.valueOf(31), true);
                st.giveItems(7191, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "ancient_lithography2_q0603_0301.htm";
            }
        } else if (npcId == 31550) {
            if ("reply_1".equalsIgnoreCase(event)) {
                st.setCond(4);
                st.set("daemon_of_hundred_eyes_first", String.valueOf(41), true);
                st.giveItems(7191, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "ancient_lithography3_q0603_0401.htm";
            }
        } else if (npcId == 31551) {
            if ("reply_1".equalsIgnoreCase(event)) {
                st.setCond(5);
                st.set("daemon_of_hundred_eyes_first", String.valueOf(51), true);
                st.giveItems(7191, 1L);
                st.playSound("ItemSound.quest_middle");
                htmltext = "ancient_lithography4_q0603_0501.htm";
            }
        } else if (npcId == 31552 && "reply_1".equalsIgnoreCase(event)) {
            st.setCond(6);
            st.set("daemon_of_hundred_eyes_first", String.valueOf(61), true);
            st.giveItems(7191, 1L);
            st.playSound("ItemSound.quest_middle");
            htmltext = "ancient_lithography5_q0603_0601.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("daemon_of_hundred_eyes_first");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31683) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 73) {
                    htmltext = "eye_of_argos_q0603_0101.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0603_0103.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 31683:
                        if (GetMemoState == 11) {
                            htmltext = "eye_of_argos_q0603_0105.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(7191) >= 1L && GetMemoState == 61) {
                            htmltext = "eye_of_argos_q0603_0601.htm";
                            break;
                        }
                        if (GetMemoState > 72 || GetMemoState < 71) {
                            break;
                        }
                        if (GetMemoState == 72 && st.getQuestItemsCount(7190) >= 200L) {
                            htmltext = "eye_of_argos_q0603_0703.htm";
                            break;
                        }
                        htmltext = "eye_of_argos_q0603_0704.htm";
                        break;
                    case 31548:
                        if (GetMemoState == 11) {
                            htmltext = "ancient_lithography1_q0603_0101.htm";
                            break;
                        }
                        if (GetMemoState == 21) {
                            htmltext = "ancient_lithography1_q0603_0203.htm";
                            break;
                        }
                        break;
                    case 31549:
                        if (st.getQuestItemsCount(7191) >= 1L && GetMemoState == 21) {
                            htmltext = "ancient_lithography2_q0603_0201.htm";
                            break;
                        }
                        if (GetMemoState == 31) {
                            htmltext = "ancient_lithography2_q0603_0303.htm";
                            break;
                        }
                        break;
                    case 31550:
                        if (st.getQuestItemsCount(7191) >= 1L && GetMemoState == 31) {
                            htmltext = "ancient_lithography3_q0603_0301.htm";
                            break;
                        }
                        if (GetMemoState == 41) {
                            htmltext = "ancient_lithography3_q0603_0403.htm";
                            break;
                        }
                        break;
                    case 31551:
                        if (st.getQuestItemsCount(7191) >= 1L && GetMemoState == 41) {
                            htmltext = "ancient_lithography4_q0603_0401.htm";
                            break;
                        }
                        if (GetMemoState == 51) {
                            htmltext = "ancient_lithography4_q0603_0503.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 31552) {
                            break;
                        }
                        if (st.getQuestItemsCount(7191) >= 1L && GetMemoState == 51) {
                            htmltext = "ancient_lithography5_q0603_0501.htm";
                            break;
                        }
                        if (GetMemoState == 61) {
                            htmltext = "ancient_lithography5_q0603_0603.htm";
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
        final int GetMemoState = st.getInt("daemon_of_hundred_eyes_first");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 71) {
            switch (npcId) {
                case 21299: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 519) {
                        if (st.getQuestItemsCount(7190) + 1L >= 200L) {
                            if (st.getQuestItemsCount(7190) < 200L) {
                                st.setCond(8);
                                st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
                                st.giveItems(7190, 200L - st.getQuestItemsCount(7190));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(7190, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21297: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 500) {
                        if (st.getQuestItemsCount(7190) + 1L >= 200L) {
                            if (st.getQuestItemsCount(7190) < 200L) {
                                st.setCond(8);
                                st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
                                st.giveItems(7190, 200L - st.getQuestItemsCount(7190));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(7190, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21304: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 673) {
                        if (st.getQuestItemsCount(7190) + 1L >= 200L) {
                            if (st.getQuestItemsCount(7190) < 200L) {
                                st.setCond(8);
                                st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
                                st.giveItems(7190, 200L - st.getQuestItemsCount(7190));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(7190, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
}
