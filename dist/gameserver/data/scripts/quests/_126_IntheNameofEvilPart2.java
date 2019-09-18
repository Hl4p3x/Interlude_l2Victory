package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

public class _126_IntheNameofEvilPart2 extends Quest {
    private static final int mushika = 32114;
    private static final int asama = 32115;
    private static final int ulu_kaimu_stone = 32119;
    private static final int balu_kaimu_stone = 32120;
    private static final int jiuta_kaimu_stone = 32121;
    private static final int grave_of_brave_man = 32122;
    private static final int statue_of_shilen = 32109;
    private static final int q_ash_flour = 8783;
    private static final int q_muzzle_pattem = 8781;
    private static final int q_piece_of_gazk = 8782;
    private static final int scrl_of_ench_wp_a = 729;

    public _126_IntheNameofEvilPart2() {
        super(false);
        addStartNpc(32115);
        addTalkId(32114, 32119, 32120, 32121, 32122, 32109);
        addQuestItem(8783, 8781);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("name_of_cruel_god_two");
        final int GetMemoStateEx = st.getInt("name_of_cruel_god_two_ex");
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "asama_q0126_07.htm";
        } else if ("reply_4".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.set("name_of_cruel_god_two", String.valueOf(1), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "asama_q0126_10.htm";
        } else if ("reply_62".equalsIgnoreCase(event) && GetMemoState == 406) {
            st.setCond(21);
            st.set("name_of_cruel_god_two", String.valueOf(407), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "asama_q0126_16.htm";
        } else if ("reply_71".equalsIgnoreCase(event) && GetMemoState == 407) {
            st.set("name_of_cruel_god_two", String.valueOf(408), true);
            htmltext = "asama_q0126_26.htm";
        } else if ("reply_72".equalsIgnoreCase(event) && GetMemoState == 408) {
            st.setCond(22);
            st.set("name_of_cruel_god_two", String.valueOf(409), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "asama_q0126_28.htm";
        } else if ("reply_1".equalsIgnoreCase(event) && (GetMemoState == 409 || GetMemoState == 410)) {
            htmltext = "mushika_q0126_03.htm";
            if (GetMemoState == 409) {
                st.setCond(23);
                st.set("name_of_cruel_god_two", String.valueOf(410), true);
                st.playSound("ItemSound.quest_middle");
            }
        } else if ("reply_2".equalsIgnoreCase(event) && GetMemoState == 410) {
            htmltext = "mushika_q0126_08.htm";
        } else if ("reply_3".equalsIgnoreCase(event) && GetMemoState == 410) {
            st.giveItems(57, 298496L);
            st.giveItems(729, 1L);
            htmltext = "mushika_q0126_09.htm";
            st.unset("name_of_cruel_god_two");
            st.unset("name_of_cruel_god_two_ex");
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("reply_5".equalsIgnoreCase(event) && GetMemoState == 2) {
            st.setCond(3);
            st.set("name_of_cruel_god_two", String.valueOf(3), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "ulu_kaimu_stone_q0126_03.htm";
        } else if ("reply_6".equalsIgnoreCase(event) && GetMemoState == 3) {
            htmltext = "ulu_kaimu_stone_q0126_05.htm";
        } else if ("reply_7".equalsIgnoreCase(event)) {
            htmltext = "ulu_kaimu_stone_q0126_06.htm";
        } else if ("reply_8".equalsIgnoreCase(event) && GetMemoState == 3) {
            st.setCond(4);
            st.set("name_of_cruel_god_two", String.valueOf(4), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "ulu_kaimu_stone_q0126_07.htm";
        } else if ("reply_9".equalsIgnoreCase(event)) {
            htmltext = "ulu_kaimu_stone_q0126_09.htm";
            st.playSound("EtcSound.elcroki_song_1st");
        } else if ("reply_10".equalsIgnoreCase(event)) {
            htmltext = "ulu_kaimu_stone_q0126_10.htm";
        } else if ("reply_11".equalsIgnoreCase(event) && GetMemoState == 4) {
            st.setCond(5);
            st.set("name_of_cruel_god_two", String.valueOf(5), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "ulu_kaimu_stone_q0126_11.htm";
        } else if ("reply_12".equalsIgnoreCase(event)) {
            htmltext = "ulu_kaimu_stone_q0126_13.htm";
            st.playSound("EtcSound.elcroki_song_1st");
        } else if ("reply_12a".equalsIgnoreCase(event) && GetMemoState == 5) {
            htmltext = "balu_kaimu_stone_q0126_03.htm";
        } else if ("reply_13".equalsIgnoreCase(event) && GetMemoState == 5) {
            st.setCond(6);
            st.set("name_of_cruel_god_two", String.valueOf(7), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "balu_kaimu_stone_q0126_04.htm";
        } else if ("reply_400".equalsIgnoreCase(event) && GetMemoState == 7) {
            htmltext = "balu_kaimu_stone_q0126_06.htm";
        } else if ("reply_14".equalsIgnoreCase(event) && GetMemoState == 7) {
            st.setCond(7);
            st.set("name_of_cruel_god_two", String.valueOf(8), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "balu_kaimu_stone_q0126_07.htm";
        } else if ("reply_15".equalsIgnoreCase(event)) {
            htmltext = "balu_kaimu_stone_q0126_09.htm";
            st.playSound("EtcSound.elcroki_song_2nd");
        } else if ("reply_16".equalsIgnoreCase(event)) {
            htmltext = "balu_kaimu_stone_q0126_10.htm";
        } else if ("reply_17".equalsIgnoreCase(event) && GetMemoState == 8) {
            st.setCond(8);
            st.set("name_of_cruel_god_two", String.valueOf(9), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "balu_kaimu_stone_q0126_11.htm";
        } else if ("reply_18".equalsIgnoreCase(event)) {
            htmltext = "balu_kaimu_stone_q0126_13.htm";
            st.playSound("EtcSound.elcroki_song_2nd");
        } else if ("reply_18a".equalsIgnoreCase(event) && GetMemoState == 9) {
            st.setCond(9);
            st.set("name_of_cruel_god_two", String.valueOf(11), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "jiuta_kaimu_stone_q0126_03.htm";
        } else if ("reply_19".equalsIgnoreCase(event) && GetMemoState == 11) {
            htmltext = "jiuta_kaimu_stone_q0126_05.htm";
        } else if ("reply_20".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_06.htm";
        } else if ("reply_21".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_07.htm";
        } else if ("reply_22".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_08.htm";
        } else if ("reply_23".equalsIgnoreCase(event) && GetMemoState == 11) {
            st.setCond(10);
            st.set("name_of_cruel_god_two", String.valueOf(12), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "jiuta_kaimu_stone_q0126_09.htm";
        } else if ("reply_24".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_11.htm";
            st.playSound("EtcSound.elcroki_song_3rd");
        } else if ("reply_25".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_12.htm";
        } else if ("reply_26".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_13.htm";
        } else if ("reply_27".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_14.htm";
        } else if ("reply_28".equalsIgnoreCase(event) && GetMemoState == 12) {
            st.giveItems(8782, 1L);
            st.setCond(11);
            st.set("name_of_cruel_god_two", String.valueOf(13), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "jiuta_kaimu_stone_q0126_15.htm";
        } else if ("reply_29".equalsIgnoreCase(event)) {
            htmltext = "jiuta_kaimu_stone_q0126_17.htm";
            st.playSound("EtcSound.elcroki_song_3rd");
        } else if ("reply_30".equalsIgnoreCase(event) && GetMemoState == 13) {
            htmltext = "grave_of_brave_man_q0126_03.htm";
            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
        } else if ("reply_31".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_04.htm";
        } else if ("reply_32".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_05.htm";
        } else if ("reply_33".equalsIgnoreCase(event) && GetMemoState == 13) {
            st.takeItems(8782, -1L);
            st.set("name_of_cruel_god_two", String.valueOf(14), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(0), true);
            htmltext = "grave_of_brave_man_q0126_06.htm";
        } else if ("reply_300".equalsIgnoreCase(event) && GetMemoState == 14) {
            htmltext = "grave_of_brave_man_q0126_08.htm";
        } else if ("reply_34".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_09.htm";
        } else if ("reply_35".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_10.htm";
        } else if ("reply_36".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_11.htm";
        } else if ("reply_37".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_12.htm";
        } else if ("reply_38".equalsIgnoreCase(event)) {
            htmltext = "grave_of_brave_man_q0126_13.htm";
        } else if ("reply_39".equalsIgnoreCase(event) && GetMemoState == 14) {
            htmltext = "grave_of_brave_man_q0126_14.htm";
        } else if ("reply_40".equalsIgnoreCase(event) && GetMemoState == 14) {
            st.setCond(13);
            st.set("name_of_cruel_god_two", String.valueOf(15), true);
            htmltext = "grave_of_brave_man_q0126_15.htm";
            st.playSound("ItemSound.quest_middle");
        } else if ("reply_41".equalsIgnoreCase(event) && GetMemoState == 15) {
            htmltext = "grave_of_brave_man_q0126_17.htm";
        } else if ("reply_42".equalsIgnoreCase(event) && GetMemoState == 15) {
            st.setCond(14);
            st.set("name_of_cruel_god_two", String.valueOf(16), true);
            htmltext = "grave_of_brave_man_q0126_18.htm";
            st.playSound("ItemSound.quest_middle");
        } else if ("reply_43".equalsIgnoreCase(event) && GetMemoState == 16) {
            st.set("name_of_cruel_god_two", String.valueOf(100), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(7473), true);
            htmltext = "grave_of_brave_man_q0126_21.htm";
        } else if ("reply_80".equalsIgnoreCase(event) && GetMemoState >= 100 && GetMemoState < 200) {
            st.set("name_of_cruel_god_two", String.valueOf(100), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(7473), true);
            htmltext = "grave_of_brave_man_q0126_22.htm";
        } else if ("reply_44".equalsIgnoreCase(event) && GetMemoState == 100) {
            htmltext = "grave_of_brave_man_q0126_24.htm";
        } else if ("reply_100".equalsIgnoreCase(event) && GetMemoState == 100) {
            st.set("name_of_cruel_god_two", String.valueOf(101), true);
            htmltext = "grave_of_brave_man_q0126_26.htm";
        } else if ("reply_101".equalsIgnoreCase(event) && GetMemoState == 100) {
            st.set("name_of_cruel_god_two", String.valueOf(101), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_27.htm";
        } else if ("reply_45".equalsIgnoreCase(event) && GetMemoState == 101) {
            htmltext = "grave_of_brave_man_q0126_28.htm";
        } else if ("reply_102".equalsIgnoreCase(event) && GetMemoState == 101) {
            st.set("name_of_cruel_god_two", String.valueOf(102), true);
            htmltext = "grave_of_brave_man_q0126_30.htm";
        } else if ("reply_103".equalsIgnoreCase(event) && GetMemoState == 101) {
            st.set("name_of_cruel_god_two", String.valueOf(102), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_31.htm";
        } else if ("reply_46".equalsIgnoreCase(event) && GetMemoState == 102) {
            htmltext = "grave_of_brave_man_q0126_32.htm";
        } else if ("reply_104".equalsIgnoreCase(event) && GetMemoState == 102) {
            st.set("name_of_cruel_god_two", String.valueOf(103), true);
            htmltext = "grave_of_brave_man_q0126_34.htm";
        } else if ("reply_105".equalsIgnoreCase(event) && GetMemoState == 102) {
            st.set("name_of_cruel_god_two", String.valueOf(103), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_35.htm";
        } else if ("reply_47".equalsIgnoreCase(event) && GetMemoState == 103) {
            htmltext = "grave_of_brave_man_q0126_36.htm";
        } else if ("reply_106".equalsIgnoreCase(event) && GetMemoState == 103) {
            st.set("name_of_cruel_god_two", String.valueOf(104), true);
            htmltext = "grave_of_brave_man_q0126_38.htm";
        } else if ("reply_107".equalsIgnoreCase(event) && GetMemoState == 103) {
            st.set("name_of_cruel_god_two", String.valueOf(104), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_39.htm";
        } else if ("reply_48".equalsIgnoreCase(event) && GetMemoState == 104) {
            htmltext = "grave_of_brave_man_q0126_40.htm";
        } else if ("reply_108".equalsIgnoreCase(event) && GetMemoState == 104) {
            if (GetMemoStateEx == 7473) {
                st.setCond(15);
                st.set("name_of_cruel_god_two", String.valueOf(200), true);
                st.set("name_of_cruel_god_two_ex", String.valueOf(8302), true);
                htmltext = "grave_of_brave_man_q0126_42.htm";
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "grave_of_brave_man_q0126_43.htm";
            }
        } else if ("reply_109".equalsIgnoreCase(event) && GetMemoState == 104) {
            htmltext = "grave_of_brave_man_q0126_44.htm";
        } else if ("reply_49".equalsIgnoreCase(event) && GetMemoState == 200) {
            htmltext = "grave_of_brave_man_q0126_45.htm";
        } else if ("reply_110".equalsIgnoreCase(event) && GetMemoState == 200) {
            st.set("name_of_cruel_god_two", String.valueOf(201), true);
            htmltext = "grave_of_brave_man_q0126_47.htm";
        } else if ("reply_111".equalsIgnoreCase(event) && GetMemoState == 200) {
            st.set("name_of_cruel_god_two", String.valueOf(201), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_48.htm";
        } else if ("reply_50".equalsIgnoreCase(event) && GetMemoState == 201) {
            htmltext = "grave_of_brave_man_q0126_49.htm";
        } else if ("reply_112".equalsIgnoreCase(event) && GetMemoState == 201) {
            st.set("name_of_cruel_god_two", String.valueOf(202), true);
            htmltext = "grave_of_brave_man_q0126_51.htm";
        } else if ("reply_113".equalsIgnoreCase(event) && GetMemoState == 201) {
            st.set("name_of_cruel_god_two", String.valueOf(202), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_52.htm";
        } else if ("reply_51".equalsIgnoreCase(event) && GetMemoState == 202) {
            htmltext = "grave_of_brave_man_q0126_53.htm";
        } else if ("reply_114".equalsIgnoreCase(event) && GetMemoState == 202) {
            st.set("name_of_cruel_god_two", String.valueOf(203), true);
            htmltext = "grave_of_brave_man_q0126_55.htm";
        } else if ("reply_115".equalsIgnoreCase(event) && GetMemoState == 202) {
            st.set("name_of_cruel_god_two", String.valueOf(203), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_56.htm";
        } else if ("reply_52".equalsIgnoreCase(event) && GetMemoState == 203) {
            htmltext = "grave_of_brave_man_q0126_57.htm";
        } else if ("reply_116".equalsIgnoreCase(event) && GetMemoState == 203) {
            st.set("name_of_cruel_god_two", String.valueOf(204), true);
            htmltext = "grave_of_brave_man_q0126_59.htm";
        } else if ("reply_117".equalsIgnoreCase(event) && GetMemoState == 203) {
            st.set("name_of_cruel_god_two", String.valueOf(204), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_60.htm";
        } else if ("reply_53".equalsIgnoreCase(event) && GetMemoState == 204) {
            htmltext = "grave_of_brave_man_q0126_61.htm";
        } else if ("reply_118".equalsIgnoreCase(event) && GetMemoState == 204) {
            if (GetMemoStateEx == 8302) {
                st.setCond(16);
                st.set("name_of_cruel_god_two", String.valueOf(300), true);
                st.set("name_of_cruel_god_two_ex", String.valueOf(1134), true);
                htmltext = "grave_of_brave_man_q0126_63.htm";
                st.playSound("ItemSound.quest_middle");
            } else {
                st.set("name_of_cruel_god_two", String.valueOf(200), true);
                st.set("name_of_cruel_god_two_ex", String.valueOf(8302), true);
                htmltext = "grave_of_brave_man_q0126_64.htm";
            }
        } else if ("reply_119".equalsIgnoreCase(event) && GetMemoState == 204) {
            st.set("name_of_cruel_god_two", String.valueOf(200), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(8302), true);
            htmltext = "grave_of_brave_man_q0126_65.htm";
        } else if ("reply_54".equalsIgnoreCase(event) && GetMemoState == 300) {
            htmltext = "grave_of_brave_man_q0126_66.htm";
        } else if ("reply_120".equalsIgnoreCase(event) && GetMemoState == 300) {
            st.set("name_of_cruel_god_two", String.valueOf(301), true);
            htmltext = "grave_of_brave_man_q0126_68.htm";
        } else if ("reply_121".equalsIgnoreCase(event) && GetMemoState == 300) {
            st.set("name_of_cruel_god_two", String.valueOf(301), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_69.htm";
        } else if ("reply_55".equalsIgnoreCase(event) && GetMemoState == 301) {
            htmltext = "grave_of_brave_man_q0126_70.htm";
        } else if ("reply_122".equalsIgnoreCase(event) && GetMemoState == 301) {
            st.set("name_of_cruel_god_two", String.valueOf(302), true);
            htmltext = "grave_of_brave_man_q0126_72.htm";
        } else if ("reply_123".equalsIgnoreCase(event) && GetMemoState == 301) {
            st.set("name_of_cruel_god_two", String.valueOf(302), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_73.htm";
        } else if ("reply_56".equalsIgnoreCase(event) && GetMemoState == 302) {
            htmltext = "grave_of_brave_man_q0126_74.htm";
        } else if ("reply_124".equalsIgnoreCase(event) && GetMemoState == 302) {
            st.set("name_of_cruel_god_two", String.valueOf(303), true);
            htmltext = "grave_of_brave_man_q0126_76.htm";
        } else if ("reply_125".equalsIgnoreCase(event) && GetMemoState == 302) {
            st.set("name_of_cruel_god_two", String.valueOf(303), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_77.htm";
        } else if ("reply_57".equalsIgnoreCase(event) && GetMemoState == 303) {
            htmltext = "grave_of_brave_man_q0126_78.htm";
        } else if ("reply_126".equalsIgnoreCase(event) && GetMemoState == 303) {
            st.set("name_of_cruel_god_two", String.valueOf(304), true);
            htmltext = "grave_of_brave_man_q0126_80.htm";
        } else if ("reply_127".equalsIgnoreCase(event) && GetMemoState == 303) {
            st.set("name_of_cruel_god_two", String.valueOf(304), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(97756), true);
            htmltext = "grave_of_brave_man_q0126_81.htm";
        } else if ("reply_58".equalsIgnoreCase(event) && GetMemoState == 304) {
            htmltext = "grave_of_brave_man_q0126_82.htm";
        } else if ("reply_128".equalsIgnoreCase(event) && GetMemoState == 304) {
            if (GetMemoStateEx == 1134) {
                st.setCond(17);
                st.set("name_of_cruel_god_two", String.valueOf(400), true);
                st.set("name_of_cruel_god_two_ex", String.valueOf(0), true);
                htmltext = "grave_of_brave_man_q0126_84.htm";
                st.playSound("ItemSound.quest_middle");
            } else {
                st.set("name_of_cruel_god_two", String.valueOf(300), true);
                st.set("name_of_cruel_god_two_ex", String.valueOf(1134), true);
                htmltext = "grave_of_brave_man_q0126_85.htm";
            }
        } else if ("reply_129".equalsIgnoreCase(event) && GetMemoState == 304) {
            st.set("name_of_cruel_god_two", String.valueOf(300), true);
            st.set("name_of_cruel_god_two_ex", String.valueOf(1134), true);
            htmltext = "grave_of_brave_man_q0126_86.htm";
        } else if ("reply_130".equalsIgnoreCase(event) && GetMemoState == 400 && st.getQuestItemsCount(8783) == 0L) {
            st.giveItems(8783, 1L);
            st.playSound("EtcSound.elcroki_song_full");
            htmltext = "grave_of_brave_man_q0126_87.htm";
            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
        } else if ("reply_131".equalsIgnoreCase(event) && GetMemoState == 400 && st.getQuestItemsCount(8783) >= 1L) {
            st.set("name_of_cruel_god_two", String.valueOf(401), true);
            htmltext = "grave_of_brave_man_q0126_88.htm";
        } else if ("reply_132".equalsIgnoreCase(event) && GetMemoState == 401) {
            st.setCond(18);
            st.set("name_of_cruel_god_two", String.valueOf(402), true);
            htmltext = "grave_of_brave_man_q0126_90.htm";
            st.playSound("ItemSound.quest_middle");
        } else if ("reply_1a".equalsIgnoreCase(event) && GetMemoState == 402 && st.getQuestItemsCount(8783) >= 1L) {
            st.setCond(19);
            st.set("name_of_cruel_god_two", String.valueOf(404), true);
            htmltext = "statue_of_shilen_q0126_05.htm";
            st.playSound("ItemSound.quest_middle");
        } else if ("reply_2a".equalsIgnoreCase(event) && GetMemoState == 404) {
            htmltext = "statue_of_shilen_q0126_07.htm";
        } else if ("reply_3a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_08.htm";
        } else if ("reply_4a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_10.htm";
        } else if ("reply_5a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_11.htm";
        } else if ("reply_6a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_12.htm";
        } else if ("reply_7a".equalsIgnoreCase(event) && GetMemoState == 404) {
            st.set("name_of_cruel_god_two", String.valueOf(405), true);
            htmltext = "statue_of_shilen_q0126_13.htm";
        } else if ("reply_8a".equalsIgnoreCase(event) && GetMemoState == 405) {
            htmltext = "statue_of_shilen_q0126_15.htm";
        } else if ("reply_9a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_16.htm";
        } else if ("reply_10a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_17.htm";
        } else if ("reply_11a".equalsIgnoreCase(event)) {
            htmltext = "statue_of_shilen_q0126_18.htm";
        } else if ("reply_12b".equalsIgnoreCase(event) && GetMemoState == 405) {
            st.setCond(20);
            st.set("name_of_cruel_god_two", String.valueOf(406), true);
            st.takeItems(8783, -1L);
            htmltext = "statue_of_shilen_q0126_19.htm";
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final QuestState qs = st.getPlayer().getQuestState(_125_InTheNameOfEvilPart1.class);
        final int GetMemoState = st.getInt("name_of_cruel_god_two");
        final int GetMemoStateEx = st.getInt("name_of_cruel_god_two_ex");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32115) {
                    break;
                }
                if (qs == null || !qs.isCompleted()) {
                    htmltext = "asama_q0126_04.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 77) {
                    htmltext = "asama_q0126_01.htm";
                    break;
                }
                htmltext = "asama_q0126_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 32115:
                        if (GetMemoState < 1) {
                            htmltext = "asama_q0126_08.htm";
                            break;
                        }
                        if (GetMemoState == 1) {
                            htmltext = "asama_q0126_11.htm";
                            break;
                        }
                        if (GetMemoState >= 2 && GetMemoState < 406) {
                            htmltext = "asama_q0126_12.htm";
                            break;
                        }
                        if (GetMemoState == 406) {
                            htmltext = "asama_q0126_13.htm";
                            break;
                        }
                        if (GetMemoState > 409) {
                            htmltext = "asama_q0126_14.htm";
                            break;
                        }
                        if (GetMemoState == 407) {
                            htmltext = "asama_q0126_17.htm";
                            break;
                        }
                        if (GetMemoState == 408) {
                            htmltext = "asama_q0126_27.htm";
                            break;
                        }
                        if (GetMemoState == 409) {
                            htmltext = "asama_q0126_29.htm";
                            break;
                        }
                        break;
                    case 32114:
                        if (GetMemoState == 409) {
                            htmltext = "mushika_q0126_01.htm";
                            break;
                        }
                        if (GetMemoState < 409) {
                            htmltext = "mushika_q0126_02.htm";
                            break;
                        }
                        if (GetMemoState == 410) {
                            htmltext = "mushika_q0126_03a.htm";
                            break;
                        }
                        break;
                    case 32119:
                        if (GetMemoState == 1) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            st.set("name_of_cruel_god_two", String.valueOf(2), true);
                            htmltext = "ulu_kaimu_stone_q0126_01.htm";
                            break;
                        }
                        if (GetMemoState < 1) {
                            htmltext = "ulu_kaimu_stone_q0126_01a.htm";
                            break;
                        }
                        if (GetMemoState == 2) {
                            htmltext = "ulu_kaimu_stone_q0126_02.htm";
                            break;
                        }
                        if (GetMemoState == 3) {
                            htmltext = "ulu_kaimu_stone_q0126_04.htm";
                            break;
                        }
                        if (GetMemoState == 4) {
                            htmltext = "ulu_kaimu_stone_q0126_08.htm";
                            break;
                        }
                        if (GetMemoState >= 5) {
                            htmltext = "ulu_kaimu_stone_q0126_12.htm";
                            break;
                        }
                        break;
                    case 32120:
                        if (GetMemoState == 5) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            htmltext = "balu_kaimu_stone_q0126_01.htm";
                            break;
                        }
                        if (GetMemoState < 5) {
                            htmltext = "balu_kaimu_stone_q0126_02.htm";
                            break;
                        }
                        if (GetMemoState == 7) {
                            htmltext = "balu_kaimu_stone_q0126_05.htm";
                            break;
                        }
                        if (GetMemoState == 8) {
                            htmltext = "balu_kaimu_stone_q0126_08.htm";
                            break;
                        }
                        if (GetMemoState >= 9) {
                            htmltext = "balu_kaimu_stone_q0126_12.htm";
                            break;
                        }
                        break;
                    case 32121:
                        if (GetMemoState == 9) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            htmltext = "jiuta_kaimu_stone_q0126_01.htm";
                            break;
                        }
                        if (GetMemoState < 9) {
                            htmltext = "jiuta_kaimu_stone_q0126_02.htm";
                            break;
                        }
                        if (GetMemoState == 11) {
                            htmltext = "jiuta_kaimu_stone_q0126_04.htm";
                            break;
                        }
                        if (GetMemoState == 12) {
                            htmltext = "jiuta_kaimu_stone_q0126_10.htm";
                            break;
                        }
                        if (GetMemoState >= 13) {
                            htmltext = "jiuta_kaimu_stone_q0126_16.htm";
                            break;
                        }
                        break;
                    case 32122:
                        if (GetMemoState == 13) {
                            htmltext = "grave_of_brave_man_q0126_01.htm";
                            if (GetMemoStateEx != 1818) {
                                st.setCond(12);
                                st.set("name_of_cruel_god_two_ex", String.valueOf(1818), true);
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            break;
                        } else {
                            if (GetMemoState < 13) {
                                htmltext = "grave_of_brave_man_q0126_02.htm";
                                break;
                            }
                            if (GetMemoState == 14) {
                                htmltext = "grave_of_brave_man_q0126_07.htm";
                                break;
                            }
                            if (GetMemoState == 15) {
                                htmltext = "grave_of_brave_man_q0126_16.htm";
                                break;
                            }
                            if (GetMemoState == 16) {
                                htmltext = "grave_of_brave_man_q0126_19.htm";
                                break;
                            }
                            if (GetMemoState == 100) {
                                htmltext = "grave_of_brave_man_q0126_25.htm";
                                break;
                            }
                            if (GetMemoState == 101) {
                                htmltext = "grave_of_brave_man_q0126_29.htm";
                                break;
                            }
                            if (GetMemoState == 102) {
                                htmltext = "grave_of_brave_man_q0126_33.htm";
                                break;
                            }
                            if (GetMemoState == 103) {
                                htmltext = "grave_of_brave_man_q0126_37.htm";
                                break;
                            }
                            if (GetMemoState == 104) {
                                htmltext = "grave_of_brave_man_q0126_41.htm";
                                break;
                            }
                            if (GetMemoState == 200) {
                                htmltext = "grave_of_brave_man_q0126_46.htm";
                                break;
                            }
                            if (GetMemoState == 200) {
                                htmltext = "grave_of_brave_man_q0126_46.htm";
                                break;
                            }
                            if (GetMemoState == 201) {
                                htmltext = "grave_of_brave_man_q0126_50.htm";
                                break;
                            }
                            if (GetMemoState == 202) {
                                htmltext = "grave_of_brave_man_q0126_54.htm";
                                break;
                            }
                            if (GetMemoState == 203) {
                                htmltext = "grave_of_brave_man_q0126_58.htm";
                                break;
                            }
                            if (GetMemoState == 204) {
                                htmltext = "grave_of_brave_man_q0126_62.htm";
                                break;
                            }
                            if (GetMemoState == 300) {
                                htmltext = "grave_of_brave_man_q0126_67.htm";
                                break;
                            }
                            if (GetMemoState == 301) {
                                htmltext = "grave_of_brave_man_q0126_71.htm";
                                break;
                            }
                            if (GetMemoState == 302) {
                                htmltext = "grave_of_brave_man_q0126_75.htm";
                                break;
                            }
                            if (GetMemoState == 303) {
                                htmltext = "grave_of_brave_man_q0126_79.htm";
                                break;
                            }
                            if (GetMemoState == 304) {
                                htmltext = "grave_of_brave_man_q0126_83.htm";
                                break;
                            }
                            if (GetMemoState == 400 && st.getQuestItemsCount(8783) == 0L) {
                                st.giveItems(8783, 1L);
                                st.playSound("EtcSound.elcroki_song_full");
                                htmltext = "grave_of_brave_man_q0126_86a.htm";
                                npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                                break;
                            }
                            if (GetMemoState == 400 && st.getQuestItemsCount(8783) == 1L) {
                                htmltext = "grave_of_brave_man_q0126_87a.htm";
                                break;
                            }
                            if (GetMemoState == 401) {
                                htmltext = "grave_of_brave_man_q0126_89.htm";
                                break;
                            }
                            if (GetMemoState >= 402) {
                                htmltext = "grave_of_brave_man_q0126_91.htm";
                                break;
                            }
                            break;
                        }
                    default:
                        if (npcId != 32109) {
                            break;
                        }
                        if (GetMemoState == 402 && st.getQuestItemsCount(8783) >= 1L) {
                            htmltext = "statue_of_shilen_q0126_02.htm";
                            break;
                        }
                        if (GetMemoState < 402) {
                            htmltext = "statue_of_shilen_q0126_03.htm";
                            break;
                        }
                        if (GetMemoState > 406) {
                            htmltext = "statue_of_shilen_q0126_04.htm";
                            break;
                        }
                        if (GetMemoState == 404) {
                            htmltext = "statue_of_shilen_q0126_06.htm";
                            break;
                        }
                        if (GetMemoState == 405) {
                            htmltext = "statue_of_shilen_q0126_14.htm";
                            break;
                        }
                        if (GetMemoState == 406) {
                            htmltext = "statue_of_shilen_q0126_20.htm";
                            break;
                        }
                        break;
                }
            }
        }
        return htmltext;
    }
}
