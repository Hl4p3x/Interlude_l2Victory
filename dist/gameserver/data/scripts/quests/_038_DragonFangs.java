package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _038_DragonFangs extends Quest {
    private static final int magister_rohmer = 30344;
    private static final int guard_luis = 30386;
    private static final int iris = 30034;
    private static final int langk_lizardman_sub_ldr = 20357;
    private static final int langk_lizardman_sentinel = 21100;
    private static final int langk_lizardman_leader = 20356;
    private static final int langk_lizardman_shaman = 21101;
    private static final int q_liz_feather = 7173;
    private static final int q_liz_totem_tooth1 = 7174;
    private static final int q_liz_totem_tooth2 = 7175;
    private static final int q_liz_letter1 = 7176;
    private static final int q_liz_letter2 = 7177;
    private static final int bone_helmet = 45;
    private static final int aspis = 627;
    private static final int leather_gauntlet = 605;
    private static final int blue_buckskin_boots = 1123;

    public _038_DragonFangs() {
        super(false);
        addStartNpc(30386);
        addTalkId(30034, 30344);
        addKillId(20357, 21100, 20356, 21101);
        addQuestItem(7173, 7174, 7175, 7176, 7177);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetHTMLCookie = st.getInt("tooth_of_dragon_cookie");
        final int npcId = npc.getNpcId();
        if (npcId == 30386) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("tooth_of_dragon", String.valueOf(11), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "guard_luis_q0038_0104.htm";
            } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 1) {
                if (st.getQuestItemsCount(7173) >= 100L) {
                    st.setCond(3);
                    st.set("tooth_of_dragon", String.valueOf(21), true);
                    st.takeItems(7173, 100L);
                    st.giveItems(7174, 1L);
                    htmltext = "guard_luis_q0038_0201.htm";
                    st.playSound("ItemSound.quest_middle");
                } else {
                    htmltext = "guard_luis_q0038_0202.htm";
                }
            }
        } else if (npcId == 30034) {
            if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 2) {
                if (st.getQuestItemsCount(7174) >= 1L) {
                    st.setCond(4);
                    st.set("tooth_of_dragon", String.valueOf(31), true);
                    st.takeItems(7174, 1L);
                    st.giveItems(7176, 1L);
                    htmltext = "iris_q0038_0301.htm";
                    st.playSound("ItemSound.quest_middle");
                } else {
                    htmltext = "iris_q0038_0302.htm";
                }
            } else if ("reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 4) {
                if (st.getQuestItemsCount(7177) >= 1L) {
                    st.setCond(6);
                    st.set("tooth_of_dragon", String.valueOf(51), true);
                    st.takeItems(7177, 1L);
                    htmltext = "iris_q0038_0501.htm";
                    st.playSound("ItemSound.quest_middle");
                } else {
                    htmltext = "iris_q0038_0502.htm";
                }
            } else if ("reply_3".equalsIgnoreCase(event) && GetHTMLCookie == 5) {
                if (st.getQuestItemsCount(7175) >= 50L) {
                    final int i1 = Rnd.get(1000);
                    st.takeItems(7175, -1L);
                    st.addExpAndSp(435117L, 23977L);
                    if (i1 < 250) {
                        st.giveItems(45, 1L);
                        st.giveItems(57, 5200L);
                    } else if (i1 < 500) {
                        st.giveItems(627, 1L);
                        st.giveItems(57, 1500L);
                    } else if (i1 < 750) {
                        st.giveItems(1123, 1L);
                        st.giveItems(57, 3200L);
                    } else if (i1 < 1000) {
                        st.giveItems(605, 1L);
                        st.giveItems(57, 3200L);
                    }
                    st.unset("tooth_of_dragon");
                    st.unset("tooth_of_dragon_cookie");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    htmltext = "iris_q0038_0601.htm";
                } else {
                    htmltext = "iris_q0038_0602.htm";
                }
            }
        } else if (npcId == 30344 && "reply_1".equalsIgnoreCase(event) && GetHTMLCookie == 3) {
            if (st.getQuestItemsCount(7176) >= 1L) {
                st.setCond(5);
                st.set("tooth_of_dragon", String.valueOf(41), true);
                st.takeItems(7176, 1L);
                st.giveItems(7177, 1L);
                htmltext = "magister_rohmer_q0038_0401.htm";
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "magister_rohmer_q0038_0402.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("tooth_of_dragon");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30386) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 19) {
                    htmltext = "guard_luis_q0038_0101.htm";
                    break;
                }
                htmltext = "guard_luis_q0038_0103.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 30386:
                        if (GetMemoState >= 11 && GetMemoState <= 12) {
                            if (GetMemoState == 12 && st.getQuestItemsCount(7173) >= 100L) {
                                st.set("tooth_of_dragon_cookie", String.valueOf(1), true);
                                htmltext = "guard_luis_q0038_0105.htm";
                                break;
                            }
                            htmltext = "guard_luis_q0038_0106.htm";
                            break;
                        } else {
                            if (GetMemoState == 21) {
                                htmltext = "guard_luis_q0038_0203.htm";
                                break;
                            }
                            break;
                        }
                    case 30034:
                        if (st.getQuestItemsCount(7174) >= 1L && GetMemoState == 21) {
                            st.set("tooth_of_dragon_cookie", String.valueOf(2), true);
                            htmltext = "iris_q0038_0201.htm";
                            break;
                        }
                        if (GetMemoState == 31) {
                            htmltext = "iris_q0038_0303.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(7177) >= 1L && GetMemoState == 41) {
                            st.set("tooth_of_dragon_cookie", String.valueOf(4), true);
                            htmltext = "iris_q0038_0401.htm";
                            break;
                        }
                        if (GetMemoState > 52 || GetMemoState < 51) {
                            break;
                        }
                        if (GetMemoState == 52 && st.getQuestItemsCount(7175) >= 50L) {
                            st.set("tooth_of_dragon_cookie", String.valueOf(5), true);
                            htmltext = "iris_q0038_0503.htm";
                            break;
                        }
                        htmltext = "iris_q0038_0504.htm";
                        break;
                    default:
                        if (npcId != 30344) {
                            break;
                        }
                        if (st.getQuestItemsCount(7176) >= 1L && GetMemoState == 31) {
                            st.set("tooth_of_dragon_cookie", String.valueOf(3), true);
                            htmltext = "magister_rohmer_q0038_0301.htm";
                            break;
                        }
                        if (GetMemoState == 41) {
                            htmltext = "magister_rohmer_q0038_0403.htm";
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
        final int GetMemoState = st.getInt("tooth_of_dragon");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11) {
            if (npcId == 20357 || npcId == 21100) {
                if (st.getQuestItemsCount(7173) + 1L >= 100L) {
                    if (st.getQuestItemsCount(7173) < 100L) {
                        st.giveItems(7173, 100L - st.getQuestItemsCount(7173));
                        st.playSound("ItemSound.quest_middle");
                    }
                    st.setCond(2);
                    st.set("tooth_of_dragon", String.valueOf(12), true);
                } else {
                    st.giveItems(7173, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (GetMemoState == 51 && (npcId == 20356 || npcId == 21101)) {
            final int i4 = Rnd.get(1000);
            if (i4 < 500) {
                if (st.getQuestItemsCount(7175) + 1L >= 50L) {
                    if (st.getQuestItemsCount(7175) < 50L) {
                        st.giveItems(7175, 50L - st.getQuestItemsCount(7175));
                        st.playSound("ItemSound.quest_middle");
                    }
                    st.setCond(7);
                    st.set("tooth_of_dragon", String.valueOf(52), true);
                } else {
                    st.giveItems(7175, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
