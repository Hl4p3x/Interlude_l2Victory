package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;
import java.util.stream.IntStream;

public class _420_LittleWings extends Quest {
    private static final int Cooper = 30829;
    private static final int Cronos = 30610;
    private static final int Byron = 30711;
    private static final int Maria = 30608;
    private static final int Mimyu = 30747;
    private static final int Exarion = 30748;
    private static final int Zwov = 30749;
    private static final int Kalibran = 30750;
    private static final int Suzet = 30751;
    private static final int Shamhai = 30752;
    private static final int Enchanted_Valey_First = 20589;
    private static final int Enchanted_Valey_Last = 20599;
    private static final int Toad_Lord = 20231;
    private static final int Marsh_Spider = 20233;
    private static final int Leto_Lizardman_Warrior = 20580;
    private static final int Road_Scavenger = 20551;
    private static final int Breka_Orc_Overlord = 20270;
    private static final int Dead_Seeker = 20202;
    private static final int Toad_Lord_Back_Skin_Chance = 30;
    private static final int Egg_Chance = 50;
    private static final int Pet_Armor_Chance = 35;
    private static final int Dragonflute_of_Wind = 3500;
    private static final int Dragonflute_of_Twilight = 3502;
    private static final int Hatchlings_Soft_Leather = 3912;
    private static final int Hatchlings_Mithril_Coat = 3918;
    private static final int Food_For_Hatchling = 4038;
    private static final int Fairy_Dust = 3499;
    private static final int Fairy_Stone = 3816;
    private static final int Deluxe_Fairy_Stone = 3817;
    private static final int Fairy_Stone_List = 3818;
    private static final int Deluxe_Fairy_Stone_List = 3819;
    private static final int Juice_of_Monkshood = 3821;
    private static int Coal = 1870;
    private static int Charcoal = 1871;
    private static int Silver_Nugget = 1873;
    private static int Stone_of_Purity = 1875;
    private static int GemstoneD = 2130;
    private static int GemstoneC = 2131;
    private static int Toad_Lord_Back_Skin = 3820;
    private static int Scale_of_Drake_Exarion = 3822;
    private static int Scale_of_Drake_Zwov = 3824;
    private static int Scale_of_Drake_Kalibran = 3826;
    private static int Scale_of_Wyvern_Suzet = 3828;
    private static int Scale_of_Wyvern_Shamhai = 3830;
    private static int Egg_of_Drake_Exarion = 3823;
    private static int Egg_of_Drake_Zwov = 3825;
    private static int Egg_of_Drake_Kalibran = 3827;
    private static int Egg_of_Wyvern_Suzet = 3829;
    private static int Egg_of_Wyvern_Shamhai = 3831;
    private static final int[][] wyrms = {{20580, 30748, Scale_of_Drake_Exarion, Egg_of_Drake_Exarion}, {20233, 30749, Scale_of_Drake_Zwov, Egg_of_Drake_Zwov}, {20551, 30750, Scale_of_Drake_Kalibran, Egg_of_Drake_Kalibran}, {20270, 30751, Scale_of_Wyvern_Suzet, Egg_of_Wyvern_Suzet}, {20202, 30752, Scale_of_Wyvern_Shamhai, Egg_of_Wyvern_Shamhai}};
    private static int[][] Fairy_Stone_Items = {{Coal, 10}, {Charcoal, 10}, {GemstoneD, 1}, {Silver_Nugget, 3}, {Toad_Lord_Back_Skin, 10}};
    private static int[][] Delux_Fairy_Stone_Items = {{Coal, 10}, {Charcoal, 10}, {GemstoneC, 1}, {Stone_of_Purity, 1}, {Silver_Nugget, 5}, {Toad_Lord_Back_Skin, 20}};

