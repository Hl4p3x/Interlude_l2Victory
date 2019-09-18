package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _644_GraveRobberAnnihilation extends Quest {
    private static final int karuda = 32017;
    private static final int thief_patroller_archer = 22003;
    private static final int thief_patroller = 22004;
    private static final int thief_guard = 22005;
    private static final int grave_robber_swordscout = 22006;
    private static final int thief_fighter = 22008;
    private static final int q_grave_goods_of_orc = 8088;
    private static final int varnish = 1865;
    private static final int animal_skin = 1867;
    private static final int animal_bone = 1872;
    private static final int charcoal = 1871;
    private static final int coal = 1870;
    private static final int iron_ore = 1869;

    public _644_GraveRobberAnnihilation() {
        super(false);
        addStartNpc(32017);
        addKillId(22003, 22004, 22005, 22006, 22008);
        addQuestItem(8088);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("sweep_the_snatcher");
        final int GetHTMLCookie = st.getInt("sweep_the_snatcher_cookie");
        final int npcId = npc.getNpcId();
        if (npcId == 32017) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("sweep_the_snatcher", String.valueOf(11), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "karuda_q0644_0103.htm";
            } else if ("reply_3".equalsIgnoreCase(event) && GetHTMLCookie == 1 && GetMemoState >= 11) {
                htmltext = "karuda_q0644_0201.htm";
            } else if (GetMemoState >= 11 && st.getQuestItemsCount(8088) >= 120L) {
                st.takeItems(8088, 120L);
                if ("reply_11".equalsIgnoreCase(event)) {
                    st.giveItems(1865, 30L);
                } else if ("reply_12".equalsIgnoreCase(event)) {
                    st.giveItems(1867, 40L);
                } else if ("reply_13".equalsIgnoreCase(event)) {
                    st.giveItems(1872, 40L);
                } else if ("reply_14".equalsIgnoreCase(event)) {
                    st.giveItems(1871, 30L);
                } else if ("reply_15".equalsIgnoreCase(event)) {
                    st.giveItems(1870, 30L);
                } else if ("reply_16".equalsIgnoreCase(event)) {
                    st.giveItems(1869, 30L);
                }
                st.unset("sweep_the_snatcher");
                st.unset("sweep_the_snatcher_cookie");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "karuda_q0644_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("sweep_the_snatcher");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32017) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 20) {
                    htmltext = "karuda_q0644_0101.htm";
                    break;
                }
                htmltext = "karuda_q0644_0102.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 32017 || GetMemoState < 11 || GetMemoState > 12) {
                    break;
                }
                if (GetMemoState == 12 && st.getQuestItemsCount(8088) >= 120L) {
                    st.set("sweep_the_snatcher_cookie", String.valueOf(1), true);
                    htmltext = "karuda_q0644_0105.htm";
                    break;
                }
                htmltext = "karuda_q0644_0106.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("sweep_the_snatcher");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11) {
            switch (npcId) {
                case 22003: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 714) {
                        if (st.getQuestItemsCount(8088) + 1L >= 120L) {
                            if (st.getQuestItemsCount(8088) < 120L) {
                                st.giveItems(8088, 120L - st.getQuestItemsCount(8088));
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.setCond(2);
                            st.set("sweep_the_snatcher", String.valueOf(12), true);
                        } else {
                            st.giveItems(8088, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 22004: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 841) {
                        if (st.getQuestItemsCount(8088) + 1L >= 120L) {
                            if (st.getQuestItemsCount(8088) < 120L) {
                                st.giveItems(8088, 120L - st.getQuestItemsCount(8088));
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.setCond(2);
                            st.set("sweep_the_snatcher", String.valueOf(12), true);
                        } else {
                            st.giveItems(8088, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 22005: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 746) {
                        if (st.getQuestItemsCount(8088) + 1L >= 120L) {
                            if (st.getQuestItemsCount(8088) < 120L) {
                                st.giveItems(8088, 120L - st.getQuestItemsCount(8088));
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.setCond(2);
                            st.set("sweep_the_snatcher", String.valueOf(12), true);
                        } else {
                            st.giveItems(8088, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 22006: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 778) {
                        if (st.getQuestItemsCount(8088) + 1L >= 120L) {
                            if (st.getQuestItemsCount(8088) < 120L) {
                                st.giveItems(8088, 120L - st.getQuestItemsCount(8088));
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.setCond(2);
                            st.set("sweep_the_snatcher", String.valueOf(12), true);
                        } else {
                            st.giveItems(8088, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 22008: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 810) {
                        if (st.getQuestItemsCount(8088) + 1L >= 120L) {
                            if (st.getQuestItemsCount(8088) < 120L) {
                                st.giveItems(8088, 120L - st.getQuestItemsCount(8088));
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.setCond(2);
                            st.set("sweep_the_snatcher", String.valueOf(12), true);
                        } else {
                            st.giveItems(8088, 1L);
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
