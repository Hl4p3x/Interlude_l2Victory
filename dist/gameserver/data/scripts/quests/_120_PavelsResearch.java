package quests;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;

public class _120_PavelsResearch extends Quest {
    private static final int collecter_yumi = 32041;
    private static final int weather_controller1 = 32042;
    private static final int weather_controller2 = 32043;
    private static final int weather_controller3 = 32044;
    private static final int drchaos_box = 32045;
    private static final int pavel_atlanta = 32046;
    private static final int chaos_secretary_wendy = 32047;
    private static final int sealed_seal_earing = 854;
    private static final int q_drchaos_diary_off = 8058;
    private static final int q_drchaos_diary_on = 8059;
    private static final int q_drchaos_diary_key = 8060;
    private static final int q_flower_of_pavel = 8290;
    private static final int q_whisper_atlanta = 8291;
    private static final int q_broch_of_wendy = 8292;

    public _120_PavelsResearch() {
        super(false);
        addStartNpc(32046);
        addTalkId(32046, 32041, 32047, 32045, 32042, 32043, 32044);
        addQuestItem(8058, 8059, 8060, 8290, 8291, 8292);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final Player player = st.getPlayer();
        final int GetMemoState = st.getInt("last_research_of_pavel");
        final int GetMemoStateEx = st.getInt("last_research_of_pavel_ex");
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 32046:
                if ("quest_accept".equalsIgnoreCase(event)) {
                    if (st.getPlayer().getLevel() >= 50) {
                        st.setCond(1);
                        st.set("last_research_of_pavel", String.valueOf(1), true);
                        st.setState(2);
                        st.playSound("ItemSound.quest_accept");
                        htmltext = "pavel_atlanta_q0120_09.htm";
                    } else {
                        htmltext = "pavel_atlanta_q0120_08.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "pavel_atlanta_q0120_05.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 1) {
                        st.setCond(2);
                        st.set("last_research_of_pavel", String.valueOf(2), true);
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "pavel_atlanta_q0120_12.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    if (GetMemoState == 3) {
                        st.setCond(6);
                        st.set("last_research_of_pavel", String.valueOf(4), true);
                        st.giveItems(8290, 1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "pavel_atlanta_q0120_16.htm";
                    }
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 7) {
                        st.setCond(10);
                        st.set("last_research_of_pavel", String.valueOf(8), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "pavel_atlanta_q0120_25.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 11) {
                        st.setCond(13);
                        st.set("last_research_of_pavel", String.valueOf(12), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "pavel_atlanta_q0120_32.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 19) {
                        st.setCond(20);
                        st.set("last_research_of_pavel", String.valueOf(20), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "pavel_atlanta_q0120_38.htm";
                    }
                } else if ("reply_8".equalsIgnoreCase(event) && GetMemoState == 22) {
                    st.setCond(23);
                    st.set("last_research_of_pavel", String.valueOf(23), true);
                    st.giveItems(8291, 1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "pavel_atlanta_q0120_41.htm";
                }
                break;
            case 32041:
                if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "collecter_yumi_q0120_02.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 2 && GetMemoStateEx == 0) {
                        st.setCond(3);
                        st.set("last_research_of_pavel_ex", String.valueOf(1), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "collecter_yumi_q0120_03.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    if (GetMemoState == 2 && GetMemoStateEx == 0) {
                        st.setCond(4);
                        st.set("last_research_of_pavel_ex", String.valueOf(2), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "collecter_yumi_q0120_05.htm";
                    }
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 5) {
                        st.setCond(8);
                        st.set("last_research_of_pavel", String.valueOf(6), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "collecter_yumi_q0120_13.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 14) {
                        st.setCond(16);
                        st.set("last_research_of_pavel", String.valueOf(15), true);
                        st.giveItems(8060, 1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "collecter_yumi_q0120_17.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 15 && st.getQuestItemsCount(8059) >= 1L) {
                        st.setCond(17);
                        st.set("last_research_of_pavel", String.valueOf(16), true);
                        st.takeItems(8060, -1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "collecter_yumi_q0120_23.htm";
                    }
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    htmltext = "collecter_yumi_q0120_30.htm";
                } else if ("reply_9".equalsIgnoreCase(event)) {
                    htmltext = "collecter_yumi_q0120_31.htm";
                } else if ("reply_10".equalsIgnoreCase(event) && GetMemoState == 26) {
                    st.giveItems(854, 1L);
                    st.giveItems(57, 783720L);
                    st.addExpAndSp(3447315L, 272615L);
                    st.takeItems(8292, -1L);
                    st.unset("last_research_of_pavel");
                    st.unset("last_research_of_pavel_ex");
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                    htmltext = "collecter_yumi_q0120_33.htm";
                }
                break;
            case 32047:
                if ("reply_1".equalsIgnoreCase(event)) {
                    htmltext = "chaos_secretary_wendy_q0120_02.htm";
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 2) {
                        st.setCond(5);
                        st.set("last_research_of_pavel", String.valueOf(3), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "chaos_secretary_wendy_q0120_06.htm";
                    }
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    htmltext = "chaos_secretary_wendy_q0120_09.htm";
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 4) {
                        st.setCond(7);
                        st.set("last_research_of_pavel", String.valueOf(5), true);
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(8290, -1L);
                        htmltext = "chaos_secretary_wendy_q0120_10.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 6) {
                        st.setCond(9);
                        st.set("last_research_of_pavel", String.valueOf(7), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "chaos_secretary_wendy_q0120_14.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 12) {
                        st.setCond(14);
                        st.set("last_research_of_pavel", String.valueOf(13), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "chaos_secretary_wendy_q0120_18.htm";
                    }
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    if (GetMemoState == 23) {
                        st.setCond(24);
                        st.set("last_research_of_pavel", String.valueOf(24), true);
                        st.takeItems(8291, -1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "chaos_secretary_wendy_q0120_26.htm";
                    }
                } else if ("reply_9".equalsIgnoreCase(event)) {
                    if (GetMemoState == 24) {
                        st.set("last_research_of_pavel", String.valueOf(25), true);
                        htmltext = "chaos_secretary_wendy_q0120_29.htm";
                    }
                } else if ("reply_10".equalsIgnoreCase(event) && GetMemoState == 25) {
                    st.setCond(25);
                    st.set("last_research_of_pavel", String.valueOf(26), true);
                    st.giveItems(8292, 1L);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "chaos_secretary_wendy_q0120_32.htm";
                }
                break;
            case 32045:
                if ("reply_1".equalsIgnoreCase(event) && GetMemoState == 13) {
                    st.setCond(15);
                    st.set("last_research_of_pavel", String.valueOf(14), true);
                    st.giveItems(8058, 1L);
                    if (player != null) {
                        npc.broadcastPacket(new MagicSkillUse(npc, player, 5073, 5, 1500, 0L));
                    }
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "drchaos_box_q0120_02.htm";
                }
                break;
            case 32042:
                if ("reply_90".equalsIgnoreCase(event)) {
                    if (GetMemoState == 8) {
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        htmltext = "weather_controller1_q0120_02.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event)) {
                    if (GetMemoState == 8) {
                        st.set("last_research_of_pavel_ex", String.valueOf(1), true);
                        htmltext = "weather_controller1_q0120_03.htm";
                    }
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 8) {
                        int i0 = GetMemoStateEx;
                        i0 %= 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(10 + i0), true);
                        htmltext = "weather_controller1_q0120_04.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    if (GetMemoState == 8 && GetMemoStateEx != 11) {
                        htmltext = "weather_controller1_q0120_05.htm";
                    }
                    if (GetMemoState == 8 && GetMemoStateEx == 11) {
                        st.setCond(11);
                        st.set("last_research_of_pavel", String.valueOf(9), true);
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "weather_controller1_q0120_06.htm";
                    }
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller1_q0120_07.htm";
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 9) {
                        st.set("last_research_of_pavel", String.valueOf(10), true);
                        st.playSound("AmbSound.dt_percussion_01");
                        htmltext = "weather_controller1_q0120_08.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 10 && GetMemoStateEx != 10101) {
                        htmltext = "weather_controller1_q0120_09.htm";
                    }
                    if (GetMemoState == 10 && GetMemoStateEx == 10101) {
                        htmltext = "weather_controller1_q0120_13.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 10) {
                        int i0 = GetMemoStateEx;
                        i0 /= 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(i0 * 10 + 1), true);
                        htmltext = "weather_controller1_q0120_10.htm";
                    }
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    if (GetMemoState == 10) {
                        final int i2 = GetMemoStateEx / 1000;
                        final int i3 = GetMemoStateEx % 100;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 1000 + 100 + i3), true);
                        htmltext = "weather_controller1_q0120_11.htm";
                    }
                } else if ("reply_9".equalsIgnoreCase(event)) {
                    if (GetMemoState == 10) {
                        final int i2 = GetMemoStateEx % 10000;
                        st.set("last_research_of_pavel_ex", String.valueOf(10000 + i2), true);
                        htmltext = "weather_controller1_q0120_12.htm";
                    }
                } else if ("reply_10".equalsIgnoreCase(event) && GetMemoState == 10 && GetMemoStateEx == 10101) {
                    st.setCond(12);
                    st.set("last_research_of_pavel", String.valueOf(11), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "weather_controller1_q0120_13a.htm";
                }
                break;
            case 32043:
                if ("reply_90".equalsIgnoreCase(event)) {
                    if (GetMemoState == 16) {
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        htmltext = "weather_controller2_q0120_02.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event)) {
                    if (GetMemoState == 16) {
                        st.set("last_research_of_pavel_ex", String.valueOf(1), true);
                        htmltext = "weather_controller2_q0120_03.htm";
                    }
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 16) {
                        int i0 = GetMemoStateEx;
                        i0 %= 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(10 + i0), true);
                        htmltext = "weather_controller2_q0120_04.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    if (GetMemoState == 16 && GetMemoStateEx != 11) {
                        htmltext = "weather_controller2_q0120_05.htm";
                    }
                    if (GetMemoState == 16 && GetMemoStateEx == 11) {
                        st.setCond(18);
                        st.set("last_research_of_pavel", String.valueOf(17), true);
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "weather_controller2_q0120_06.htm";
                    }
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller2_q0120_07.htm";
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 17) {
                        st.set("last_research_of_pavel", String.valueOf(18), true);
                        htmltext = "weather_controller2_q0120_09.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18 && GetMemoStateEx != 1111) {
                        htmltext = "weather_controller2_q0120_11.htm";
                    }
                    if (GetMemoState == 18 && GetMemoStateEx == 1111) {
                        htmltext = "weather_controller2_q0120_11b.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18) {
                        int i0 = GetMemoStateEx;
                        i0 /= 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(i0 * 10 + 1), true);
                        htmltext = "weather_controller2_q0120_12.htm";
                    }
                } else if ("reply_10".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller2_q0120_13.htm";
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18 && GetMemoStateEx < 1000) {
                        htmltext = "weather_controller2_q0120_14.htm";
                    }
                    if (GetMemoState == 18 && GetMemoStateEx >= 1000) {
                        htmltext = "weather_controller2_q0120_17.htm";
                    }
                } else if ("reply_71".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller2_q0120_15.htm";
                } else if ("reply_11".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18) {
                        final int i2 = GetMemoStateEx / 10000;
                        final int i3 = GetMemoStateEx % 1000;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 10000 + 1000 + i3), true);
                        st.playSound("AmbSound.ed_drone_02");
                        htmltext = "weather_controller2_q0120_16.htm";
                    }
                } else if ("reply_9".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller2_q0120_19.htm";
                } else if ("reply_12".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18) {
                        final int i2 = GetMemoStateEx / 100;
                        final int i3 = GetMemoStateEx % 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 100 + 10 + i3), true);
                        htmltext = "weather_controller2_q0120_20.htm";
                    }
                } else if ("reply_14".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller2_q0120_23.htm";
                } else if ("reply_72".equalsIgnoreCase(event)) {
                    if (GetMemoState == 18) {
                        final int i2 = GetMemoStateEx / 1000;
                        final int i3 = GetMemoStateEx % 100;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 1000 + 100 + i3), true);
                        htmltext = "weather_controller2_q0120_25.htm";
                    }
                } else if ("reply_13".equalsIgnoreCase(event) && GetMemoState == 18 && GetMemoStateEx == 1111) {
                    st.setCond(19);
                    st.set("last_research_of_pavel", String.valueOf(19), true);
                    st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "weather_controller2_q0120_21.htm";
                }
                break;
            case 32044:
                if ("reply_90".equalsIgnoreCase(event)) {
                    if (GetMemoState == 20) {
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        htmltext = "weather_controller3_q0120_02.htm";
                    }
                } else if ("reply_1".equalsIgnoreCase(event)) {
                    if (GetMemoState == 20) {
                        st.set("last_research_of_pavel_ex", String.valueOf(1), true);
                        htmltext = "weather_controller3_q0120_03.htm";
                    }
                } else if ("reply_2".equalsIgnoreCase(event)) {
                    if (GetMemoState == 20) {
                        int i0 = GetMemoStateEx;
                        i0 %= 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(10 + i0), true);
                        htmltext = "weather_controller3_q0120_04.htm";
                    }
                } else if ("reply_3".equalsIgnoreCase(event)) {
                    if (GetMemoState == 20 && GetMemoStateEx != 11) {
                        htmltext = "weather_controller3_q0120_05.htm";
                    }
                    if (GetMemoState == 20 && GetMemoStateEx == 11) {
                        st.setCond(21);
                        st.set("last_research_of_pavel", String.valueOf(21), true);
                        st.set("last_research_of_pavel_ex", String.valueOf(0), true);
                        st.playSound("AmbSound.ac_percussion_02");
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "weather_controller3_q0120_06.htm";
                    }
                } else if ("reply_4".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller3_q0120_07.htm";
                } else if ("reply_100".equalsIgnoreCase(event)) {
                    if (GetMemoState == 21 && GetMemoStateEx % 100 != 11) {
                        htmltext = "weather_controller3_q0120_09.htm";
                    }
                    if (GetMemoState == 21 && GetMemoStateEx % 100 == 11) {
                        htmltext = "weather_controller3_q0120_09a.htm";
                    }
                } else if ("reply_5".equalsIgnoreCase(event)) {
                    if (GetMemoState == 21) {
                        final int i2 = GetMemoStateEx / 100;
                        final int i3 = GetMemoStateEx % 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 100 + 10 + i3), true);
                        htmltext = "weather_controller3_q0120_10.htm";
                    }
                } else if ("reply_6".equalsIgnoreCase(event)) {
                    if (GetMemoState == 21 && GetMemoStateEx / 100 != 1) {
                        final int i2 = GetMemoStateEx / 10;
                        st.set("last_research_of_pavel_ex", String.valueOf(i2 * 10 + 1), true);
                        htmltext = "weather_controller3_q0120_11.htm";
                    }
                    if (GetMemoState == 21 && GetMemoStateEx / 100 == 1) {
                        htmltext = "weather_controller3_q0120_11a.htm";
                    }
                } else if ("reply_7".equalsIgnoreCase(event)) {
                    if (GetMemoState == 21 && GetMemoStateEx / 100 != 1) {
                        htmltext = "weather_controller3_q0120_12.htm";
                    }
                    if (GetMemoState == 21 && GetMemoStateEx / 100 == 1) {
                        htmltext = "weather_controller3_q0120_12a.htm";
                    }
                } else if ("reply_8".equalsIgnoreCase(event)) {
                    if (GetMemoState == 21 && GetMemoStateEx / 100 != 1) {
                        int i0 = GetMemoStateEx;
                        i0 %= 100;
                        st.set("last_research_of_pavel_ex", String.valueOf(100 + i0), true);
                        htmltext = "weather_controller3_q0120_13.htm";
                    }
                } else if ("reply_9".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller3_q0120_14.htm";
                } else if ("reply_10".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller3_q0120_15.htm";
                } else if ("reply_11".equalsIgnoreCase(event)) {
                    htmltext = "weather_controller3_q0120_16.htm";
                } else if ("reply_12".equalsIgnoreCase(event) && GetMemoState == 21 && GetMemoStateEx / 100 == 1) {
                    st.setCond(22);
                    st.set("last_research_of_pavel", String.valueOf(22), true);
                    st.playSound("AmbSound.ed_drone_02");
                    if (player != null) {
                        npc.broadcastPacket(new MagicSkillUse(npc, player, 5073, 5, 1500, 0L));
                    }
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "weather_controller3_q0120_17.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final QuestState qs = st.getPlayer().getQuestState(_114_ResurrectionOfAnOldManager.class);
        final int GetMemoState = st.getInt("last_research_of_pavel");
        final int GetMemoStateEx = st.getInt("last_research_of_pavel_ex");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32046) {
                    break;
                }
                if (qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 50) {
                    htmltext = "pavel_atlanta_q0120_01.htm";
                    break;
                }
                htmltext = "pavel_atlanta_q0120_03.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 32046:
                        if (GetMemoState == 1) {
                            htmltext = "pavel_atlanta_q0120_10.htm";
                            break;
                        }
                        if (GetMemoState == 2) {
                            htmltext = "pavel_atlanta_q0120_13.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "pavel_atlanta_q0120_14.htm";
                            break;
                        }
                        if (GetMemoState == 4) {
                            htmltext = "pavel_atlanta_q0120_16a.htm";
                            break;
                        }
                        if (GetMemoState == 7) {
                            htmltext = "pavel_atlanta_q0120_17.htm";
                            break;
                        }
                        if (GetMemoState == 8) {
                            htmltext = "pavel_atlanta_q0120_28.htm";
                            break;
                        }
                        if (GetMemoState == 11) {
                            htmltext = "pavel_atlanta_q0120_29.htm";
                            break;
                        }
                        if (GetMemoState == 12) {
                            htmltext = "pavel_atlanta_q0120_33.htm";
                            break;
                        }
                        if (GetMemoState == 19) {
                            htmltext = "pavel_atlanta_q0120_34.htm";
                            break;
                        }
                        if (GetMemoState == 20) {
                            htmltext = "pavel_atlanta_q0120_39.htm";
                            break;
                        }
                        if (GetMemoState == 22) {
                            htmltext = "pavel_atlanta_q0120_40.htm";
                            break;
                        }
                        if (GetMemoState == 23) {
                            htmltext = "pavel_atlanta_q0120_42.htm";
                            break;
                        }
                        break;
                    case 32041:
                        if (GetMemoState == 2 && GetMemoStateEx == 0) {
                            htmltext = "collecter_yumi_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 2 && GetMemoStateEx == 1) {
                            htmltext = "collecter_yumi_q0120_04.htm";
                            break;
                        }
                        if (GetMemoState == 2 && GetMemoStateEx == 2) {
                            htmltext = "collecter_yumi_q0120_06.htm";
                            break;
                        }
                        if (GetMemoState == 5 && GetMemoStateEx > 0) {
                            htmltext = "collecter_yumi_q0120_07.htm";
                            break;
                        }
                        if (GetMemoState == 5 && GetMemoStateEx == 0) {
                            htmltext = "collecter_yumi_q0120_08.htm";
                            break;
                        }
                        if (GetMemoState == 6) {
                            htmltext = "collecter_yumi_q0120_14.htm";
                            break;
                        }
                        if (GetMemoState == 14) {
                            htmltext = "collecter_yumi_q0120_15.htm";
                            break;
                        }
                        if (GetMemoState == 15 && st.getQuestItemsCount(8059) == 0L) {
                            htmltext = "collecter_yumi_q0120_18.htm";
                            break;
                        }
                        if (GetMemoState == 15 && st.getQuestItemsCount(8059) >= 1L) {
                            htmltext = "collecter_yumi_q0120_19.htm";
                            break;
                        }
                        if (GetMemoState == 16) {
                            htmltext = "collecter_yumi_q0120_26.htm";
                            break;
                        }
                        if (GetMemoState == 26) {
                            htmltext = "collecter_yumi_q0120_27.htm";
                            break;
                        }
                        break;
                    case 32047:
                        if (GetMemoState == 2) {
                            htmltext = "chaos_secretary_wendy_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "chaos_secretary_wendy_q0120_07.htm";
                            break;
                        }
                        if (GetMemoState == 4) {
                            htmltext = "chaos_secretary_wendy_q0120_08.htm";
                            break;
                        }
                        if (GetMemoState == 5) {
                            htmltext = "chaos_secretary_wendy_q0120_11.htm";
                            break;
                        }
                        if (GetMemoState == 6) {
                            htmltext = "chaos_secretary_wendy_q0120_11z.htm";
                            break;
                        }
                        if (GetMemoState == 7) {
                            htmltext = "chaos_secretary_wendy_q0120_15.htm";
                            break;
                        }
                        if (GetMemoState == 12) {
                            htmltext = "chaos_secretary_wendy_q0120_16.htm";
                            break;
                        }
                        if (GetMemoState == 13) {
                            htmltext = "chaos_secretary_wendy_q0120_19.htm";
                            break;
                        }
                        if (GetMemoState == 14) {
                            htmltext = "chaos_secretary_wendy_q0120_20.htm";
                            break;
                        }
                        if (GetMemoState == 23) {
                            htmltext = "chaos_secretary_wendy_q0120_21.htm";
                            break;
                        }
                        if (GetMemoState == 24) {
                            htmltext = "chaos_secretary_wendy_q0120_26.htm";
                            break;
                        }
                        if (GetMemoState == 25) {
                            htmltext = "chaos_secretary_wendy_q0120_29.htm";
                            break;
                        }
                        if (GetMemoState == 26) {
                            htmltext = "chaos_secretary_wendy_q0120_33.htm";
                            break;
                        }
                        break;
                    case 32045:
                        if (GetMemoState == 13) {
                            htmltext = "drchaos_box_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 14) {
                            htmltext = "drchaos_box_q0120_03.htm";
                            break;
                        }
                        break;
                    case 32042:
                        if (GetMemoState == 8) {
                            st.playSound("AmbSound.cd_crystal_loop");
                            htmltext = "weather_controller1_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 9) {
                            htmltext = "weather_controller1_q0120_06.htm";
                            break;
                        }
                        if (GetMemoState == 10 && GetMemoStateEx != 10101) {
                            htmltext = "weather_controller1_q0120_09.htm";
                            break;
                        }
                        if (GetMemoState == 10 && GetMemoStateEx == 10101) {
                            htmltext = "weather_controller1_q0120_13.htm";
                            break;
                        }
                        if (GetMemoState == 11) {
                            htmltext = "weather_controller1_q0120_13a.htm";
                            break;
                        }
                        break;
                    case 32043:
                        if (GetMemoState == 16) {
                            htmltext = "weather_controller2_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 17) {
                            htmltext = "weather_controller2_q0120_06.htm";
                            break;
                        }
                        if (GetMemoState == 18) {
                            htmltext = "weather_controller2_q0120_09.htm";
                            break;
                        }
                        if (GetMemoState == 19) {
                            htmltext = "weather_controller2_q0120_22.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 32044) {
                            break;
                        }
                        if (GetMemoState == 20) {
                            htmltext = "weather_controller3_q0120_01.htm";
                            break;
                        }
                        if (GetMemoState == 21) {
                            htmltext = "weather_controller3_q0120_08.htm";
                            break;
                        }
                        if (GetMemoState == 22) {
                            htmltext = "weather_controller3_q0120_19.htm";
                            break;
                        }
                        break;
                }
            }
        }
        return htmltext;
    }
}
