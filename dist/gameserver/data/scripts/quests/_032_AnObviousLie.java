package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _032_AnObviousLie extends Quest {
    private static final int maximilian = 30120;
    private static final int gentler = 30094;
    private static final int miki_the_cat = 31706;
    private static final int crocodile = 20135;
    private static final int q_map_of_gentler = 7165;
    private static final int q_medicinal_herb = 7166;
    private static final int spirit_ore = 3031;
    private static final int thread = 1868;
    private static final int suede = 1866;
    private static final int racoon_ear = 7680;
    private static final int cat_ear = 6843;
    private static final int rabbit_ear = 7683;

    public _032_AnObviousLie() {
        super(true);
        addStartNpc(30120);
        addTalkId(30120, 30094, 31706);
        addKillId(20135);
        addQuestItem(7166, 7165);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetHTMLCookie = st.getInt("blatant_lie_cookie");
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 30120:
                if ("quest_accept".equalsIgnoreCase(event)) {
                    st.setCond(1);
                    st.set("blatant_lie", String.valueOf(11), true);
                    st.setState(2);
                    st.playSound("ItemSound.quest_accept");
                    htmltext = "maximilian_q0032_0104.htm";
                }
                break;
            case 30094:
                if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 1) {
                    st.setCond(2);
                    st.set("blatant_lie", String.valueOf(21), true);
                    st.giveItems(7165, 1L);
                    htmltext = "gentler_q0032_0201.htm";
                    st.playSound("ItemSound.quest_middle");
                } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 3) {
                    if (st.getQuestItemsCount(7166) >= 20L) {
                        st.setCond(5);
                        st.set("blatant_lie", String.valueOf(41), true);
                        st.takeItems(7166, 20L);
                        htmltext = "gentler_q0032_0401.htm";
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        htmltext = "gentler_q0032_0402.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 4) {
                    if (st.getQuestItemsCount(3031) >= 500L) {
                        st.setCond(6);
                        st.set("blatant_lie", String.valueOf(51), true);
                        st.takeItems(3031, 500L);
                        htmltext = "gentler_q0032_0501.htm";
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        htmltext = "gentler_q0032_0502.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 6) {
                    st.setCond(8);
                    st.set("blatant_lie", String.valueOf(71), true);
                    htmltext = "gentler_q0032_0701.htm";
                    st.playSound("ItemSound.quest_middle");
                } else if ("reply_3".equalsIgnoreCase(event) && GetHTMLCookie == 7) {
                    htmltext = "gentler_q0032_0801.htm";
                } else if ("reply_11".equalsIgnoreCase(event) && GetHTMLCookie == 7) {
                    if (st.getQuestItemsCount(1868) >= 1000L && st.getQuestItemsCount(1866) >= 500L) {
                        st.takeItems(1868, 1000L);
                        st.takeItems(1866, 500L);
                        st.giveItems(6843, 1L);
                        st.unset("blatant_lie");
                        st.unset("blatant_lie_cookie");
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(false);
                        htmltext = "gentler_q0032_0802.htm";
                    } else {
                        htmltext = "gentler_q0032_0803.htm";
                    }
                } else if ("reply_12".equalsIgnoreCase(event) && GetHTMLCookie == 7) {
                    if (st.getQuestItemsCount(1868) >= 1000L && st.getQuestItemsCount(1866) >= 500L) {
                        st.takeItems(1868, 1000L);
                        st.takeItems(1866, 500L);
                        st.giveItems(7680, 1L);
                        st.unset("blatant_lie");
                        st.unset("blatant_lie_cookie");
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(false);
                        htmltext = "gentler_q0032_0802.htm";
                    } else {
                        htmltext = "gentler_q0032_0803.htm";
                    }
                } else if ("reply_13".equalsIgnoreCase(event) && GetHTMLCookie == 7) {
                    if (st.getQuestItemsCount(1868) >= 1000L && st.getQuestItemsCount(1866) >= 500L) {
                        st.takeItems(1868, 1000L);
                        st.takeItems(1866, 500L);
                        st.giveItems(7683, 1L);
                        st.unset("blatant_lie");
                        st.unset("blatant_lie_cookie");
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(false);
                        htmltext = "gentler_q0032_0802.htm";
                    } else {
                        htmltext = "gentler_q0032_0803.htm";
                    }
                }
                break;
            case 31706:
                if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 2) {
                    st.setCond(3);
                    st.set("blatant_lie", String.valueOf(31), true);
                    st.takeItems(7165, 1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "miki_the_cat_q0032_0301.htm";
                } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 5) {
                    st.setCond(7);
                    st.set("blatant_lie", String.valueOf(61), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "miki_the_cat_q0032_0601.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("blatant_lie");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30120) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 45) {
                    htmltext = "maximilian_q0032_0101.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "maximilian_q0032_0103.htm";
                break;
            }
            case 2: {
                if (npcId == 30120) {
                    if (GetMemoState == 11) {
                        htmltext = "maximilian_q0032_0105.htm";
                        break;
                    }
                    break;
                } else if (npcId == 30094) {
                    if (GetMemoState == 11) {
                        st.set("blatant_lie_cookie", String.valueOf(1), true);
                        htmltext = "gentler_q0032_0101.htm";
                        break;
                    }
                    if (GetMemoState == 21) {
                        htmltext = "gentler_q0032_0202.htm";
                        break;
                    }
                    switch (GetMemoState) {
                        case 32:
                            if (st.getQuestItemsCount(7166) >= 20L) {
                                st.set("blatant_lie_cookie", String.valueOf(3), true);
                                htmltext = "gentler_q0032_0301.htm";
                                break;
                            }
                            htmltext = "gentler_q0032_0302.htm";
                            break;
                        case 41:
                            if (st.getQuestItemsCount(3031) >= 500L) {
                                st.set("blatant_lie_cookie", String.valueOf(4), true);
                                htmltext = "gentler_q0032_0403.htm";
                                break;
                            }
                            htmltext = "gentler_q0032_0404.htm";
                            break;
                        default:
                            if (GetMemoState == 51) {
                                htmltext = "gentler_q0032_0503.htm";
                                break;
                            }
                            if (GetMemoState == 61) {
                                st.set("blatant_lie_cookie", String.valueOf(6), true);
                                htmltext = "gentler_q0032_0601.htm";
                                break;
                            }
                            if (GetMemoState != 71) {
                                break;
                            }
                            if (st.getQuestItemsCount(1868) >= 1000L && st.getQuestItemsCount(1866) >= 500L) {
                                st.set("blatant_lie_cookie", String.valueOf(7), true);
                                htmltext = "gentler_q0032_0702.htm";
                                break;
                            }
                            htmltext = "gentler_q0032_0703.htm";
                            break;
                    }
                } else {
                    if (npcId != 31706) {
                        break;
                    }
                    if (st.getQuestItemsCount(7165) >= 1L && GetMemoState == 21) {
                        st.set("blatant_lie_cookie", String.valueOf(2), true);
                        htmltext = "miki_the_cat_q0032_0201.htm";
                        break;
                    }
                    if (GetMemoState == 31) {
                        htmltext = "miki_the_cat_q0032_0303.htm";
                        break;
                    }
                    if (GetMemoState == 51) {
                        st.set("blatant_lie_cookie", String.valueOf(5), true);
                        htmltext = "miki_the_cat_q0032_0501.htm";
                        break;
                    }
                    if (GetMemoState == 61) {
                        htmltext = "miki_the_cat_q0032_0602.htm";
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
        final int GetMemoState = st.getInt("blatant_lie");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 31 && npcId == 20135) {
            final int i4 = Rnd.get(500);
            if (i4 < 500) {
                if (st.getQuestItemsCount(7166) + 1L >= 20L) {
                    if (st.getQuestItemsCount(7166) <= 20L) {
                        st.giveItems(7166, 20L - st.getQuestItemsCount(7166));
                        st.playSound("ItemSound.quest_middle");
                    }
                    st.setCond(4);
                    st.set("blatant_lie", String.valueOf(32), true);
                } else {
                    st.giveItems(7166, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
