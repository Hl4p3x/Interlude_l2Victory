package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _358_IllegitimateChildOfAGoddess extends Quest {
    private static final int grandmaster_oltlin = 30862;
    private static final int trives = 20672;
    private static final int falibati = 20673;
    private static final int snake_scale = 5868;
    private static final int rp_sealed_phoenixs_necklace_i = 6329;
    private static final int rp_sealed_phoenixs_earing_i = 6331;
    private static final int rp_sealed_phoenixs_ring_i = 6333;
    private static final int rp_sealed_majestic_necklace_i = 6335;
    private static final int rp_sealed_majestic_earing_i = 6337;
    private static final int rp_sealed_majestic_ring_i = 6339;
    private static final int rp_sealed_dark_crystal_shield_i = 5364;
    private static final int rp_sealed_shield_of_nightmare_i = 5366;

    public _358_IllegitimateChildOfAGoddess() {
        super(false);
        addStartNpc(30862);
        addKillId(20672, 20673);
        addQuestItem(5868);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 30862) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("an_illegitimate_child_of_godness", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "grandmaster_oltlin_q0358_05.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "grandmaster_oltlin_q0358_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("an_illegitimate_child_of_godness");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30862) {
                    break;
                }
                if (st.getPlayer().getLevel() < 63) {
                    htmltext = "grandmaster_oltlin_q0358_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 63) {
                    htmltext = "grandmaster_oltlin_q0358_02.htm";
                    break;
                }
                break;
            }
            case 2: {
                if (npcId != 30862) {
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(5868) < 108L) {
                    htmltext = "grandmaster_oltlin_q0358_06.htm";
                    break;
                }
                if (GetMemoState == 1 && st.getQuestItemsCount(5868) >= 108L) {
                    final int i0 = Rnd.get(1000);
                    if (i0 < 125) {
                        st.giveItems(6331, 1L);
                    } else if (i0 < 250) {
                        st.giveItems(6337, 1L);
                    } else if (i0 < 375) {
                        st.giveItems(6329, 1L);
                    } else if (i0 < 500) {
                        st.giveItems(6335, 1L);
                    } else if (i0 < 625) {
                        st.giveItems(6333, 1L);
                    } else if (i0 < 750) {
                        st.giveItems(6339, 1L);
                    } else if (i0 < 875) {
                        st.giveItems(5366, 1L);
                    } else {
                        st.giveItems(5364, 1L);
                    }
                    st.takeItems(5868, -1L);
                    st.unset("an_illegitimate_child_of_godness");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    htmltext = "grandmaster_oltlin_q0358_07.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("an_illegitimate_child_of_godness");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 1) {
            if (npcId == 20672) {
                if (st.getQuestItemsCount(5868) < 108L && Rnd.get(100) < 71) {
                    st.giveItems(5868, 1L);
                    st.playSound("ItemSound.quest_itemget");
                    if (st.getQuestItemsCount(5868) >= 107L) {
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            } else if (npcId == 20673 && st.getQuestItemsCount(5868) < 108L && Rnd.get(100) < 74) {
                st.giveItems(5868, 1L);
                if (st.getQuestItemsCount(5868) >= 107L) {
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
