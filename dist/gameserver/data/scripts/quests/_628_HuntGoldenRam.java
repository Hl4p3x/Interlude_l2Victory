package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _628_HuntGoldenRam extends Quest {
    private static final int merc_kahmun = 31554;
    private static final int splinter_stakato = 21508;
    private static final int splinter_stakato_worker = 21509;
    private static final int splinter_stakato_soldier = 21510;
    private static final int splinter_stakato_drone = 21511;
    private static final int splinter_stakato_drone_a = 21512;
    private static final int needle_stakato = 21513;
    private static final int needle_stakato_worker = 21514;
    private static final int needle_stakato_soldier = 21515;
    private static final int needle_stakato_drone = 21516;
    private static final int needle_stakato_drone_a = 21517;
    private static final int q_goldenram_badge1 = 7246;
    private static final int q_goldenram_badge2 = 7247;
    private static final int q_splinter_chitin = 7248;
    private static final int q_needle_chitin = 7249;

    public _628_HuntGoldenRam() {
        super(true);
        addStartNpc(31554);
        addKillId(21508, 21509, 21510, 21511, 21512, 21513, 21514, 21515, 21516, 21517);
        addQuestItem(q_splinter_chitin, q_needle_chitin);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(q_goldenram_badge1) < 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "merc_kahmun_q0628_03.htm";
            } else if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L) {
                st.setCond(2);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "merc_kahmun_q0628_04.htm";
            } else if (st.getQuestItemsCount(q_goldenram_badge2) >= 1L) {
                st.setCond(3);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "merc_kahmun_q0628_05.htm";
            }
        } else if ("reply_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(q_goldenram_badge1) < 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) >= 100L) {
                st.setCond(2);
                st.giveItems(q_goldenram_badge1, 1L);
                st.takeItems(q_splinter_chitin, -1L);
                st.getPlayer().updateRam();
                htmltext = "merc_kahmun_q0628_08.htm";
            }
        } else if ("reply_3".equalsIgnoreCase(event)) {
            st.takeItems(q_goldenram_badge1, -1L);
            st.takeItems(q_goldenram_badge2, -1L);
            st.takeItems(q_splinter_chitin, -1L);
            st.takeItems(q_needle_chitin, -1L);
            st.getPlayer().updateRam();
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            htmltext = "merc_kahmun_q0628_13.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31554) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 66) {
                    htmltext = "merc_kahmun_q0628_01.htm";
                    break;
                }
                htmltext = "merc_kahmun_q0628_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 31554) {
                    break;
                }
                if (st.getQuestItemsCount(q_goldenram_badge1) < 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                    htmltext = "merc_kahmun_q0628_06.htm";
                    break;
                }
                if (st.getQuestItemsCount(q_goldenram_badge1) < 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) >= 100L) {
                    htmltext = "merc_kahmun_q0628_07.htm";
                    break;
                }
                if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L && (st.getQuestItemsCount(q_splinter_chitin) < 100L || st.getQuestItemsCount(q_needle_chitin) < 100L)) {
                    htmltext = "merc_kahmun_q0628_09.htm";
                    break;
                }
                if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) >= 100L && st.getQuestItemsCount(q_needle_chitin) >= 100L) {
                    st.setCond(3);
                    st.giveItems(q_goldenram_badge2, 1L);
                    st.takeItems(q_goldenram_badge1, -1L);
                    st.takeItems(q_splinter_chitin, -1L);
                    st.takeItems(q_needle_chitin, -1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "merc_kahmun_q0628_10.htm";
                    break;
                }
                if (st.getQuestItemsCount(q_goldenram_badge2) >= 1L) {
                    htmltext = "merc_kahmun_q0628_11.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if (npcId == 21508) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                final int i0 = Rnd.get(100);
                if (i0 < 50) {
                    if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L) {
                        if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21509) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                final int i0 = Rnd.get(100);
                if (i0 < 43) {
                    if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L) {
                        if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21510) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                final int i0 = Rnd.get(1000);
                if (i0 < 521) {
                    if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L) {
                        if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21511) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                final int i0 = Rnd.get(1000);
                if (i0 < 575) {
                    if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L) {
                        if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21512) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_splinter_chitin) < 100L) {
                final int i0 = Rnd.get(1000);
                if (i0 < 746) {
                    if (st.getQuestItemsCount(q_goldenram_badge1) >= 1L) {
                        if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_middle");
                        } else {
                            st.rollAndGive(q_splinter_chitin, 1, 100.0);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    } else if (st.getQuestItemsCount(q_splinter_chitin) >= 99L) {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_splinter_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21513) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_needle_chitin) < 100L) {
                final int i0 = Rnd.get(100);
                if (i0 < 50) {
                    if (st.getQuestItemsCount(q_needle_chitin) >= 99L) {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21514) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_needle_chitin) < 100L) {
                final int i0 = Rnd.get(100);
                if (i0 < 43) {
                    if (st.getQuestItemsCount(q_needle_chitin) >= 99L) {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21515) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_needle_chitin) < 100L) {
                final int i0 = Rnd.get(100);
                if (i0 < 52) {
                    if (st.getQuestItemsCount(q_needle_chitin) >= 99L) {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21516) {
            if (st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_needle_chitin) < 100L) {
                final int i0 = Rnd.get(1000);
                if (i0 < 531) {
                    if (st.getQuestItemsCount(q_needle_chitin) >= 99L) {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.rollAndGive(q_needle_chitin, 1, 100.0);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        } else if (npcId == 21517 && st.getQuestItemsCount(q_goldenram_badge2) < 1L && st.getQuestItemsCount(q_goldenram_badge1) >= 1L && st.getQuestItemsCount(q_needle_chitin) < 100L) {
            final int i0 = Rnd.get(1000);
            if (i0 < 744) {
                if (st.getQuestItemsCount(q_needle_chitin) >= 99L) {
                    st.rollAndGive(q_needle_chitin, 1, 100.0);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.rollAndGive(q_needle_chitin, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
