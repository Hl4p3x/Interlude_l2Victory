package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _354_ConquestofAlligatorIsland extends Quest {
    private static final int warehouse_keeper_kluck = 30895;
    private static final int crokian_lad = 20804;
    private static final int dailaon_lad = 20805;
    private static final int crokian_lad_warrior = 20806;
    private static final int farhite_lad = 20807;
    private static final int nos_lad = 20808;
    private static final int tribe_of_swamp = 20991;
    private static final int croc_tooth = 5863;
    private static final int mysterious_map_piece = 5864;
    private static final int pirates_treasure_map = 5915;

    public _354_ConquestofAlligatorIsland() {
        super(false);
        addStartNpc(30895);
        addKillId(20804, 20805, 20806, 20807, 20808, 20991);
        addQuestItem(5863, 5864);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 30895) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "warehouse_keeper_kluck_q0354_03.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(5863) >= 100L) {
                    st.giveItems(57, st.getQuestItemsCount(5863) * 220L + 10700L);
                    st.takeItems(5863, -1L);
                    htmltext = "warehouse_keeper_kluck_q0354_06b.htm";
                } else if (st.getQuestItemsCount(5863) > 0L && st.getQuestItemsCount(5863) < 100L) {
                    st.giveItems(57, st.getQuestItemsCount(5863) * 220L + 3100L);
                    st.takeItems(5863, st.getQuestItemsCount(5863));
                    htmltext = "warehouse_keeper_kluck_q0354_06a.htm";
                } else if (st.getQuestItemsCount(5863) == 0L) {
                    htmltext = "warehouse_keeper_kluck_q0354_06.htm";
                }
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "warehouse_keeper_kluck_q0354_07.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "warehouse_keeper_kluck_q0354_08.htm";
            } else if ("reply_4".equalsIgnoreCase(event) && st.getQuestItemsCount(5864) > 0L && st.getQuestItemsCount(5864) < 10L) {
                htmltext = "warehouse_keeper_kluck_q0354_09.htm";
            } else if ("reply_4".equalsIgnoreCase(event) && st.getQuestItemsCount(5864) >= 10L) {
                st.giveItems(5915, 1L);
                st.takeItems(5864, 10L);
                htmltext = "warehouse_keeper_kluck_q0354_10.htm";
            }
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
                if (npcId != 30895) {
                    break;
                }
                if (st.getPlayer().getLevel() < 38) {
                    htmltext = "warehouse_keeper_kluck_q0354_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                htmltext = "warehouse_keeper_kluck_q0354_02.htm";
                break;
            }
            case 2: {
                if (npcId != 30895) {
                    break;
                }
                if (st.getQuestItemsCount(5864) == 0L) {
                    htmltext = "warehouse_keeper_kluck_q0354_04.htm";
                    break;
                }
                if (st.getQuestItemsCount(5864) >= 1L) {
                    htmltext = "warehouse_keeper_kluck_q0354_05.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 20804:
                if (Rnd.get(100) < 84) {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
            case 20805:
                if (Rnd.get(100) < 91) {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
            case 20806:
                if (Rnd.get(100) < 88) {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
            case 20807:
                if (Rnd.get(100) < 92) {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
            case 20808:
                if (Rnd.get(100) < 14) {
                    st.giveItems(5863, 2L);
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
            case 20991:
                if (Rnd.get(100) < 69) {
                    st.giveItems(5863, 2L);
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    st.giveItems(5863, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
                if (Rnd.get(10) == 5) {
                    st.giveItems(5864, 1L);
                }
                break;
        }
        return null;
    }
}