    public _420_LittleWings() {
        super(false);
        addStartNpc(30829);
        addTalkId(30610);
        addTalkId(30747);
        addTalkId(30711);
        addTalkId(30608);
        addKillId(20231);
        IntStream.rangeClosed(20589, 20599).forEach(this::addKillId);
        Arrays.stream(wyrms).forEach(wyrm -> {
            addTalkId(wyrm[1]);
            addKillId(wyrm[0]);
        });
        addQuestItem(Fairy_Dust);
        addQuestItem(Fairy_Stone);
        addQuestItem(Deluxe_Fairy_Stone);
        addQuestItem(Fairy_Stone_List);
        addQuestItem(Deluxe_Fairy_Stone_List);
        addQuestItem(Toad_Lord_Back_Skin);
        addQuestItem(Juice_of_Monkshood);
        addQuestItem(Scale_of_Drake_Exarion);
        addQuestItem(Scale_of_Drake_Zwov);
        addQuestItem(Scale_of_Drake_Kalibran);
        addQuestItem(Scale_of_Wyvern_Suzet);
        addQuestItem(Scale_of_Wyvern_Shamhai);
        addQuestItem(Egg_of_Drake_Exarion);
        addQuestItem(Egg_of_Drake_Zwov);
        addQuestItem(Egg_of_Drake_Kalibran);
        addQuestItem(Egg_of_Wyvern_Suzet);
        addQuestItem(Egg_of_Wyvern_Shamhai);
    }

    private static int getWyrmScale(final int npc_id) {
        return Arrays.stream(wyrms).filter(wyrm -> npc_id == wyrm[1]).findFirst().map(wyrm -> wyrm[2]).orElse(0);
    }

    private static int getWyrmEgg(final int npc_id) {
        return Arrays.stream(wyrms).filter(wyrm -> npc_id == wyrm[1]).findFirst().map(wyrm -> wyrm[3]).orElse(0);
    }

    private static int isWyrmStoler(final int npc_id) {
        return Arrays.stream(wyrms).filter(wyrm -> npc_id == wyrm[0]).findFirst().map(wyrm -> wyrm[1]).orElse(0);
    }

    public static int getNeededSkins(final QuestState st) {
        if (st.getQuestItemsCount(Deluxe_Fairy_Stone_List) > 0L) {
            return 20;
        }
        if (st.getQuestItemsCount(Fairy_Stone_List) > 0L) {
            return 10;
        }
        return -1;
    }

    public static boolean CheckFairyStoneItems(final QuestState st, final int[][] item_list) {
        return Arrays.stream(item_list).noneMatch(_item -> st.getQuestItemsCount(_item[0]) < _item[1]);
    }

