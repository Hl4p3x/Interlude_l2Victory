package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _217_TestimonyOfTrust extends Quest {
    private static final int MARK_OF_TRUST_ID = 2734;
    private static final int LETTER_TO_ELF_ID = 1558;
    private static final int LETTER_TO_DARKELF_ID = 1556;
    private static final int LETTER_TO_DWARF_ID = 2737;
    private static final int LETTER_TO_ORC_ID = 2738;
    private static final int LETTER_TO_SERESIN_ID = 2739;
    private static final int SCROLL_OF_DARKELF_TRUST_ID = 2740;
    private static final int SCROLL_OF_ELF_TRUST_ID = 2741;
    private static final int SCROLL_OF_DWARF_TRUST_ID = 2742;
    private static final int SCROLL_OF_ORC_TRUST_ID = 2743;
    private static final int RECOMMENDATION_OF_HOLLIN_ID = 2744;
    private static final int ORDER_OF_OZZY_ID = 2745;
    private static final int BREATH_OF_WINDS_ID = 2746;
    private static final int SEED_OF_VERDURE_ID = 2747;
    private static final int LETTER_OF_THIFIELL_ID = 2748;
    private static final int BLOOD_OF_GUARDIAN_BASILISK_ID = 2749;
    private static final int GIANT_APHID_ID = 2750;
    private static final int STAKATOS_FLUIDS_ID = 2751;
    private static final int BASILISK_PLASMA_ID = 2752;
    private static final int HONEY_DEW_ID = 2753;
    private static final int STAKATO_ICHOR_ID = 2754;
    private static final int ORDER_OF_CLAYTON_ID = 2755;
    private static final int PARASITE_OF_LOTA_ID = 2756;
    private static final int LETTER_TO_MANAKIA_ID = 2757;
    private static final int LETTER_OF_MANAKIA_ID = 2758;
    private static final int LETTER_TO_NICHOLA_ID = 2759;
    private static final int ORDER_OF_NICHOLA_ID = 2760;
    private static final int HEART_OF_PORTA_ID = 2761;
    private static final int RewardExp = 39571;
    private static final int RewardSP = 2500;

    public _217_TestimonyOfTrust() {
        super(false);
        addStartNpc(30191);
        addTalkId(30031);
        addTalkId(30154);
        addTalkId(30358);
        addTalkId(30464);
        addTalkId(30515);
        addTalkId(30531);
        addTalkId(30565);
        addTalkId(30621);
        addTalkId(30657);
        addKillId(20013);
        addKillId(20157);
        addKillId(20019);
        addKillId(20213);
        addKillId(20230);
        addKillId(20232);
        addKillId(20234);
        addKillId(20036);
        addKillId(20044);
        addKillId(27120);
        addKillId(27121);
        addKillId(20550);
        addKillId(20553);
        addKillId(20082);
        addKillId(20084);
        addKillId(20086);
        addKillId(20087);
        addKillId(20088);
        addQuestItem(2740, 2741, 2742, 2743, 2746, 2747, 2745, 1558, 2755, 2752, 2754, 2753, 1556, 2748, 2739, 2738, 2758, 2757, 2756, 2737, 2759, 2761, 2760, 2744, 2749, 2751, 2750);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("1".equalsIgnoreCase(event)) {
            if (!st.getPlayer().getVarB("dd2")) {
                st.giveItems(7562, 96L);
                st.getPlayer().setVar("dd2", "1", -1L);
            }
            htmltext = "hollin_q0217_04.htm";
            st.setCond(1);
            st.set("id", "0");
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1558, 1L);
            st.giveItems(1556, 1L);
        } else if ("30154_1".equalsIgnoreCase(event)) {
            htmltext = "ozzy_q0217_02.htm";
        } else if ("30154_2".equalsIgnoreCase(event)) {
            htmltext = "ozzy_q0217_03.htm";
            st.takeItems(1558, 1L);
            st.giveItems(2745, 1L);
            st.setCond(2);
        } else if ("30358_1".equalsIgnoreCase(event)) {
            htmltext = "tetrarch_thifiell_q0217_02.htm";
            st.takeItems(1556, 1L);
            st.giveItems(2748, 1L);
            st.setCond(5);
        } else if ("30657_1".equalsIgnoreCase(event)) {
            if (st.getPlayer().getLevel() >= 38) {
                htmltext = "cardinal_seresin_q0217_03.htm";
                st.takeItems(2739, 1L);
                st.giveItems(2738, 1L);
                st.giveItems(2737, 1L);
                st.setCond(12);
            } else {
                htmltext = "cardinal_seresin_q0217_02.htm";
            }
        } else if ("30565_1".equalsIgnoreCase(event)) {
            htmltext = "kakai_the_lord_of_flame_q0217_02.htm";
            st.takeItems(2738, 1L);
            st.giveItems(2757, 1L);
            st.setCond(13);
        } else if ("30515_1".equalsIgnoreCase(event)) {
            htmltext = "seer_manakia_q0217_02.htm";
            st.takeItems(2757, 1L);
            st.setCond(14);
        } else if ("30531_1".equalsIgnoreCase(event)) {
            htmltext = "first_elder_lockirin_q0217_02.htm";
            st.takeItems(2737, 1L);
            st.giveItems(2759, 1L);
            st.setCond(18);
        } else if ("30621_1".equalsIgnoreCase(event)) {
            htmltext = "maestro_nikola_q0217_02.htm";
            st.takeItems(2759, 1L);
            st.giveItems(2760, 1L);
            st.setCond(19);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(2734) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 30191) {
            if (cond == 0) {
                if (st.getPlayer().getRace() == Race.human) {
                    if (st.getPlayer().getLevel() >= 37) {
                        htmltext = "hollin_q0217_03.htm";
                    } else {
                        htmltext = "hollin_q0217_01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "hollin_q0217_02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 9 && st.getQuestItemsCount(2741) > 0L && st.getQuestItemsCount(2740) > 0L) {
                htmltext = "hollin_q0217_05.htm";
                st.takeItems(2740, 1L);
                st.takeItems(2741, 1L);
                st.giveItems(2739, 1L);
                st.setCond(10);
            } else if (cond == 22 && st.getQuestItemsCount(2742) > 0L && st.getQuestItemsCount(2743) > 0L) {
                htmltext = "hollin_q0217_06.htm";
                st.takeItems(2742, 1L);
                st.takeItems(2743, 1L);
                st.giveItems(2744, 1L);
                st.setCond(23);
            } else if (cond == 19) {
                htmltext = "hollin_q0217_07.htm";
            } else if (cond == 1) {
                htmltext = "hollin_q0217_08.htm";
            } else if (cond == 8) {
                htmltext = "hollin_q0217_09.htm";
            }
        } else if (npcId == 30154) {
            if (cond == 1 && st.getQuestItemsCount(1558) > 0L) {
                htmltext = "ozzy_q0217_01.htm";
            } else if (cond == 2 && st.getQuestItemsCount(2745) > 0L) {
                htmltext = "ozzy_q0217_04.htm";
            } else if (cond == 3 && st.getQuestItemsCount(2746) > 0L && st.getQuestItemsCount(2747) > 0L) {
                htmltext = "ozzy_q0217_05.htm";
                st.takeItems(2746, 1L);
                st.takeItems(2747, 1L);
                st.takeItems(2745, 1L);
                st.giveItems(2741, 1L);
                st.setCond(4);
            } else if (cond == 4) {
                htmltext = "ozzy_q0217_06.htm";
            }
        } else if (npcId == 30358) {
            if (cond == 4 && st.getQuestItemsCount(1556) > 0L) {
                htmltext = "tetrarch_thifiell_q0217_01.htm";
            } else if (cond == 8 && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3L) {
                st.takeItems(2752, 1L);
                st.takeItems(2754, 1L);
                st.takeItems(2753, 1L);
                st.giveItems(2740, 1L);
                st.setCond(9);
                htmltext = "tetrarch_thifiell_q0217_03.htm";
            } else if (cond == 7) {
                htmltext = "tetrarch_thifiell_q0217_04.htm";
            } else if (cond == 5) {
                htmltext = "tetrarch_thifiell_q0217_05.htm";
            }
        } else if (npcId == 30464) {
            if (cond == 5 && st.getQuestItemsCount(2748) > 0L) {
                htmltext = "magister_clayton_q0217_01.htm";
                st.takeItems(2748, 1L);
                st.giveItems(2755, 1L);
                st.setCond(6);
            } else if (cond == 6 && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) < 3L) {
                htmltext = "magister_clayton_q0217_02.htm";
            } else if (cond == 7 && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3L) {
                st.takeItems(2755, 1L);
                st.setCond(8);
                htmltext = "magister_clayton_q0217_03.htm";
            }
        } else if (npcId == 30657) {
            if ((cond == 10 || cond == 11) && st.getQuestItemsCount(2739) > 0L && st.getPlayer().getLevel() >= 38) {
                htmltext = "cardinal_seresin_q0217_01.htm";
            } else if ((cond == 10 || cond == 11) && st.getPlayer().getLevel() < 38) {
                htmltext = "cardinal_seresin_q0217_02.htm";
                if (cond == 10) {
                    st.setCond(11);
                }
            } else if (cond == 18) {
                htmltext = "cardinal_seresin_q0217_05.htm";
            }
        } else if (npcId == 30565) {
            if (cond == 12 && st.getQuestItemsCount(2738) > 0L) {
                htmltext = "kakai_the_lord_of_flame_q0217_01.htm";
            } else if (cond == 13) {
                htmltext = "kakai_the_lord_of_flame_q0217_03.htm";
            } else if (cond == 16) {
                htmltext = "kakai_the_lord_of_flame_q0217_04.htm";
                st.takeItems(2758, 1L);
                st.giveItems(2743, 1L);
                st.setCond(17);
            } else if (cond >= 17) {
                htmltext = "kakai_the_lord_of_flame_q0217_05.htm";
            }
        } else if (npcId == 30515) {
            if (cond == 13 && st.getQuestItemsCount(2757) > 0L) {
                htmltext = "seer_manakia_q0217_01.htm";
            } else if (cond == 14 && st.getQuestItemsCount(2756) < 10L) {
                htmltext = "seer_manakia_q0217_03.htm";
            } else if (cond == 15 && st.getQuestItemsCount(2756) == 10L) {
                htmltext = "seer_manakia_q0217_04.htm";
                st.takeItems(2756, -1L);
                st.giveItems(2758, 1L);
                st.setCond(16);
            } else if (cond == 16) {
                htmltext = "seer_manakia_q0217_05.htm";
            }
        } else if (npcId == 30531) {
            if (cond == 17 && st.getQuestItemsCount(2737) > 0L) {
                htmltext = "first_elder_lockirin_q0217_01.htm";
            } else if (cond == 18) {
                htmltext = "first_elder_lockirin_q0217_03.htm";
            } else if (cond == 21) {
                htmltext = "first_elder_lockirin_q0217_04.htm";
                st.giveItems(2742, 1L);
                st.setCond(22);
            } else if (cond == 22) {
                htmltext = "first_elder_lockirin_q0217_05.htm";
            }
        } else if (npcId == 30621) {
            if (cond == 18 && st.getQuestItemsCount(2759) > 0L) {
                htmltext = "maestro_nikola_q0217_01.htm";
            } else if (cond == 19 && st.getQuestItemsCount(2761) < 1L) {
                htmltext = "maestro_nikola_q0217_03.htm";
            } else if (cond == 20 && st.getQuestItemsCount(2761) >= 1L) {
                htmltext = "maestro_nikola_q0217_04.htm";
                st.takeItems(2761, -1L);
                st.takeItems(2760, 1L);
                st.setCond(21);
            } else if (cond == 21) {
                htmltext = "maestro_nikola_q0217_05.htm";
            }
        } else if (npcId == 30031 && cond == 23 && st.getQuestItemsCount(2744) > 0L) {
            htmltext = "quilt_q0217_01.htm";
            st.takeItems(2744, -1L);
            st.giveItems(2734, 1L);
            if (!st.getPlayer().getVarB("prof2.2")) {
                st.addExpAndSp(39571L, 2500L);
                st.getPlayer().setVar("prof2.2", "1", -1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20036 || npcId == 20044) {
            if (cond == 2 && st.getQuestItemsCount(2746) == 0L) {
                st.set("id", String.valueOf(st.getInt("id") + 1));
                if (Rnd.chance(st.getInt("id") * 33)) {
                    st.addSpawn(27120);
                    st.playSound("Itemsound.quest_before_battle");
                }
            }
        } else if (npcId == 20013 || npcId == 20019) {
            if (cond == 2 && st.getQuestItemsCount(2747) == 0L) {
                st.set("id", String.valueOf(st.getInt("id") + 1));
                if (Rnd.chance(st.getInt("id") * 33)) {
                    st.addSpawn(27121);
                    st.playSound("Itemsound.quest_before_battle");
                }
            }
        } else if (npcId == 27120) {
            if (cond == 2 && st.getQuestItemsCount(2746) == 0L) {
                if (st.getQuestItemsCount(2747) > 0L) {
                    st.giveItems(2746, 1L);
                    st.setCond(3);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2746, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 27121) {
            if (cond == 2 && st.getQuestItemsCount(2747) == 0L) {
                if (st.getQuestItemsCount(2746) > 0L) {
                    st.giveItems(2747, 1L);
                    st.setCond(3);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2747, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20550) {
            if (cond == 6 && st.getQuestItemsCount(2749) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2752) == 0L) {
                if (st.getQuestItemsCount(2749) == 9L) {
                    st.takeItems(2749, -1L);
                    st.giveItems(2752, 1L);
                    if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                        st.setCond(7);
                    }
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2749, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234) {
            if (cond == 6 && st.getQuestItemsCount(2751) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2754) == 0L) {
                if (st.getQuestItemsCount(2751) == 9L) {
                    st.takeItems(2751, -1L);
                    st.giveItems(2754, 1L);
                    if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                        st.setCond(7);
                    }
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2751, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20082 || npcId == 20086 || npcId == 20087 || npcId == 20084 || npcId == 20088) {
            if (cond == 6 && st.getQuestItemsCount(2750) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2753) == 0L) {
                if (st.getQuestItemsCount(2750) == 9L) {
                    st.takeItems(2750, -1L);
                    st.giveItems(2753, 1L);
                    if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                        st.setCond(7);
                    }
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2750, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20553) {
            if (cond == 14 && st.getQuestItemsCount(2756) < 10L && Rnd.chance(50)) {
                if (st.getQuestItemsCount(2756) == 9L) {
                    st.giveItems(2756, 1L);
                    st.setCond(15);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(2756, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20213 && cond == 19 && st.getQuestItemsCount(2761) < 1L) {
            st.giveItems(2761, 1L);
            st.setCond(20);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
