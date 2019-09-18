package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _601_WatchingEyes extends Quest {
    private static final int eye_of_argos = 31683;
    private static final int apostle_defender = 21306;
    private static final int apostle_avenger = 21308;
    private static final int apostle_avenger_a = 21309;
    private static final int apostle_magistrate = 21310;
    private static final int apostle_magistrate_a = 21311;
    private static final int sealed_ring_of_aurakyria_gem = 6699;
    private static final int sealed_sanddragons_earing_piece = 6698;
    private static final int sealed_dragon_necklace_wire = 6700;
    private static final int q_proof_of_avenger = 7188;

    public _601_WatchingEyes() {
        super(false);
        addStartNpc(31683);
        addKillId(21306, 21308, 21309, 21310, 21311);
        addQuestItem(q_proof_of_avenger);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("argoss_favor", String.valueOf(11), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "eye_of_argos_q0601_0104.htm";
        } else if ("reply_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(q_proof_of_avenger) >= 100L) {
                final int i1 = Rnd.get(1000);
                st.takeItems(q_proof_of_avenger, 100L);
                if (i1 < 200) {
                    st.giveItems(6699, 5L);
                    st.giveItems(57, 90000L);
                    st.addExpAndSp(120000L, 10000L);
                } else if (i1 < 400) {
                    st.giveItems(6698, 5L);
                    st.giveItems(57, 80000L);
                    st.addExpAndSp(120000L, 10000L);
                } else if (i1 < 500) {
                    st.giveItems(6700, 5L);
                    st.giveItems(57, 40000L);
                    st.addExpAndSp(120000L, 10000L);
                } else if (i1 < 1000) {
                    st.giveItems(57, 230000L);
                }
                st.unset("argoss_favor");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "eye_of_argos_q0601_0201.htm";
            } else {
                htmltext = "eye_of_argos_q0601_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("argoss_favor");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31683) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 71) {
                    htmltext = "eye_of_argos_q0601_0101.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0601_0103.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 31683 || GetMemoState < 11 || GetMemoState > 12) {
                    break;
                }
                if (GetMemoState == 12 && st.getQuestItemsCount(q_proof_of_avenger) >= 100L) {
                    htmltext = "eye_of_argos_q0601_0105.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0601_0106.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("argoss_favor");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11) {
            switch (npcId) {
                case 21306: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 850) {
                        if (st.getQuestItemsCount(q_proof_of_avenger) + 1L >= 100L) {
                            if (st.getQuestItemsCount(q_proof_of_avenger) < 100L) {
                                st.setCond(2);
                                st.set("argoss_favor", String.valueOf(12), true);
                                st.giveItems(q_proof_of_avenger, 100L - st.getQuestItemsCount(q_proof_of_avenger));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(q_proof_of_avenger, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21308: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 790) {
                        if (st.getQuestItemsCount(q_proof_of_avenger) + 1L >= 100L) {
                            if (st.getQuestItemsCount(q_proof_of_avenger) < 100L) {
                                st.setCond(2);
                                st.set("argoss_favor", String.valueOf(12), true);
                                st.giveItems(q_proof_of_avenger, 100L - st.getQuestItemsCount(q_proof_of_avenger));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(q_proof_of_avenger, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21309: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 820) {
                        if (st.getQuestItemsCount(q_proof_of_avenger) + 1L >= 100L) {
                            if (st.getQuestItemsCount(q_proof_of_avenger) < 100L) {
                                st.setCond(2);
                                st.set("argoss_favor", String.valueOf(12), true);
                                st.giveItems(q_proof_of_avenger, 100L - st.getQuestItemsCount(q_proof_of_avenger));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(q_proof_of_avenger, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21310: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 680) {
                        if (st.getQuestItemsCount(q_proof_of_avenger) + 1L >= 100L) {
                            if (st.getQuestItemsCount(q_proof_of_avenger) < 100L) {
                                st.setCond(2);
                                st.set("argoss_favor", String.valueOf(12), true);
                                st.giveItems(q_proof_of_avenger, 100L - st.getQuestItemsCount(q_proof_of_avenger));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(q_proof_of_avenger, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                    break;
                }
                case 21311: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 630) {
                        if (st.getQuestItemsCount(q_proof_of_avenger) + 1L >= 100L) {
                            if (st.getQuestItemsCount(q_proof_of_avenger) < 100L) {
                                st.setCond(2);
                                st.set("argoss_favor", String.valueOf(12), true);
                                st.giveItems(q_proof_of_avenger, 100L - st.getQuestItemsCount(q_proof_of_avenger));
                                st.playSound("ItemSound.quest_middle");
                            }
                        } else {
                            st.giveItems(q_proof_of_avenger, 1L);
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
