package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _352_HelpRoodRaiseANewPet extends Quest {
    private static final int pet_manager_rood = 31067;
    private static final int lienrik = 20786;
    private static final int lienrik_lad = 20787;
    private static final int lienlik_egg1 = 5860;
    private static final int lienlik_egg2 = 5861;

    public _352_HelpRoodRaiseANewPet() {
        super(false);
        addStartNpc(pet_manager_rood);
        addKillId(lienrik, lienrik_lad);
        addQuestItem(lienlik_egg1, lienlik_egg2);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == pet_manager_rood) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("how_about_new_pet", String.valueOf(1), true);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "pet_manager_rood_q0352_05.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "pet_manager_rood_q0352_09.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "pet_manager_rood_q0352_10.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                st.unset("how_about_new_pet");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "pet_manager_rood_q0352_11.htm";
            } else if ("reply_4".equalsIgnoreCase(event)) {
                htmltext = "pet_manager_rood_q0352_04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("how_about_new_pet");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != pet_manager_rood) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 39) {
                    htmltext = "pet_manager_rood_q0352_02.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "pet_manager_rood_q0352_01.htm";
                break;
            }
            case 2: {
                if (npcId != pet_manager_rood || GetMemoState != 1) {
                    break;
                }
                if (st.getQuestItemsCount(lienlik_egg1) < 1L && st.getQuestItemsCount(lienlik_egg2) < 1L) {
                    htmltext = "pet_manager_rood_q0352_06.htm";
                    break;
                }
                if (st.getQuestItemsCount(lienlik_egg1) >= 1L && st.getQuestItemsCount(lienlik_egg2) < 1L) {
                    if (st.getQuestItemsCount(lienlik_egg1) >= 10L) {
                        st.giveItems(57, st.getQuestItemsCount(lienlik_egg1) * 34L + 4000L);
                    } else {
                        st.giveItems(57, st.getQuestItemsCount(lienlik_egg1) * 34L + 2000L);
                    }
                    st.takeItems(lienlik_egg1, -1L);
                    htmltext = "pet_manager_rood_q0352_07.htm";
                    break;
                }
                if (st.getQuestItemsCount(lienlik_egg2) >= 1L) {
                    st.giveItems(57, 4000L + (st.getQuestItemsCount(lienlik_egg1) * 34L + st.getQuestItemsCount(lienlik_egg2) * 1025L));
                    st.takeItems(lienlik_egg2, -1L);
                    st.takeItems(lienlik_egg1, -1L);
                    htmltext = "pet_manager_rood_q0352_08.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("how_about_new_pet");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 1) {
            if (npcId == lienrik) {
                final int i0 = Rnd.get(100);
                if (i0 < 46) {
                    st.giveItems(lienlik_egg1, 1L);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i0 < 48) {
                    st.giveItems(lienlik_egg2, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            } else if (npcId == lienrik_lad) {
                final int i0 = Rnd.get(100);
                if (i0 < 69) {
                    st.giveItems(lienlik_egg1, 1L);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i0 < 71) {
                    st.giveItems(lienlik_egg2, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
