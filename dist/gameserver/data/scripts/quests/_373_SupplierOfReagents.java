package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _373_SupplierOfReagents extends Quest {
    private static final int bandor = 30166;
    private static final int alchemical_mixing_jar = 31149;
    private static final int credion = 20813;
    private static final int hallates_maiden = 20822;
    private static final int platinum_tribe_shaman = 20828;
    private static final int hallates_guardian = 21061;
    private static final int plat_protect_shaman = 21066;
    private static final int lava_wyrm = 21111;
    private static final int hames_orc_shaman = 21115;
    private static final int mixing_manual = 6317;
    private static final int dracoplasm = 6021;
    private static final int swift_attack_potion = 735;
    private static final int magma_dust = 6022;
    private static final int scroll_of_resurrection = 737;
    private static final int moon_dust = 6023;
    private static final int cursed_bone = 2508;
    private static final int necroplasm = 6024;
    private static final int enria = 4042;
    private static final int asofe = 4043;
    private static final int thons = 4044;
    private static final int demonplasm = 6025;
    private static final int rp_avadon_gloves_i = 4953;
    private static final int rp_avadon_boots_i = 4959;
    private static final int rp_shrnoens_gauntlet_i = 4960;
    private static final int rp_shrnoens_boots_i = 4958;
    private static final int inferno_dust = 6026;
    private static final int rp_blue_wolves_gloves_i = 4998;
    private static final int rp_blue_wolves_boots_i = 4992;
    private static final int draconic_essence = 6027;
    private static final int fire_essence = 6028;
    private static final int rp_doom_gloves_i = 4993;
    private static final int rp_doom_boots_i = 4999;
    private static final int lunargent = 6029;
    private static final int sealed_dark_crystal_leather_mail_pattern = 5478;
    private static final int sealed_tallum_leather_mail_pattern = 5479;
    private static final int sealed_leather_mail_of_nightmare_fabric = 5480;
    private static final int sealed_majestic_leather_mail_fabric = 5481;
    private static final int sealed_legging_of_dark_crystal_design = 5482;
    private static final int midnight_oil = 6030;
    private static final int sealed_dark_crystal_breastplate_pattern = 5520;
    private static final int sealed_tallum_plate_armor_pattern = 5521;
    private static final int sealed_armor_of_nightmare_pattern = 5522;
    private static final int sealed_majestic_platte_armor_pattern = 5523;
    private static final int sealed_dark_crystal_gaiters_pattern = 5524;
    private static final int demonic_essence = 6031;
    private static final int abyss_oil = 6032;
    private static final int hellfire_oil = 6033;
    private static final int nightmare_oil = 6034;
    private static final int wyrms_blood = 6011;
    private static final int lava_stone = 6012;
    private static final int moonstone_shard = 6013;
    private static final int decaying_bone = 6014;
    private static final int demons_blood = 6015;
    private static final int infernium_ore = 6016;
    private static final int blood_root = 6017;
    private static final int volcanic_ash = 6018;
    private static final int quicksilver = 6019;
    private static final int sulfur = 6020;
    private static final int ingredient_pouch1 = 6007;
    private static final int ingredient_pouch2 = 6008;
    private static final int ingredient_pouch3 = 6009;
    private static final int ingredient_box = 6010;
    private static final int tower_shield = 103;
    private static final int drake_leather_boots = 2437;
    private static final int square_shield = 630;
    private static final int shrnoens_gauntlet = 612;
    private static final int avadon_gloves = 2464;
    private static final int shrnoens_boots = 554;
    private static final int avadon_boots = 600;
    private static final int blue_wolves_boots = 2439;
    private static final int doom_boots = 601;
    private static final int blue_wolves_gloves = 2487;
    private static final int doom_gloves = 2475;
    private static final int wesleys_mixing_stone = 5904;
    private static final int pure_silver = 6320;

    public _373_SupplierOfReagents() {
        super(true);
        addStartNpc(30166);
        addTalkId(31149);
        addKillId(20813, 20822, 20828, 21061, 21066, 21111, 21115);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        final int GetMemoState = st.getInt("reagent_supplier");
        final int GetMemoStateEx = st.getInt("reagent_supplier_ex");
        if (npcId == 30166) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                st.setCond(1);
                st.setState(2);
                st.giveItems(5904, 1L);
                st.giveItems(6317, 1L);
                st.playSound("ItemSound.quest_accept");
                htmltext = "bandor_q0373_04.htm";
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "bandor_q0373_03.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "bandor_q0373_06.htm";
            } else if ("reply_3".equalsIgnoreCase(event)) {
                htmltext = "bandor_q0373_07.htm";
            } else if ("reply_4".equalsIgnoreCase(event)) {
                htmltext = "bandor_q0373_08.htm";
            } else if ("reply_5".equalsIgnoreCase(event)) {
                st.takeItems(5904, -1L);
                st.takeItems(6317, -1L);
                st.unset("reagent_supplier");
                st.unset("reagent_supplier_ex");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "bandor_q0373_09.htm";
            } else if ("reply_11".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6021) >= 1L) {
                    st.giveItems(735, 1L);
                    st.takeItems(6021, 1L);
                    htmltext = "bandor_q0373_10.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_12".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6022) >= 1L) {
                    st.giveItems(737, 2L);
                    st.takeItems(6022, 1L);
                    htmltext = "bandor_q0373_11.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_13".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6023) >= 1L) {
                    st.giveItems(2508, 25L);
                    st.takeItems(6023, 1L);
                    htmltext = "bandor_q0373_12.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_14".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6024) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 32) {
                        st.giveItems(4042, 1L);
                    } else if (i0 < 66) {
                        st.giveItems(4043, 1L);
                    } else {
                        st.giveItems(4044, 1L);
                    }
                    st.giveItems(57, 500L);
                    st.takeItems(6024, 1L);
                    htmltext = "bandor_q0373_13.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_15".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6025) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 28) {
                        st.giveItems(735, 20L);
                    } else if (i0 < 68) {
                        st.giveItems(4953, 1L);
                    } else {
                        st.giveItems(4959, 1L);
                    }
                    st.takeItems(6025, 1L);
                    htmltext = "bandor_q0373_14.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_16".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6026) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 40) {
                        st.giveItems(2508, 200L);
                    } else if (i0 < 70) {
                        st.giveItems(4960, 1L);
                    } else {
                        st.giveItems(4958, 1L);
                    }
                    st.takeItems(6026, 1L);
                    htmltext = "bandor_q0373_15.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_17".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6027) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 40) {
                        st.giveItems(4998, 1L);
                    } else if (i0 < 80) {
                        st.giveItems(4992, 1L);
                    } else {
                        st.giveItems(737, 20L);
                    }
                    st.takeItems(6027, 1L);
                    htmltext = "bandor_q0373_16.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_18".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6028) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 90) {
                        st.giveItems(4993, 1L);
                        st.giveItems(4999, 1L);
                    } else {
                        st.giveItems(4042, 2L);
                    }
                    st.takeItems(6028, 1L);
                    htmltext = "bandor_q0373_17.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_19".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6029) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 20) {
                        st.giveItems(5478, 2L);
                    } else if (i0 < 40) {
                        st.giveItems(5479, 2L);
                    } else if (i0 < 60) {
                        st.giveItems(5480, 2L);
                    } else if (i0 < 80) {
                        st.giveItems(5481, 2L);
                    } else {
                        st.giveItems(5482, 2L);
                    }
                    st.giveItems(57, 8000L);
                    st.takeItems(6029, 1L);
                    htmltext = "bandor_q0373_18.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_20".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6030) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 20) {
                        st.giveItems(5520, 3L);
                    } else if (i0 < 40) {
                        st.giveItems(5521, 3L);
                    } else if (i0 < 60) {
                        st.giveItems(5522, 3L);
                    } else if (i0 < 80) {
                        st.giveItems(5523, 3L);
                    } else {
                        st.giveItems(5524, 3L);
                    }
                    st.giveItems(57, 8000L);
                    st.takeItems(6030, 1L);
                    htmltext = "bandor_q0373_19.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_21".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6031) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 40) {
                        st.giveItems(57, 32000L);
                    } else if (i0 < 80) {
                        st.giveItems(57, 24500L);
                    } else {
                        st.giveItems(57, 16000L);
                    }
                    st.takeItems(6031, 1L);
                    htmltext = "bandor_q0373_20.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_22".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6032) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 30) {
                        st.giveItems(103, 1L);
                    } else if (i0 < 60) {
                        st.giveItems(2437, 1L);
                    } else {
                        st.giveItems(630, 1L);
                    }
                    st.giveItems(57, 5000L);
                    st.takeItems(6032, 1L);
                    htmltext = "bandor_q0373_21.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_23".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6033) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 25) {
                        st.giveItems(612, 1L);
                    } else if (i0 < 50) {
                        st.giveItems(2464, 1L);
                    } else if (i0 < 75) {
                        st.giveItems(554, 1L);
                    } else {
                        st.giveItems(600, 1L);
                    }
                    st.takeItems(6033, 1L);
                    htmltext = "bandor_q0373_22.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            } else if ("reply_24".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6034) >= 1L) {
                    final int i0 = Rnd.get(100);
                    if (i0 < 17) {
                        st.giveItems(2439, 1L);
                    } else if (i0 < 34) {
                        st.giveItems(601, 1L);
                    } else if (i0 < 51) {
                        st.giveItems(2487, 1L);
                    } else if (i0 < 68) {
                        st.giveItems(2475, 1L);
                    } else {
                        st.giveItems(4992, 1L);
                        st.giveItems(4998, 1L);
                    }
                    st.giveItems(57, 19000L);
                    st.takeItems(6034, 1L);
                    htmltext = "bandor_q0373_23.htm";
                } else {
                    htmltext = "bandor_q0373_24.htm";
                }
            }
        } else if (npcId == 31149) {
            if ("reply_1".equalsIgnoreCase(event)) {
                st.set("reagent_supplier", String.valueOf(0), true);
                st.set("reagent_supplier_ex", String.valueOf(0), true);
                htmltext = "alchemical_mixing_jar_q0373_02.htm";
            } else if ("reply_11".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6011) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(11), true);
                    htmltext = "alchemical_mixing_jar_q0373_03.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_12".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6012) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(12), true);
                    htmltext = "alchemical_mixing_jar_q0373_04.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_13".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6013) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(13), true);
                    htmltext = "alchemical_mixing_jar_q0373_05.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_14".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6014) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(14), true);
                    htmltext = "alchemical_mixing_jar_q0373_06.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_15".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6015) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(15), true);
                    htmltext = "alchemical_mixing_jar_q0373_07.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_16".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6016) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(16), true);
                    htmltext = "alchemical_mixing_jar_q0373_08.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_17".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6021) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(17), true);
                    htmltext = "alchemical_mixing_jar_q0373_09.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_18".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6022) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(18), true);
                    htmltext = "alchemical_mixing_jar_q0373_10.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_19".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6023) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(19), true);
                    htmltext = "alchemical_mixing_jar_q0373_11.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_20".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6024) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(20), true);
                    htmltext = "alchemical_mixing_jar_q0373_12.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_21".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6025) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(21), true);
                    htmltext = "alchemical_mixing_jar_q0373_13.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_22".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6026) >= 10L) {
                    st.set("reagent_supplier", String.valueOf(22), true);
                    htmltext = "alchemical_mixing_jar_q0373_14.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_23".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6028) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(23), true);
                    htmltext = "alchemical_mixing_jar_q0373_15.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_24".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6029) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(24), true);
                    htmltext = "alchemical_mixing_jar_q0373_16.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "alchemical_mixing_jar_q0373_18.htm";
            } else if ("reply_31".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6017) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1100), true);
                    htmltext = "alchemical_mixing_jar_q0373_19.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_25.htm";
                }
            } else if ("reply_32".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6018) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1200), true);
                    htmltext = "alchemical_mixing_jar_q0373_20.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_33".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6019) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1300), true);
                    htmltext = "alchemical_mixing_jar_q0373_21.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_34".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6020) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1400), true);
                    htmltext = "alchemical_mixing_jar_q0373_22.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_35".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6031) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1500), true);
                    htmltext = "alchemical_mixing_jar_q0373_23.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_36".equalsIgnoreCase(event)) {
                if (st.getQuestItemsCount(6030) >= 1L) {
                    st.set("reagent_supplier", String.valueOf(GetMemoState + 1600), true);
                    htmltext = "alchemical_mixing_jar_q0373_24.htm";
                    st.playSound("SkillSound5.liquid_mix_01");
                } else {
                    switch (GetMemoState) {
                        case 11:
                            st.takeItems(6011, 10L);
                            break;
                        case 12:
                            st.takeItems(6012, 10L);
                            break;
                        case 13:
                            st.takeItems(6013, 10L);
                            break;
                        case 14:
                            st.takeItems(6014, 10L);
                            break;
                        case 15:
                            st.takeItems(6015, 10L);
                            break;
                        case 16:
                            st.takeItems(6016, 10L);
                            break;
                        case 17:
                            st.takeItems(6021, 10L);
                            break;
                        case 18:
                            st.takeItems(6022, 10L);
                            break;
                        case 19:
                            st.takeItems(6023, 10L);
                            break;
                        case 20:
                            st.takeItems(6024, 10L);
                            break;
                        case 21:
                            st.takeItems(6025, 10L);
                            break;
                        case 22:
                            st.takeItems(6026, 10L);
                            break;
                        case 23:
                            st.takeItems(6028, 1L);
                            break;
                        case 24:
                            st.takeItems(6029, 1L);
                            break;
                    }
                    htmltext = "alchemical_mixing_jar_q0373_17.htm";
                }
            } else if ("reply_3".equalsIgnoreCase(event)) {
                if (GetMemoState == 1324) {
                    htmltext = "alchemical_mixing_jar_q0373_26a.htm";
                } else {
                    htmltext = "alchemical_mixing_jar_q0373_26.htm";
                }
            } else if ("reply_4".equalsIgnoreCase(event)) {
                st.set("reagent_supplier_ex", String.valueOf(1), true);
                htmltext = "alchemical_mixing_jar_q0373_27.htm";
            } else if ("reply_5".equalsIgnoreCase(event)) {
                if (Rnd.get(100) < 33) {
                    st.set("reagent_supplier_ex", String.valueOf(3), true);
                    htmltext = "alchemical_mixing_jar_q0373_28a.htm";
                } else {
                    st.set("reagent_supplier_ex", String.valueOf(0), true);
                    htmltext = "alchemical_mixing_jar_q0373_28a.htm";
                }
            } else if ("reply_6".equalsIgnoreCase(event)) {
                if (Rnd.get(100) < 20) {
                    st.set("reagent_supplier_ex", String.valueOf(5), true);
                    htmltext = "alchemical_mixing_jar_q0373_29a.htm";
                } else {
                    st.set("reagent_supplier_ex", String.valueOf(0), true);
                    htmltext = "alchemical_mixing_jar_q0373_29a.htm";
                }
            } else if ("reply_7".equalsIgnoreCase(event) && GetMemoStateEx != 0) {
                switch (GetMemoState) {
                    case 1111:
                        if (st.getQuestItemsCount(6011) >= 10L && st.getQuestItemsCount(6017) >= 1L) {
                            st.takeItems(6011, 10L);
                            st.takeItems(6017, 1L);
                            st.giveItems(6021, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_30.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1212:
                        if (st.getQuestItemsCount(6012) >= 10L && st.getQuestItemsCount(6018) >= 1L) {
                            st.takeItems(6012, 10L);
                            st.takeItems(6018, 1L);
                            st.giveItems(6022, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_31.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1213:
                        if (st.getQuestItemsCount(6013) >= 10L && st.getQuestItemsCount(6018) >= 1L) {
                            st.takeItems(6013, 10L);
                            st.takeItems(6018, 1L);
                            st.giveItems(6023, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_32.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1114:
                        if (st.getQuestItemsCount(6014) >= 10L && st.getQuestItemsCount(6017) >= 1L) {
                            st.takeItems(6014, 10L);
                            st.takeItems(6017, 1L);
                            st.giveItems(6024, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_33.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1115:
                        if (st.getQuestItemsCount(6015) >= 10L && st.getQuestItemsCount(6017) >= 1L) {
                            st.takeItems(6015, 10L);
                            st.takeItems(6017, 1L);
                            st.giveItems(6025, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_34.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1216:
                        if (st.getQuestItemsCount(6016) >= 10L && st.getQuestItemsCount(6018) >= 1L) {
                            st.takeItems(6016, 10L);
                            st.takeItems(6018, 1L);
                            st.giveItems(6026, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_35.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1317:
                        if (st.getQuestItemsCount(6021) >= 10L && st.getQuestItemsCount(6019) >= 1L) {
                            st.takeItems(6021, 10L);
                            st.takeItems(6019, 1L);
                            st.giveItems(6027, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_36.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1418:
                        if (st.getQuestItemsCount(6022) >= 10L && st.getQuestItemsCount(6020) >= 1L) {
                            st.takeItems(6022, 10L);
                            st.takeItems(6020, 1L);
                            st.giveItems(6028, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_37.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1319:
                        if (st.getQuestItemsCount(6023) >= 10L && st.getQuestItemsCount(6019) >= 1L) {
                            st.takeItems(6023, 10L);
                            st.takeItems(6019, 1L);
                            st.giveItems(6029, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_38.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1320:
                        if (st.getQuestItemsCount(6024) >= 10L && st.getQuestItemsCount(6019) >= 1L) {
                            st.takeItems(6024, 10L);
                            st.takeItems(6019, 1L);
                            st.giveItems(6030, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_39.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1421:
                        if (st.getQuestItemsCount(6025) >= 10L && st.getQuestItemsCount(6020) >= 1L) {
                            st.takeItems(6025, 10L);
                            st.takeItems(6020, 1L);
                            st.giveItems(6031, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_40.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1422:
                        if (st.getQuestItemsCount(6026) >= 10L && st.getQuestItemsCount(6020) >= 1L) {
                            st.takeItems(6026, 10L);
                            st.takeItems(6020, 1L);
                            st.giveItems(6032, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_41.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1523:
                        if (st.getQuestItemsCount(6028) >= 1L && st.getQuestItemsCount(6031) >= 1L) {
                            st.takeItems(6028, 1L);
                            st.takeItems(6031, 1L);
                            st.giveItems(6033, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_42.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1624:
                        if (st.getQuestItemsCount(6029) >= 1L && st.getQuestItemsCount(6030) >= 1L) {
                            st.takeItems(6029, 1L);
                            st.takeItems(6030, 1L);
                            st.giveItems(6034, (long) GetMemoStateEx);
                            st.set("reagent_supplier", String.valueOf(0), true);
                            st.set("reagent_supplier_ex", String.valueOf(0), true);
                            htmltext = "alchemical_mixing_jar_q0373_43.htm";
                            st.playSound("SkillSound5.liquid_success_01");
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                            st.playSound("SkillSound5.liquid_fail_01");
                        }
                        break;
                    case 1324:
                        if (st.getQuestItemsCount(6320) == 0L) {
                            if (st.getQuestItemsCount(6029) >= 1L && st.getQuestItemsCount(6019) >= 1L) {
                                st.takeItems(6029, 1L);
                                st.takeItems(6019, 1L);
                                st.giveItems(6320, 1L);
                                st.set("reagent_supplier", String.valueOf(0), true);
                                st.set("reagent_supplier_ex", String.valueOf(0), true);
                                htmltext = "alchemical_mixing_jar_q0373_46.htm";
                                st.playSound("SkillSound5.liquid_success_01");
                            } else {
                                htmltext = "alchemical_mixing_jar_q0373_44.htm";
                                st.playSound("SkillSound5.liquid_fail_01");
                            }
                        } else {
                            htmltext = "alchemical_mixing_jar_q0373_44.htm";
                        }
                        break;
                    //case 1324:
                    //    htmltext = "alchemical_mixing_jar_q0373_44.htm";
                    //    break;
                    default:
                        final int i2 = GetMemoState / 100;
                        final int i3 = GetMemoState % 100;
                        switch (i3) {
                            case 11:
                                st.takeItems(6011, 10L);
                                break;
                            case 12:
                                st.takeItems(6012, 10L);
                                break;
                            case 13:
                                st.takeItems(6013, 10L);
                                break;
                            case 14:
                                st.takeItems(6014, 10L);
                                break;
                            case 15:
                                st.takeItems(6015, 10L);
                                break;
                            case 16:
                                st.takeItems(6016, 10L);
                                break;
                            case 17:
                                st.takeItems(6021, 10L);
                                break;
                            case 18:
                                st.takeItems(6022, 10L);
                                break;
                            case 19:
                                st.takeItems(6023, 10L);
                                break;
                            case 20:
                                st.takeItems(6024, 10L);
                                break;
                            case 21:
                                st.takeItems(6025, 10L);
                                break;
                            case 22:
                                st.takeItems(6026, 10L);
                                break;
                            case 23:
                                st.takeItems(6028, 1L);
                                break;
                            case 24:
                                st.takeItems(6029, 1L);
                                break;
                        }
                        switch (i2) {
                            case 11:
                                st.takeItems(6017, 1L);
                                break;
                            case 12:
                                st.takeItems(6018, 1L);
                                break;
                            case 13:
                                st.takeItems(6019, 1L);
                                break;
                            case 14:
                                st.takeItems(6020, 1L);
                                break;
                            case 15:
                                st.takeItems(6031, 1L);
                                break;
                            case 16:
                                st.takeItems(6030, 1L);
                                break;
                        }
                        htmltext = "alchemical_mixing_jar_q0373_44.htm";
                        st.playSound("SkillSound5.liquid_fail_01");
                        break;
                }
            } else if ("reply_7".equalsIgnoreCase(event) && GetMemoStateEx == 0) {
                final int i2 = GetMemoState / 100;
                final int i3 = GetMemoState % 100;
                switch (i3) {
                    case 11:
                        st.takeItems(6011, 10L);
                        break;
                    case 12:
                        st.takeItems(6012, 10L);
                        break;
                    case 13:
                        st.takeItems(6013, 10L);
                        break;
                    case 14:
                        st.takeItems(6014, 10L);
                        break;
                    case 15:
                        st.takeItems(6015, 10L);
                        break;
                    case 16:
                        st.takeItems(6016, 10L);
                        break;
                    case 17:
                        st.takeItems(6021, 10L);
                        break;
                    case 18:
                        st.takeItems(6022, 10L);
                        break;
                    case 19:
                        st.takeItems(6023, 10L);
                        break;
                    case 20:
                        st.takeItems(6024, 10L);
                        break;
                    case 21:
                        st.takeItems(6025, 10L);
                        break;
                    case 22:
                        st.takeItems(6026, 10L);
                        break;
                    case 23:
                        st.takeItems(6028, 1L);
                        break;
                    case 24:
                        st.takeItems(6029, 1L);
                        break;
                }
                switch (i2) {
                    case 11:
                        st.takeItems(6017, 1L);
                        break;
                    case 12:
                        st.takeItems(6018, 1L);
                        break;
                    case 13:
                        st.takeItems(6019, 1L);
                        break;
                    case 14:
                        st.takeItems(6020, 1L);
                        break;
                    case 15:
                        st.takeItems(6031, 1L);
                        break;
                    case 16:
                        st.takeItems(6030, 1L);
                        break;
                }
                htmltext = "alchemical_mixing_jar_q0373_45.htm";
                st.playSound("SkillSound5.liquid_fail_01");
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("reagent_supplier");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 30166) {
                    break;
                }
                if (st.getPlayer().getLevel() < 57) {
                    htmltext = "bandor_q0373_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                htmltext = "bandor_q0373_02.htm";
                break;
            }
            case 2: {
                if (npcId == 30166) {
                    if (GetMemoState == 0) {
                        htmltext = "bandor_q0373_05.htm";
                        break;
                    }
                    break;
                } else {
                    if (npcId == 31149 && GetMemoState >= 0) {
                        htmltext = "alchemical_mixing_jar_q0373_01.htm";
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
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 20813: {
                final int i4 = Rnd.get(1000);
                if (i4 < 618) {
                    st.rollAndGive(6014, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i4 < 1000) {
                    st.rollAndGive(6019, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 20822: {
                final int i4 = Rnd.get(100);
                if (i4 < 45) {
                    st.rollAndGive(6007, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i4 < 65) {
                    st.rollAndGive(6018, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 20828: {
                final int i4 = Rnd.get(1000);
                if (i4 < 658) {
                    st.rollAndGive(6008, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i4 < 100) {
                    st.rollAndGive(6019, 2, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 21061: {
                final int i4 = Rnd.get(1000);
                if (i4 < 766) {
                    st.rollAndGive(6015, 3, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i4 < 876) {
                    st.rollAndGive(6013, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 21066: {
                final int i4 = Rnd.get(1000);
                if (i4 < 444) {
                    st.rollAndGive(6010, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 21111: {
                final int i4 = Rnd.get(1000);
                if (i4 < 666) {
                    st.rollAndGive(6011, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                } else if (i4 < 989) {
                    st.rollAndGive(6012, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
            case 21115: {
                final int i4 = Rnd.get(1000);
                if (i4 < 616) {
                    st.rollAndGive(6009, 1, 100.0);
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            }
        }
        return null;
    }
}
