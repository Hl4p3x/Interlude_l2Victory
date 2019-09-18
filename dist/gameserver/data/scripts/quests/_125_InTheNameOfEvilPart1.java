package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

public class _125_InTheNameOfEvilPart1 extends Quest {
    private static final int mushika = 32114;
    private static final int shaman_caracawe = 32117;
    private static final int ulu_kaimu_stone = 32119;
    private static final int balu_kaimu_stone = 32120;
    private static final int jiuta_kaimu_stone = 32121;
    private static final int ornithomimus_leader = 22200;
    private static final int ornithomimus = 22201;
    private static final int ornithomimus_s = 22202;
    private static final int deinonychus_leader = 22203;
    private static final int deinonychus = 22204;
    private static final int deinonychus_s = 22205;
    private static final int ornithomimus_n = 22219;
    private static final int deinonychus_n = 22220;
    private static final int ornithomimus_leader2 = 22224;
    private static final int deinonychus_leader2 = 22225;
    private static final int q_claw_of_ornithomimus = 8779;
    private static final int q_bone_of_deinonychus = 8780;
    private static final int q_muzzle_pattem = 8781;
    private static final int q_piece_of_gazk = 8782;

    public _125_InTheNameOfEvilPart1() {
        super(false);
        addStartNpc(32114);
        addTalkId(32117, 32119, 32120, 32121);
        addKillId(22200, 22201, 22202, 22203, 22204, 22205, 22219, 22220, 22224, 22225);
        addQuestItem(8779, 8780);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int GetMemoState = st.getInt("name_of_cruel_god_one");
        final int GetMemoStateEx = st.getInt("name_of_cruel_god_one_ex");
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "mushika_q0125_08.htm";
        } else if ("reply_5".equalsIgnoreCase(event)) {
            st.set("name_of_cruel_god_one", String.valueOf(1), true);
            htmltext = "mushika_q0125_11.htm";
        } else if ("reply_6".equalsIgnoreCase(event) && GetMemoState == 1) {
            st.setCond(2);
            st.set("name_of_cruel_god_one", String.valueOf(2), true);
            st.giveItems(8782, 1L);
            st.playSound("ItemSound.quest_middle");
            htmltext = "mushika_q0125_12.htm";
        } else if ("reply_13".equalsIgnoreCase(event) && GetMemoState == 2) {
            st.setCond(3);
            st.set("name_of_cruel_god_one", String.valueOf(3), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "shaman_caracawe_q0125_09.htm";
        } else if ("reply_17".equalsIgnoreCase(event) && GetMemoState == 4) {
            st.setCond(5);
            st.set("name_of_cruel_god_one", String.valueOf(5), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "shaman_caracawe_q0125_17.htm";
        } else if ("reply_19".equalsIgnoreCase(event) && GetMemoState == 5) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
            htmltext = "ulu_kaimu_stone_q0125_04.htm";
        } else if ("reply_1".equalsIgnoreCase(event) && GetMemoState == 5) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
            htmltext = "ulu_kaimu_stone_q0125_05.htm";
        } else if ("reply_2".equalsIgnoreCase(event) && GetMemoState == 5) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
            htmltext = "ulu_kaimu_stone_q0125_06.htm";
        } else if ("reply_3".equalsIgnoreCase(event) && GetMemoState == 5) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
            htmltext = "ulu_kaimu_stone_q0125_07.htm";
        } else if ("reply_4".equalsIgnoreCase(event) && GetMemoState == 5) {
            if (GetMemoStateEx != 111) {
                htmltext = "ulu_kaimu_stone_q0125_08.htm";
            } else {
                st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
                st.set("name_of_cruel_god_one", String.valueOf(6), true);
                htmltext = "ulu_kaimu_stone_q0125_09.htm";
            }
        } else if ("reply_26".equalsIgnoreCase(event) && GetMemoState == 6) {
            st.setCond(6);
            st.set("name_of_cruel_god_one", String.valueOf(7), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "ulu_kaimu_stone_q0125_20.htm";
        } else if ("reply_27".equalsIgnoreCase(event) && GetMemoState == 7) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
            htmltext = "balu_kaimu_stone_q0125_04.htm";
        } else if ("reply_1a".equalsIgnoreCase(event) && GetMemoState == 7) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
            htmltext = "balu_kaimu_stone_q0125_05.htm";
        } else if ("reply_2a".equalsIgnoreCase(event) && GetMemoState == 7) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
            htmltext = "balu_kaimu_stone_q0125_06.htm";
        } else if ("reply_3a".equalsIgnoreCase(event) && GetMemoState == 7) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
            htmltext = "balu_kaimu_stone_q0125_07.htm";
        } else if ("reply_4a".equalsIgnoreCase(event) && GetMemoState == 7) {
            if (GetMemoStateEx != 111) {
                htmltext = "balu_kaimu_stone_q0125_08.htm";
            } else {
                st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
                st.set("name_of_cruel_god_one", String.valueOf(8), true);
                htmltext = "balu_kaimu_stone_q0125_09.htm";
            }
        } else if ("reply_34".equalsIgnoreCase(event) && GetMemoState == 8) {
            st.setCond(7);
            st.set("name_of_cruel_god_one", String.valueOf(9), true);
            st.playSound("ItemSound.quest_middle");
            htmltext = "balu_kaimu_stone_q0125_19.htm";
        } else if ("reply_35".equalsIgnoreCase(event) && GetMemoState == 9) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
            htmltext = "jiuta_kaimu_stone_q0125_04.htm";
        } else if ("reply_1b".equalsIgnoreCase(event) && GetMemoState == 9) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
            htmltext = "jiuta_kaimu_stone_q0125_05.htm";
        } else if ("reply_2b".equalsIgnoreCase(event) && GetMemoState == 9) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
            htmltext = "jiuta_kaimu_stone_q0125_06.htm";
        } else if ("reply_3b".equalsIgnoreCase(event) && GetMemoState == 9) {
            st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
            htmltext = "jiuta_kaimu_stone_q0125_07.htm";
        } else if ("reply_4b".equalsIgnoreCase(event) && GetMemoState == 9) {
            if (GetMemoStateEx != 111) {
                htmltext = "jiuta_kaimu_stone_q0125_08.htm";
            } else {
                st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
                st.set("name_of_cruel_god_one", String.valueOf(10), true);
                htmltext = "jiuta_kaimu_stone_q0125_09.htm";
            }
        } else if ("reply_38".equalsIgnoreCase(event) && GetMemoState == 10) {
            st.set("name_of_cruel_god_one", String.valueOf(11), true);
            htmltext = "jiuta_kaimu_stone_q0125_13.htm";
        } else if ("reply_41".equalsIgnoreCase(event) && GetMemoState == 11 && st.getQuestItemsCount(8782) >= 1L) {
            st.set("name_of_cruel_god_one", String.valueOf(12), true);
            htmltext = "jiuta_kaimu_stone_q0125_19.htm";
        } else if ("reply_42".equalsIgnoreCase(event)) {
            st.set("name_of_cruel_god_one", String.valueOf(13), true);
            htmltext = "jiuta_kaimu_stone_q0125_21.htm";
        } else if ("reply_43".equalsIgnoreCase(event) && GetMemoState == 13 && st.getQuestItemsCount(8782) >= 1L) {
            st.setCond(8);
            st.set("name_of_cruel_god_one", String.valueOf(14), true);
            st.giveItems(8781, 1L);
            st.takeItems(8782, -1L);
            st.playSound("ItemSound.quest_middle");
            htmltext = "jiuta_kaimu_stone_q0125_23.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final QuestState qs = st.getPlayer().getQuestState(_124_MeetingTheElroki.class);
        final int GetMemoState = st.getInt("name_of_cruel_god_one");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 32114) {
                    break;
                }
                if (qs == null || !qs.isCompleted()) {
                    htmltext = "mushika_q0125_04.htm";
                    st.exitCurrentQuest(true);
                    break;
                }
                if (st.getPlayer().getLevel() >= 76) {
                    htmltext = "mushika_q0125_01.htm";
                    break;
                }
                htmltext = "mushika_q0125_02.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                switch (npcId) {
                    case 32114:
                        if (GetMemoState < 1) {
                            htmltext = "mushika_q0125_09.htm";
                            break;
                        }
                        if (GetMemoState == 1) {
                            htmltext = "mushika_q0125_11a.htm";
                            break;
                        }
                        if (GetMemoState == 2) {
                            htmltext = "mushika_q0125_13.htm";
                            break;
                        }
                        if (GetMemoState >= 3 && GetMemoState <= 13) {
                            htmltext = "mushika_q0125_14.htm";
                            break;
                        }
                        if (GetMemoState == 14 && st.getQuestItemsCount(8781) >= 1L) {
                            st.takeItems(8781, -1L);
                            st.unset("name_of_cruel_god_one");
                            st.unset("name_of_cruel_god_one_ex");
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(false);
                            htmltext = "mushika_q0125_15.htm";
                            break;
                        }
                        break;
                    case 32117:
                        if (GetMemoState == 2) {
                            htmltext = "shaman_caracawe_q0125_01.htm";
                            break;
                        }
                        if (GetMemoState < 2) {
                            htmltext = "shaman_caracawe_q0125_02.htm";
                            break;
                        }
                        if (GetMemoState == 3 && st.getQuestItemsCount(8779) >= 2L && st.getQuestItemsCount(8780) >= 2L) {
                            st.takeItems(8779, -1L);
                            st.takeItems(8780, -1L);
                            st.set("name_of_cruel_god_one", String.valueOf(4), true);
                            htmltext = "shaman_caracawe_q0125_11.htm";
                            break;
                        }
                        if (GetMemoState == 3 && (st.getQuestItemsCount(8779) < 2L || st.getQuestItemsCount(8780) < 2L)) {
                            htmltext = "shaman_caracawe_q0125_12.htm";
                            break;
                        }
                        if (GetMemoState == 4) {
                            htmltext = "shaman_caracawe_q0125_14.htm";
                            break;
                        }
                        if (GetMemoState == 5) {
                            htmltext = "shaman_caracawe_q0125_18.htm";
                            break;
                        }
                        if (GetMemoState >= 6 && GetMemoState < 13) {
                            htmltext = "shaman_caracawe_q0125_19.htm";
                            break;
                        }
                        if (GetMemoState == 14 && st.getQuestItemsCount(8781) >= 1L) {
                            htmltext = "shaman_caracawe_q0125_20.htm";
                            break;
                        }
                        break;
                    case 32119:
                        if (GetMemoState == 5) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            htmltext = "ulu_kaimu_stone_q0125_01.htm";
                            break;
                        }
                        if (GetMemoState < 5) {
                            htmltext = "ulu_kaimu_stone_q0125_02.htm";
                            break;
                        }
                        if (GetMemoState > 7) {
                            htmltext = "ulu_kaimu_stone_q0125_03.htm";
                            break;
                        }
                        if (GetMemoState == 6) {
                            htmltext = "ulu_kaimu_stone_q0125_11.htm";
                            break;
                        }
                        if (GetMemoState == 7) {
                            htmltext = "ulu_kaimu_stone_q0125_21.htm";
                            break;
                        }
                        break;
                    case 32120:
                        if (GetMemoState == 7) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            htmltext = "balu_kaimu_stone_q0125_01.htm";
                            break;
                        }
                        if (GetMemoState < 7) {
                            htmltext = "balu_kaimu_stone_q0125_02.htm";
                            break;
                        }
                        if (GetMemoState > 9) {
                            htmltext = "balu_kaimu_stone_q0125_03.htm";
                            break;
                        }
                        if (GetMemoState == 8) {
                            htmltext = "balu_kaimu_stone_q0125_11.htm";
                            break;
                        }
                        if (GetMemoState == 9) {
                            htmltext = "balu_kaimu_stone_q0125_20.htm";
                            break;
                        }
                        break;
                    default:
                        if (npcId != 32121) {
                            break;
                        }
                        if (GetMemoState == 9) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
                            htmltext = "jiuta_kaimu_stone_q0125_01.htm";
                            break;
                        }
                        if (GetMemoState < 9) {
                            htmltext = "jiuta_kaimu_stone_q0125_02.htm";
                            break;
                        }
                        if (GetMemoState > 14) {
                            htmltext = "jiuta_kaimu_stone_q0125_03.htm";
                            break;
                        }
                        if (GetMemoState == 10) {
                            htmltext = "jiuta_kaimu_stone_q0125_11.htm";
                            break;
                        }
                        if (GetMemoState == 11) {
                            htmltext = "jiuta_kaimu_stone_q0125_14.htm";
                            break;
                        }
                        if (GetMemoState == 12 && st.getQuestItemsCount(8782) >= 1L) {
                            htmltext = "jiuta_kaimu_stone_q0125_20.htm";
                            break;
                        }
                        if (GetMemoState == 13 && st.getQuestItemsCount(8782) >= 1L) {
                            htmltext = "jiuta_kaimu_stone_q0125_22.htm";
                            break;
                        }
                        if (GetMemoState == 14 && st.getQuestItemsCount(8781) >= 1L) {
                            htmltext = "jiuta_kaimu_stone_q0125_24.htm";
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
        final int GetMemoState = st.getInt("name_of_cruel_god_one");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 3) {
            switch (npcId) {
                case 22200:
                case 22202: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 661 && st.getQuestItemsCount(8779) <= 1L) {
                        if (st.getQuestItemsCount(8779) < 1L) {
                            st.giveItems(8779, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8779) >= 1L) {
                            st.giveItems(8779, 1L);
                            if (st.getQuestItemsCount(8780) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
                case 22201: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 330 && st.getQuestItemsCount(8779) <= 1L) {
                        if (st.getQuestItemsCount(8779) < 1L) {
                            st.giveItems(8779, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8779) >= 1L) {
                            st.giveItems(8779, 1L);
                            if (st.getQuestItemsCount(8780) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
                case 22203:
                case 22205: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 651 && st.getQuestItemsCount(8780) <= 1L) {
                        if (st.getQuestItemsCount(8780) < 1L) {
                            st.giveItems(8780, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8780) >= 1L) {
                            st.giveItems(8780, 1L);
                            if (st.getQuestItemsCount(8779) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
                case 22204: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 326 && st.getQuestItemsCount(8780) <= 1L) {
                        if (st.getQuestItemsCount(8780) < 1L) {
                            st.giveItems(8780, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8780) >= 1L) {
                            st.giveItems(8780, 1L);
                            if (st.getQuestItemsCount(8779) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
                case 22219:
                case 22224: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 327 && st.getQuestItemsCount(8779) <= 1L) {
                        if (st.getQuestItemsCount(8779) < 1L) {
                            st.giveItems(8779, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8779) >= 1L) {
                            st.giveItems(8779, 1L);
                            if (st.getQuestItemsCount(8780) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
                case 22220:
                case 22225: {
                    final int i4 = Rnd.get(1000);
                    if (i4 < 319 && st.getQuestItemsCount(8780) <= 1L) {
                        if (st.getQuestItemsCount(8780) < 1L) {
                            st.giveItems(8780, 1L);
                            st.playSound("ItemSound.quest_itemget");
                        } else if (st.getQuestItemsCount(8780) >= 1L) {
                            st.giveItems(8780, 1L);
                            if (st.getQuestItemsCount(8779) >= 2L) {
                                st.setCond(4);
                                st.playSound("ItemSound.quest_middle");
                            }
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
}
