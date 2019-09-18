package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _612_WarwithKetraOrcs extends Quest {
    private static final int elder_ashas_barka_durai = 31377;
    private static final int ketra_orc_footman = 21324;
    private static final int ketra_orc_trooper = 21327;
    private static final int ketra_orc_scout = 21328;
    private static final int ketra_orc_shaman = 21329;
    private static final int ketra_orc_warrior = 21331;
    private static final int ketra_orc_captain = 21332;
    private static final int ketra_orc_medium = 21334;
    private static final int ketra_orc_centurion = 21336;
    private static final int ketra_orc_seer = 21338;
    private static final int ketra_orc_officer = 21339;
    private static final int ketra_orc_praefect = 21340;
    private static final int ketra_orc_overseer = 21342;
    private static final int ketra_orc_legatus = 21343;
    private static final int ketra_high_shaman = 21345;
    private static final int ketra_soothsayer = 21347;
    private static final int q_ketra_molar = 7234;
    private static final int q_nephentes_seed = 7187;

    public _612_WarwithKetraOrcs() {
        super(true);
        addStartNpc(31377);
        addKillId(21324, 21327, 21328, 21329, 21331, 21332, 21334, 21336, 21338, 21339, 21340, 21342, 21343, 21345, 21347);
        addQuestItem(7234);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("war_with_ketra_orcs", String.valueOf(11), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "elder_ashas_barka_durai_q0612_0104.htm";
        } else if ("reply_1".equalsIgnoreCase(event)) {
            htmltext = "elder_ashas_barka_durai_q0612_0201.htm";
        } else if ("reply_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7234) >= 100L) {
                st.takeItems(7234, 100L);
                st.giveItems(7187, 20L);
                htmltext = "elder_ashas_barka_durai_q0612_0202.htm";
            } else {
                htmltext = "elder_ashas_barka_durai_q0612_0203.htm";
            }
        } else if ("reply_4".equalsIgnoreCase(event)) {
            st.takeItems(7234, -1L);
            st.unset("war_with_ketra_orcs");
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            htmltext = "elder_ashas_barka_durai_q0612_0204.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("war_with_ketra_orcs");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31377) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 74) {
                    htmltext = "elder_ashas_barka_durai_q0612_0101.htm";
                    break;
                }
                htmltext = "elder_ashas_barka_durai_q0612_0103.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 31377 || GetMemoState != 11) {
                    break;
                }
                if (st.getQuestItemsCount(7234) == 0L) {
                    htmltext = "elder_ashas_barka_durai_q0612_0106.htm";
                    break;
                }
                htmltext = "elder_ashas_barka_durai_q0612_0105.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("war_with_ketra_orcs");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11) {
            switch (npcId) {
                case 21324: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 500) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21327: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 510) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21328: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 522) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21329: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 519) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21331:
                case 21332: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 529) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21334: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 539) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21336: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 548) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21338: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 558) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21339:
                case 21340: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 568) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21342: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 578) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21343: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 664) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21345: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 713) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
                case 21347: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 738) {
                        st.giveItems(7234, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                }
            }
        }
        return null;
    }
}
