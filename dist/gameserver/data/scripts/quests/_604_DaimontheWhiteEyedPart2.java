package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;

public class _604_DaimontheWhiteEyedPart2 extends Quest {
    private static final int eye_of_argos = 31683;
    private static final int daimons_altar = 31541;
    private static final int daemon_of_hundred_eyes = 25290;
    private static final int q_unfinished_s_crystal = 7192;
    private static final int q_summon_crystal = 7193;
    private static final int q_essense_of_daimon = 7194;
    private static final int dye_i2m2_c = 4595;
    private static final int dye_i2w2_c = 4596;
    private static final int dye_m2i2_c = 4597;
    private static final int dye_m2w2_c = 4598;
    private static final int dye_w2i2_c = 4599;
    private static final int dye_w2m2_c = 4600;

    public _604_DaimontheWhiteEyedPart2() {
        super(true);
        addStartNpc(31683);
        addTalkId(31541);
        addKillId(25290);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int spawned_daemon_of_hundred_eyes = st.getInt("spawned_daemon_of_hundred_eyes");
        final int GetHTMLCookie = st.getInt("daemon_of_hundred_eyes_second_cookie");
        final int npcId = npc.getNpcId();
        if (npcId == 31683) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("daemon_of_hundred_eyes_second", String.valueOf(11), true);
                st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
                st.takeItems(7192, -1L);
                st.giveItems(7193, 1L);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "eye_of_argos_q0604_0104.htm";
            } else if ("reply_3".equalsIgnoreCase(event) && GetHTMLCookie == 2) {
                if (st.getQuestItemsCount(7194) >= 1L) {
                    final int i1 = Rnd.get(1000);
                    st.takeItems(7194, 1L);
                    if (i1 < 167) {
                        st.giveItems(4595, 5L);
                    } else if (i1 < 334) {
                        st.giveItems(4596, 5L);
                    } else if (i1 < 501) {
                        st.giveItems(4597, 5L);
                    } else if (i1 < 668) {
                        st.giveItems(4598, 5L);
                    } else if (i1 < 835) {
                        st.giveItems(4599, 5L);
                    } else if (i1 < 1000) {
                        st.giveItems(4600, 5L);
                    }
                    st.unset("daemon_of_hundred_eyes_second");
                    st.unset("daemon_of_hundred_eyes_second_cookie");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    htmltext = "eye_of_argos_q0604_0301.htm";
                } else {
                    htmltext = "eye_of_argos_q0604_0302.htm";
                }
            }
        } else if (npcId == 31541) {
            if ("60401".equalsIgnoreCase(event)) {
                Functions.npcSay(npc, "Can light exist without darkness?");
                st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
                if (npc != null) {
                    npc.deleteMe();
                }
                return null;
            }
            if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 1) {
                if (st.getQuestItemsCount(7193) >= 1L) {
                    if (spawned_daemon_of_hundred_eyes == 0) {
                        st.setCond(2);
                        st.set("daemon_of_hundred_eyes_second", String.valueOf(21), true);
                        st.takeItems(7193, 1L);
                        st.set("spawned_daemon_of_hundred_eyes", String.valueOf(1), true);
                        final NpcInstance daemon_eyes = st.addSpawn(25290, 186320, -43904, -3175);
                        Functions.npcSay(daemon_eyes, "Who is calling me?");
                        st.startQuestTimer("60401", 1200000L, daemon_eyes);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "daimons_altar_q0604_0201.htm";
                    } else {
                        htmltext = "daimons_altar_q0604_0202.htm";
                    }
                } else {
                    htmltext = "daimons_altar_q0604_0203.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("daemon_of_hundred_eyes_second");
        final int spawned_daemon_of_hundred_eyes = st.getInt("spawned_daemon_of_hundred_eyes");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31683) {
                    break;
                }
                if (st.getPlayer().getLevel() < 73) {
                    htmltext = "eye_of_argos_q0604_0103.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getQuestItemsCount(7192) >= 1L) {
                    htmltext = "eye_of_argos_q0604_0101.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0604_0102.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId == 31683) {
                    if (GetMemoState == 11) {
                        htmltext = "eye_of_argos_q0604_0105.htm";
                        break;
                    }
                    if (GetMemoState < 22) {
                        break;
                    }
                    if (st.getQuestItemsCount(7194) >= 1L) {
                        st.set("daemon_of_hundred_eyes_second_cookie", String.valueOf(2), true);
                        htmltext = "eye_of_argos_q0604_0201.htm";
                        break;
                    }
                    htmltext = "eye_of_argos_q0604_0202.htm";
                    break;
                } else {
                    if (npcId != 31541) {
                        break;
                    }
                    if (st.getQuestItemsCount(7193) >= 1L && GetMemoState == 11) {
                        st.set("daemon_of_hundred_eyes_second_cookie", String.valueOf(1), true);
                        htmltext = "daimons_altar_q0604_0101.htm";
                        break;
                    }
                    if (GetMemoState == 21) {
                        if (spawned_daemon_of_hundred_eyes == 0) {
                            st.set("spawned_daemon_of_hundred_eyes", String.valueOf(1), true);
                            final NpcInstance daemon_eyes = st.addSpawn(25290, 186320, -43904, -3175);
                            Functions.npcSay(daemon_eyes, "Who is calling me?");
                            st.startQuestTimer("60401", 1200000L, daemon_eyes);
                            htmltext = "daimons_altar_q0604_0201.htm";
                            break;
                        }
                        htmltext = "daimons_altar_q0604_0202.htm";
                        break;
                    } else {
                        if (GetMemoState >= 22) {
                            htmltext = "daimons_altar_q0604_0204.htm";
                            break;
                        }
                        break;
                    }
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("daemon_of_hundred_eyes_second");
        final int npcId = npc.getNpcId();
        if (GetMemoState >= 11 && GetMemoState <= 21 && npcId == 25290) {
            final int i4 = Rnd.get(1000);
            if (i4 < 1000) {
                if (st.getQuestItemsCount(7194) + 1L >= 1L) {
                    st.setCond(3);
                    st.set("daemon_of_hundred_eyes_second", String.valueOf(22), true);
                    st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
                    st.giveItems(7194, 1L - st.getQuestItemsCount(7194));
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(7194, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
