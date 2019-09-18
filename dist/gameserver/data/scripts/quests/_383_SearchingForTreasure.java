package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _383_SearchingForTreasure extends Quest {
    private static final int trader_espen = 30890;
    private static final int pirates_t_chest = 31148;
    private static final int pirates_treasure_map = 5915;
    private static final int key_of_thief = 1661;
    private static final int elven_mithril_gloves = 2450;
    private static final int sages_worn_gloves = 2451;
    private static final int scrl_of_ench_am_d = 956;
    private static final int scrl_of_ench_am_c = 952;
    private static final int dye_s1c3_c = 4481;
    private static final int dye_s1d3_c = 4482;
    private static final int dye_c1s3_c = 4483;
    private static final int dye_c1c3_c = 4484;
    private static final int dye_d1s3_c = 4485;
    private static final int dye_d1c3_c = 4486;
    private static final int dye_i1m3_c = 4487;
    private static final int dye_i1w3_c = 4488;
    private static final int dye_m1i3_c = 4489;
    private static final int dye_m1w3_c = 4490;
    private static final int dye_w1i3_c = 4491;
    private static final int dye_w1m3_c = 4492;
    private static final int emerald = 1337;
    private static final int blue_onyx = 1338;
    private static final int onyx = 1339;
    private static final int q_loot_4 = 3447;
    private static final int q_loot_7 = 3450;
    private static final int q_loot_10 = 3453;
    private static final int q_loot_13 = 3456;
    private static final int q_musicnote_love = 4408;
    private static final int q_musicnote_battle = 4409;
    private static final int q_musicnote_celebration = 4418;
    private static final int q_musicnote_comedy = 4419;

    public _383_SearchingForTreasure() {
        super(false);
        addStartNpc(30890);
        addTalkId(31148);
        addQuestItem(5915);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("treasure_hunt");
        final int npcId = npc.getNpcId();
        if (npcId == 30890) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.set("treasure_hunt", String.valueOf(1), true);
                st.takeItems(5915, 1L);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                htmltext = "trader_espen_q0383_08.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "trader_espen_q0383_04.htm";
            } else if ("reply_2".equalsIgnoreCase(event) && st.getQuestItemsCount(5915) > 0L) {
                st.giveItems(57, 1000L);
                st.unset("treasure_hunt");
                st.takeItems(5915, 1L);
                htmltext = "trader_espen_q0383_05.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(5915) > 0L) {
                    htmltext = "trader_espen_q0383_06.htm";
                } else {
                    htmltext = "trader_espen_q0383_07.htm";
                }
            } else if ("reply_4".equalsIgnoreCase(event)) {
                htmltext = "trader_espen_q0383_09.htm";
            } else if ("reply_5".equalsIgnoreCase(event)) {
                htmltext = "trader_espen_q0383_10.htm";
            } else if ("reply_6".equalsIgnoreCase(event)) {
                htmltext = "trader_espen_q0383_11.htm";
            } else if ("reply_7".equalsIgnoreCase(event) && GetMemoState == 1) {
                st.setCond(2);
                st.set("treasure_hunt", String.valueOf(2), true);
                st.playSound("ItemSound.quest_middle");
                htmltext = "trader_espen_q0383_12.htm";
            }
        } else if (npcId == 31148 && "reply_1".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(1661) == 0L) {
                htmltext = "pirates_t_chest_q0383_02.htm";
            } else if (GetMemoState == 2 && st.getQuestItemsCount(1661) >= 1L) {
                st.takeItems(1661, 1L);
                st.unset("treasure_hunt");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "pirates_t_chest_q0383_03.htm";
                int i1 = 0;
                final int i2 = Rnd.get(100);
                if (i2 < 5) {
                    st.giveItems(2450, 1L);
                } else if (i2 < 6) {
                    st.giveItems(2451, 1L);
                } else if (i2 < 18) {
                    st.giveItems(956, 1L);
                } else if (i2 < 28) {
                    st.giveItems(952, 1L);
                } else {
                    i1 += 500;
                }
                final int i3 = Rnd.get(1000);
                if (i3 < 25) {
                    st.giveItems(4481, 1L);
                } else if (i3 < 50) {
                    st.giveItems(4482, 1L);
                } else if (i3 < 75) {
                    st.giveItems(4483, 1L);
                } else if (i3 < 100) {
                    st.giveItems(4484, 1L);
                } else if (i3 < 125) {
                    st.giveItems(4485, 1L);
                } else if (i3 < 150) {
                    st.giveItems(4486, 1L);
                } else if (i3 < 175) {
                    st.giveItems(4487, 1L);
                } else if (i3 < 200) {
                    st.giveItems(4488, 1L);
                } else if (i3 < 225) {
                    st.giveItems(4489, 1L);
                } else if (i3 < 250) {
                    st.giveItems(4490, 1L);
                } else if (i3 < 275) {
                    st.giveItems(4491, 1L);
                } else if (i3 < 300) {
                    st.giveItems(4492, 1L);
                } else {
                    i1 += 300;
                }
                final int i4 = Rnd.get(100);
                if (i4 < 4) {
                    st.giveItems(1337, 1L);
                } else if (i4 < 8) {
                    st.giveItems(1338, 2L);
                } else if (i4 < 12) {
                    st.giveItems(1339, 2L);
                } else if (i4 < 16) {
                    st.giveItems(3447, 2L);
                } else if (i4 < 20) {
                    st.giveItems(3450, 1L);
                } else if (i4 < 25) {
                    st.giveItems(3453, 1L);
                } else if (i4 < 27) {
                    st.giveItems(3456, 1L);
                } else {
                    i1 += 500;
                }
                final int i5 = Rnd.get(100);
                if (i5 < 20) {
                    st.giveItems(4408, 1L);
                } else if (i5 < 40) {
                    st.giveItems(4409, 1L);
                } else if (i5 < 60) {
                    st.giveItems(4418, 1L);
                } else if (i5 < 80) {
                    st.giveItems(4419, 1L);
                } else {
                    i1 += 500;
                }
                st.giveItems(57, (long) i1);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("treasure_hunt");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30890) {
                    break;
                }
                if (st.getPlayer().getLevel() < 42) {
                    htmltext = "trader_espen_q0383_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 42 && st.getQuestItemsCount(5915) == 0L) {
                    htmltext = "trader_espen_q0383_02.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 42 && st.getQuestItemsCount(5915) > 0L) {
                    htmltext = "trader_espen_q0383_03.htm";
                    break;
                }
                break;
            }
            case 2: {
                if (npcId == 30890) {
                    if (GetMemoState == 1) {
                        htmltext = "trader_espen_q0383_13.htm";
                        break;
                    }
                    if (GetMemoState == 2) {
                        htmltext = "trader_espen_q0383_14.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId == 31148 && GetMemoState == 2) {
                        htmltext = "pirates_t_chest_q0383_01.htm";
                        break;
                    }
                    break;
                }
            }
        }
        return htmltext;
    }
}
