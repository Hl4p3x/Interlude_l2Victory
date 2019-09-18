package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _371_ShriekOfGhosts extends Quest {
    private static final int seer_reva = 30867;
    private static final int patrin = 30929;
    private static final int hallates_warrior = 20818;
    private static final int hallates_knight = 20820;
    private static final int hallates_commander = 20824;
    private static final int ancient_porcelain = 6002;
    private static final int ancient_porcelain_s = 6003;
    private static final int ancient_porcelain_a = 6004;
    private static final int ancient_porcelain_b = 6005;
    private static final int ancient_porcelain_c = 6006;
    private static final int ancient_funeral_urn = 5903;

    public _371_ShriekOfGhosts() {
        super(true);
        addStartNpc(seer_reva);
        addTalkId(patrin);
        addKillId(hallates_warrior, hallates_knight, hallates_commander);
        addQuestItem(ancient_funeral_urn);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == seer_reva) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("spirits_cry_secrets", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "seer_reva_q0371_03.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(ancient_funeral_urn) < 1L) {
                    htmltext = "seer_reva_q0371_06.htm";
                } else if (st.getQuestItemsCount(ancient_funeral_urn) >= 1L && st.getQuestItemsCount(ancient_funeral_urn) < 100L) {
                    st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000L + 15000L);
                    st.takeItems(ancient_funeral_urn, -1L);
                    htmltext = "seer_reva_q0371_07.htm";
                } else if (st.getQuestItemsCount(ancient_funeral_urn) >= 100L) {
                    st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000L + 37700L);
                    st.takeItems(ancient_funeral_urn, -1L);
                    htmltext = "seer_reva_q0371_08.htm";
                }
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "seer_reva_q0371_09.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(ancient_funeral_urn) > 0L) {
                    st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000L);
                }
                st.takeItems(ancient_funeral_urn, -1L);
                st.unset("spirits_cry_secrets");
                st.exitCurrentQuest(true);
                htmltext = "seer_reva_q0371_10.htm";
            }
        } else if (npcId == patrin && "reply_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(ancient_porcelain) < 1L) {
                htmltext = "patrin_q0371_02.htm";
            } else if (st.getQuestItemsCount(ancient_porcelain) >= 1L) {
                final int i0 = Rnd.get(100);
                if (i0 < 2) {
                    st.giveItems(ancient_porcelain_s, 1L);
                    st.takeItems(ancient_porcelain, 1L);
                    htmltext = "patrin_q0371_03.htm";
                } else if (i0 < 32) {
                    st.giveItems(ancient_porcelain_a, 1L);
                    st.takeItems(ancient_porcelain, 1L);
                    htmltext = "patrin_q0371_04.htm";
                } else if (i0 < 62) {
                    st.giveItems(ancient_porcelain_b, 1L);
                    st.takeItems(ancient_porcelain, 1L);
                    htmltext = "patrin_q0371_05.htm";
                } else if (i0 < 77) {
                    st.giveItems(ancient_porcelain_c, 1L);
                    st.takeItems(ancient_porcelain, 1L);
                    htmltext = "patrin_q0371_06.htm";
                } else {
                    st.giveItems(ancient_porcelain, 1L);
                    htmltext = "patrin_q0371_07.htm";
                }
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("spirits_cry_secrets");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != seer_reva) {
                    break;
                }
                if (st.getPlayer().getLevel() < 59) {
                    htmltext = "seer_reva_q0371_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 59) {
                    htmltext = "seer_reva_q0371_02.htm";
                    break;
                }
                break;
            }
            case 2: {
                if (npcId == seer_reva) {
                    if (GetMemoState == 1 && st.getQuestItemsCount(ancient_porcelain) < 1L) {
                        htmltext = "seer_reva_q0371_04.htm";
                        break;
                    }
                    if (GetMemoState == 1 && st.getQuestItemsCount(ancient_porcelain) >= 1L) {
                        htmltext = "seer_reva_q0371_05.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId == patrin && GetMemoState == 1) {
                        htmltext = "patrin_q0371_01.htm";
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
        final int GetMemoState = st.getInt("spirits_cry_secrets");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 1) {
            switch (npcId) {
                case hallates_warrior: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 350) {
                        st.giveItems(ancient_funeral_urn, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i4 < 400) {
                        st.giveItems(ancient_porcelain, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case hallates_knight: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 583) {
                        st.giveItems(ancient_funeral_urn, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i4 < 673) {
                        st.giveItems(ancient_porcelain, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case hallates_commander: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 458) {
                        st.giveItems(ancient_funeral_urn, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    } else if (i4 < 538) {
                        st.giveItems(ancient_porcelain, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
            }
        }
        return null;
    }
}