    public static void TakeFairyStoneItems(final QuestState st, final int[][] item_list) {
        for (final int[] _item : item_list) st.takeItems(_item[0], (long) _item[1]);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("30829-02.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if (("30610-05.htm".equalsIgnoreCase(event) || "30610-12.htm".equalsIgnoreCase(event)) && _state == 2 && cond == 1) {
            st.setCond(2);
            st.takeItems(Fairy_Stone, -1L);
            st.takeItems(Deluxe_Fairy_Stone, -1L);
            st.takeItems(Fairy_Stone_List, -1L);
            st.takeItems(Deluxe_Fairy_Stone_List, -1L);
            st.giveItems(Fairy_Stone_List, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if (("30610-06.htm".equalsIgnoreCase(event) || "30610-13.htm".equalsIgnoreCase(event)) && _state == 2 && cond == 1) {
            st.setCond(2);
            st.takeItems(Fairy_Stone, -1L);
            st.takeItems(Deluxe_Fairy_Stone, -1L);
            st.takeItems(Fairy_Stone_List, -1L);
            st.takeItems(Deluxe_Fairy_Stone_List, -1L);
            st.giveItems(Deluxe_Fairy_Stone_List, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30608-03.htm".equalsIgnoreCase(event) && _state == 2 && cond == 2 && st.getQuestItemsCount(Fairy_Stone_List) > 0L) {
            if (!CheckFairyStoneItems(st, Fairy_Stone_Items)) {
                return "30608-01.htm";
            }
            st.setCond(3);
            TakeFairyStoneItems(st, Fairy_Stone_Items);
            st.giveItems(Fairy_Stone, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30608-03a.htm".equalsIgnoreCase(event) && _state == 2 && cond == 2 && st.getQuestItemsCount(Deluxe_Fairy_Stone_List) > 0L) {
            if (!CheckFairyStoneItems(st, Delux_Fairy_Stone_Items)) {
                return "30608-01a.htm";
            }
            st.setCond(3);
            TakeFairyStoneItems(st, Delux_Fairy_Stone_Items);
            st.giveItems(Deluxe_Fairy_Stone, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30711-03.htm".equalsIgnoreCase(event) && _state == 2 && cond == 3 && st.getQuestItemsCount(Fairy_Stone) + st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
            if (st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
                return (st.getInt("broken") == 1) ? "30711-04a.htm" : "30711-03a.htm";
            }
            if (st.getInt("broken") == 1) {
                return "30711-04.htm";
            }
        } else if ("30747-02.htm".equalsIgnoreCase(event) && _state == 2 && cond == 4 && st.getQuestItemsCount(Fairy_Stone) > 0L) {
            st.takeItems(Fairy_Stone, -1L);
            st.set("takedStone", "1");
        } else if ("30747-02a.htm".equalsIgnoreCase(event) && _state == 2 && cond == 4 && st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
            st.takeItems(Deluxe_Fairy_Stone, -1L);
            st.set("takedStone", "2");
            st.giveItems(Fairy_Dust, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30747-04.htm".equalsIgnoreCase(event) && _state == 2 && cond == 4 && st.getInt("takedStone") > 0) {
            st.setCond(5);
            st.unset("takedStone");
            st.giveItems(Juice_of_Monkshood, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30748-02.htm".equalsIgnoreCase(event) && cond == 5 && _state == 2 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
            st.setCond(6);
            st.takeItems(Juice_of_Monkshood, -1L);
            st.giveItems(3822, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30749-02.htm".equalsIgnoreCase(event) && cond == 5 && _state == 2 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
            st.setCond(6);
            st.takeItems(Juice_of_Monkshood, -1L);
            st.giveItems(3824, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30750-02.htm".equalsIgnoreCase(event) && cond == 5 && _state == 2 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
            st.setCond(6);
            st.takeItems(Juice_of_Monkshood, -1L);
            st.giveItems(3826, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30751-02.htm".equalsIgnoreCase(event) && cond == 5 && _state == 2 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
            st.setCond(6);
            st.takeItems(Juice_of_Monkshood, -1L);
            st.giveItems(3828, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30752-02.htm".equalsIgnoreCase(event) && cond == 5 && _state == 2 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
            st.setCond(6);
            st.takeItems(Juice_of_Monkshood, -1L);
            st.giveItems(3830, 1L);
            st.playSound("ItemSound.quest_itemget");
        } else if ("30747-09.htm".equalsIgnoreCase(event) && _state == 2 && cond == 7) {
            int egg_id = 0;
            for (final int[] wyrm : wyrms) {
                if (st.getQuestItemsCount(wyrm[2]) == 0L && st.getQuestItemsCount(wyrm[3]) >= 1L) {
                    egg_id = wyrm[3];
                    break;
                }
            }
            if (egg_id == 0) {
                return "noquest";
            }
            st.takeItems(egg_id, -1L);
            st.giveItems(Rnd.get(Dragonflute_of_Wind, Dragonflute_of_Twilight), 1L);
            if (st.getQuestItemsCount(Fairy_Dust) > 0L) {
                st.playSound("ItemSound.quest_middle");
                return "30747-09a.htm";
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("30747-10.htm".equalsIgnoreCase(event) && _state == 2 && cond == 7) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("30747-11.htm".equalsIgnoreCase(event) && _state == 2 && cond == 7) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
            if (st.getQuestItemsCount(Fairy_Dust) == 0L) {
                return "30747-10.htm";
            }
            st.takeItems(Fairy_Dust, -1L);
            if (Rnd.chance(35)) {
                int armor_id = Hatchlings_Soft_Leather + Rnd.get((int) st.getRateQuestsReward());
                if (armor_id > Hatchlings_Mithril_Coat) {
                    armor_id = Hatchlings_Mithril_Coat;
                }
                st.giveItems(armor_id, 1L);
            } else {
                st.giveItems(Food_For_Hatchling, 20L, true);
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != 30829) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 35) {
                st.exitCurrentQuest(true);
                return "30829-00.htm";
            }
            st.setCond(0);
            return "30829-01.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final int cond = st.getCond();
            final int broken = st.getInt("broken");
            if (npcId != 30829) {
                if (npcId == 30610) {
                    if (cond == 1) {
                        return (broken == 1) ? "30610-10.htm" : "30610-01.htm";
                    }
                    if (cond == 2) {
                        return "30610-07.htm";
                    }
                    if (cond == 3) {
                        return (broken == 1) ? "30610-14.htm" : "30610-08.htm";
                    }
                    if (cond == 4) {
                        return "30610-09.htm";
                    }
                    if (cond > 4) {
                        return "30610-11.htm";
                    }
                }
                if (npcId == 30608) {
                    if (cond == 2) {
                        if (st.getQuestItemsCount(Deluxe_Fairy_Stone_List) > 0L) {
                            return CheckFairyStoneItems(st, Delux_Fairy_Stone_Items) ? "30608-02a.htm" : "30608-01a.htm";
                        }
                        if (st.getQuestItemsCount(Fairy_Stone_List) > 0L) {
                            return CheckFairyStoneItems(st, Fairy_Stone_Items) ? "30608-02.htm" : "30608-01.htm";
                        }
                    } else if (cond > 2) {
                        return "30608-04.htm";
                    }
                }
                if (npcId == 30711) {
                    if (cond == 1 && broken == 1) {
                        return "30711-06.htm";
                    }
                    if (cond == 2 && broken == 1) {
                        return "30711-07.htm";
                    }
                    if (cond == 3 && st.getQuestItemsCount(Fairy_Stone) + st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
                        return "30711-01.htm";
                    }
                    if (cond >= 4 && st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
                        return "30711-05a.htm";
                    }
                    if (cond >= 4 && st.getQuestItemsCount(Fairy_Stone) > 0L) {
                        return "30711-05.htm";
                    }
                }
                if (npcId == 30747) {
                    if (cond == 4 && st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
                        return "30747-01a.htm";
                    }
                    if (cond == 4 && st.getQuestItemsCount(Fairy_Stone) > 0L) {
                        return "30747-01.htm";
                    }
                    if (cond == 5) {
                        return "30747-05.htm";
                    }
                    if (cond == 6) {
                        for (final int[] wyrm : wyrms) {
                            if (st.getQuestItemsCount(wyrm[2]) == 0L && st.getQuestItemsCount(wyrm[3]) >= 20L) {
                                return "30747-07.htm";
                            }
                        }
                        return "30747-06.htm";
                    }
                    if (cond == 7) {
                        for (final int[] wyrm : wyrms) {
                            if (st.getQuestItemsCount(wyrm[2]) == 0L && st.getQuestItemsCount(wyrm[3]) >= 1L) {
                                return "30747-08.htm";
                            }
                        }
                    }
                }
                if (npcId >= 30748 && npcId <= 30752) {
                    if (cond == 5 && st.getQuestItemsCount(Juice_of_Monkshood) > 0L) {
                        return String.valueOf(npcId) + "-01.htm";
                    }
                    if (cond == 6 && st.getQuestItemsCount(getWyrmScale(npcId)) > 0L) {
                        final int egg_id = getWyrmEgg(npcId);
                        if (st.getQuestItemsCount(egg_id) < 20L) {
                            return String.valueOf(npcId) + "-03.htm";
                        }
                        st.takeItems(getWyrmScale(npcId), -1L);
                        st.takeItems(egg_id, -1L);
                        st.giveItems(egg_id, 1L);
                        st.setCond(7);
                        return String.valueOf(npcId) + "-04.htm";
                    } else if (cond == 7 && st.getQuestItemsCount(getWyrmEgg(npcId)) == 1L) {
                        return String.valueOf(npcId) + "-05.htm";
                    }
                }
                return "noquest";
            }
            if (cond == 1) {
                return "30829-02.htm";
            }
            return "30829-03.htm";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 2 && npcId == 20231) {
            final int needed_skins = getNeededSkins(st);
            if (st.getQuestItemsCount(Toad_Lord_Back_Skin) < needed_skins && Rnd.chance(30)) {
                st.giveItems(Toad_Lord_Back_Skin, 1L);
                st.playSound((st.getQuestItemsCount(Toad_Lord_Back_Skin) < needed_skins) ? "ItemSound.quest_itemget" : "ItemSound.quest_middle");
            }
            return null;
        }
        if (npcId >= 20589 && npcId <= 20599 && st.getQuestItemsCount(Deluxe_Fairy_Stone) > 0L) {
            st.takeItems(Deluxe_Fairy_Stone, 1L);
            st.set("broken", "1");
            st.setCond(1);
            return "You lost fairy stone deluxe!";
        }
        if (cond == 6) {
            final int wyrm_id = isWyrmStoler(npcId);
            if (wyrm_id > 0 && st.getQuestItemsCount(getWyrmScale(wyrm_id)) > 0L && st.getQuestItemsCount(getWyrmEgg(wyrm_id)) < 20L && Rnd.chance(50)) {
                st.giveItems(getWyrmEgg(wyrm_id), 1L);
                st.playSound((st.getQuestItemsCount(getWyrmEgg(wyrm_id)) < 20L) ? "ItemSound.quest_itemget" : "ItemSound.quest_middle");
            }
        }
        return null;
    }

    
}
