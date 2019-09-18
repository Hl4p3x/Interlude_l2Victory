package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _330_AdeptOfTaste extends Quest {
    private static final int Sonia = 30062;
    private static final int Glyvka = 30067;
    private static final int Rollant = 30069;
    private static final int Jacob = 30073;
    private static final int Pano = 30078;
    private static final int Mirien = 30461;
    private static final int Jonas = 30469;
    private static final int Hobgoblin = 20147;
    private static final int Mandragora_Sprout = 20154;
    private static final int Mandragora_Sapling = 20155;
    private static final int Mandragora_Blossom = 20156;
    private static final int Bloody_Bee = 20204;
    private static final int Mandragora_Sprout2 = 20223;
    private static final int Gray_Ant = 20226;
    private static final int Giant_Crimson_Ant = 20228;
    private static final int Stinger_Wasp = 20229;
    private static final int Monster_Eye_Searcher = 20265;
    private static final int Monster_Eye_Gazer = 20266;
    private static final int Ingredient_List = 1420;
    private static final int Sonias_Botany_Book = 1421;
    private static final int Red_Mandragora_Root = 1422;
    private static final int White_Mandragora_Root = 1423;
    private static final int Red_Mandragora_Sap = 1424;
    private static final int White_Mandragora_Sap = 1425;
    private static final int Jacobs_Insect_Book = 1426;
    private static final int Nectar = 1427;
    private static final int Royal_Jelly = 1428;
    private static final int Honey = 1429;
    private static final int Golden_Honey = 1430;
    private static final int Panos_Contract = 1431;
    private static final int Hobgoblin_Amulet = 1432;
    private static final int Dionian_Potato = 1433;
    private static final int Glyvkas_Botany_Book = 1434;
    private static final int Green_Marsh_Moss = 1435;
    private static final int Brown_Marsh_Moss = 1436;
    private static final int Green_Moss_Bundle = 1437;
    private static final int Brown_Moss_Bundle = 1438;
    private static final int Rollants_Creature_Book = 1439;
    private static final int Body_of_Monster_Eye = 1440;
    private static final int Meat_of_Monster_Eye = 1441;
    private static final int[] Jonass_Steak_Dishes = {1442, 1443, 1444, 1445, 1446};
    private static final int[] Miriens_Reviews = {1447, 1448, 1449, 1450, 1451};
    private static final int[] ingredients = {1424, 1429, 1433, 1437, 1441};
    private static final int[] spec_ingredients = {1425, 1430, 1438};
    private static final int[] rewards = {0, 0, 1455, 1456, 1457};
    private static final int[] adena_rewards = {10000, 14870, 6490, 12220, 16540};

    public _330_AdeptOfTaste() {
        super(false);
        addStartNpc(30469);
        addTalkId(30062);
        addTalkId(30067);
        addTalkId(30069);
        addTalkId(30073);
        addTalkId(30078);
        addTalkId(30461);
        addKillId(20147);
        addKillId(20154);
        addKillId(20155);
        addKillId(20156);
        addKillId(20204);
        addKillId(20223);
        addKillId(20226);
        addKillId(20228);
        addKillId(20229);
        addKillId(20265);
        addKillId(20266);
        addQuestItem(1420);
        addQuestItem(1421);
        addQuestItem(1422);
        addQuestItem(1423);
        addQuestItem(1426);
        addQuestItem(1427);
        addQuestItem(1428);
        addQuestItem(1431);
        addQuestItem(1432);
        addQuestItem(1434);
        addQuestItem(1435);
        addQuestItem(1436);
        addQuestItem(1439);
        addQuestItem(1440);
        addQuestItem(_330_AdeptOfTaste.ingredients);
        addQuestItem(_330_AdeptOfTaste.spec_ingredients);
        addQuestItem(_330_AdeptOfTaste.Jonass_Steak_Dishes);
        addQuestItem(_330_AdeptOfTaste.Miriens_Reviews);
    }

    private static void MandragoraDrop(final QuestState st, final int i1, final int i2) {
        final int j = Rnd.get(100);
        if (j < i1) {
            st.rollAndGive(1422, 1, 1, 40, 100.0);
        } else if (j < i2) {
            st.rollAndGive(1423, 1, 1, 40, 100.0);
        }
    }

    private static void BeeDrop(final QuestState st, final int i1, final int i2) {
        final int j = Rnd.get(100);
        if (j < i1) {
            st.rollAndGive(1427, 1, 1, 20, 100.0);
        } else if (j < i2) {
            st.rollAndGive(1428, 1, 1, 10, 100.0);
        }
    }

    private static void AntDrop(final QuestState st, final int i1, final int i2) {
        final int j = Rnd.get(100);
        if (j < i1) {
            st.rollAndGive(1435, 1, 1, 20, 100.0);
        } else if (j < i2) {
            st.rollAndGive(1436, 1, 1, 20, 100.0);
        }
    }

    private static void Root2Sap(final QuestState st, final int sap_id) {
        st.takeItems(1421, -1L);
        st.takeItems(1423, -1L);
        st.takeItems(1422, -1L);
        st.playSound("ItemSound.quest_middle");
        st.giveItems(sap_id, 1L);
    }

    private static void Moss2Bundle(final QuestState st, final int bundle_id) {
        st.takeItems(1434, -1L);
        st.takeItems(1436, -1L);
        st.takeItems(1435, -1L);
        st.playSound("ItemSound.quest_middle");
        st.giveItems(bundle_id, 1L);
    }

    private static void Nectar2Honey(final QuestState st, final int honey_id) {
        st.takeItems(1426, -1L);
        st.takeItems(1427, -1L);
        st.takeItems(1428, -1L);
        st.playSound("ItemSound.quest_middle");
        st.giveItems(honey_id, 1L);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("30469_03.htm".equalsIgnoreCase(event) && _state == 1) {
            if (st.getQuestItemsCount(1420) == 0L) {
                st.giveItems(1420, 1L);
            }
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30062_05.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(1423) + st.getQuestItemsCount(1422) < 40L) {
                return null;
            }
            Root2Sap(st, 1424);
        } else if ("30067_05.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(1436) + st.getQuestItemsCount(1435) < 20L) {
                return null;
            }
            Moss2Bundle(st, 1437);
        } else if ("30073_05.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(1427) < 20L) {
                return null;
            }
            Nectar2Honey(st, 1429);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != 30469) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 24) {
                st.exitCurrentQuest(true);
                return "30469_01.htm";
            }
            st.setCond(0);
            return "30469_02.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final long ingredients_count = st.getQuestItemsCount(_330_AdeptOfTaste.ingredients);
            long spec_ingredients_count = st.getQuestItemsCount(_330_AdeptOfTaste.spec_ingredients);
            final long all_ingredients_count = ingredients_count + spec_ingredients_count;
            final boolean Has_Ingredient_List = st.getQuestItemsCount(1420) > 0L;
            if (npcId == 30469) {
                if (Has_Ingredient_List) {
                    if (all_ingredients_count < 5L) {
                        return "30469_04.htm";
                    }
                    st.takeAllItems(1420);
                    st.takeAllItems(_330_AdeptOfTaste.ingredients);
                    st.takeAllItems(_330_AdeptOfTaste.spec_ingredients);
                    if (spec_ingredients_count > 3L) {
                        spec_ingredients_count = 3L;
                    }
                    spec_ingredients_count += Rnd.get(0, 1);
                    st.playSound((spec_ingredients_count == 4L) ? "ItemSound.quest_jackpot" : "ItemSound.quest_middle");
                    st.giveItems(_330_AdeptOfTaste.Jonass_Steak_Dishes[(int) spec_ingredients_count], 1L);
                    ++spec_ingredients_count;
                    return "30469_05t" + spec_ingredients_count + ".htm";
                } else if (all_ingredients_count == 0L) {
                    final long Jonass_Steak_Dish_count = st.getQuestItemsCount(_330_AdeptOfTaste.Jonass_Steak_Dishes);
                    final long Miriens_Review_count = st.getQuestItemsCount(_330_AdeptOfTaste.Miriens_Reviews);
                    if (Jonass_Steak_Dish_count > 0L && Miriens_Review_count == 0L) {
                        return "30469_06.htm";
                    }
                    if (Jonass_Steak_Dish_count == 0L && Miriens_Review_count > 0L) {
                        for (int i = _330_AdeptOfTaste.Miriens_Reviews.length; i > 0; --i) {
                            if (st.getQuestItemsCount(_330_AdeptOfTaste.Miriens_Reviews[i - 1]) > 0L) {
                                st.takeAllItems(_330_AdeptOfTaste.Miriens_Reviews);
                                if (_330_AdeptOfTaste.adena_rewards[i - 1] > 0) {
                                    st.giveItems(57, (long) _330_AdeptOfTaste.adena_rewards[i - 1]);
                                }
                                if (_330_AdeptOfTaste.rewards[i - 1] > 0) {
                                    st.giveItems(_330_AdeptOfTaste.rewards[i - 1], 1L);
                                }
                                st.playSound("ItemSound.quest_finish");
                                st.exitCurrentQuest(true);
                                return "30469_06t" + i + ".htm";
                            }
                        }
                    }
                }
            }
            if (npcId == 30461) {
                if (Has_Ingredient_List) {
                    return "30461_01.htm";
                }
                if (all_ingredients_count == 0L) {
                    if (st.getQuestItemsCount(_330_AdeptOfTaste.Miriens_Reviews) > 0L) {
                        return "30461_04.htm";
                    }
                    for (int j = _330_AdeptOfTaste.Jonass_Steak_Dishes.length; j > 0; --j) {
                        if (st.getQuestItemsCount(_330_AdeptOfTaste.Jonass_Steak_Dishes[j - 1]) > 0L) {
                            st.takeAllItems(_330_AdeptOfTaste.Jonass_Steak_Dishes);
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(_330_AdeptOfTaste.Miriens_Reviews[j - 1], 1L);
                            return "30461_02t" + j + ".htm";
                        }
                    }
                }
            }
            if (!Has_Ingredient_List || all_ingredients_count >= 5L) {
                return "noquest";
            }
            if (npcId == 30062) {
                final boolean has_sap = st.getQuestItemsCount(1424) > 0L || st.getQuestItemsCount(1425) > 0L;
                if (st.getQuestItemsCount(1421) > 0L) {
                    if (!has_sap) {
                        long Root_count = st.getQuestItemsCount(1423);
                        if (Root_count >= 40L) {
                            Root2Sap(st, 1425);
                            return "30062_06.htm";
                        }
                        Root_count += st.getQuestItemsCount(1422);
                        return (Root_count < 40L) ? "30062_02.htm" : "30062_03.htm";
                    }
                } else {
                    if (has_sap) {
                        return "30062_07.htm";
                    }
                    st.giveItems(1421, 1L);
                    return "30062_01.htm";
                }
            }
            if (npcId == 30067) {
                final boolean has_bundle = st.getQuestItemsCount(1437) > 0L || st.getQuestItemsCount(1438) > 0L;
                if (st.getQuestItemsCount(1434) > 0L) {
                    if (!has_bundle) {
                        long moss_count = st.getQuestItemsCount(1436);
                        if (moss_count >= 20L) {
                            Moss2Bundle(st, 1438);
                            return "30067_06.htm";
                        }
                        moss_count += st.getQuestItemsCount(1435);
                        return (moss_count < 20L) ? "30067_02.htm" : "30067_03.htm";
                    }
                } else if (has_bundle) {
                    return "30067_07.htm";
                }
                st.giveItems(1434, 1L);
                return "30067_01.htm";
            }
            if (npcId == 30069) {
                final boolean has_meat = st.getQuestItemsCount(1441) > 0L;
                if (st.getQuestItemsCount(1439) > 0L) {
                    if (!has_meat) {
                        if (st.getQuestItemsCount(1440) < 30L) {
                            return "30069_02.htm";
                        }
                        st.takeItems(1439, -1L);
                        st.takeItems(1440, -1L);
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(1441, 1L);
                        return "30069_03.htm";
                    }
                } else {
                    if (has_meat) {
                        return "30069_04.htm";
                    }
                    st.giveItems(1439, 1L);
                    return "30069_01.htm";
                }
            }
            if (npcId == 30073) {
                final boolean has_honey = st.getQuestItemsCount(1429) > 0L || st.getQuestItemsCount(1430) > 0L;
                if (st.getQuestItemsCount(1426) > 0L) {
                    if (!has_honey) {
                        if (st.getQuestItemsCount(1427) < 20L) {
                            return "30073_02.htm";
                        }
                        if (st.getQuestItemsCount(1428) < 10L) {
                            return "30073_03.htm";
                        }
                        Nectar2Honey(st, 1430);
                        return "30073_06.htm";
                    }
                } else {
                    if (has_honey) {
                        return "30073_07.htm";
                    }
                    st.giveItems(1426, 1L);
                    return "30073_01.htm";
                }
            }
            if (npcId == 30078) {
                final boolean has_potato = st.getQuestItemsCount(1433) > 0L;
                if (st.getQuestItemsCount(1431) > 0L) {
                    if (!has_potato) {
                        if (st.getQuestItemsCount(1432) < 30L) {
                            return "30078_02.htm";
                        }
                        st.takeItems(1431, -1L);
                        st.takeItems(1432, -1L);
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(1433, 1L);
                        return "30078_03.htm";
                    }
                } else {
                    if (has_potato) {
                        return "30078_04.htm";
                    }
                    st.giveItems(1431, 1L);
                    return "30078_01.htm";
                }
            }
            return "noquest";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        final long ingredients_count = st.getQuestItemsCount(_330_AdeptOfTaste.ingredients);
        final long spec_ingredients_count = st.getQuestItemsCount(_330_AdeptOfTaste.spec_ingredients);
        final long all_ingredients_count = ingredients_count + spec_ingredients_count;
        final boolean Has_Ingredient_List = st.getQuestItemsCount(1420) > 0L;
        if (!Has_Ingredient_List || all_ingredients_count >= 5L) {
            return null;
        }
        if (npcId == 20147 && st.getQuestItemsCount(1431) > 0L) {
            st.rollAndGive(1432, 1, 1, 30, 100.0);
        } else if (npcId == 20154 && st.getQuestItemsCount(1421) > 0L) {
            MandragoraDrop(st, 70, 77);
        } else if (npcId == 20155 && st.getQuestItemsCount(1421) > 0L) {
            MandragoraDrop(st, 77, 85);
        } else if (npcId == 20156 && st.getQuestItemsCount(1421) > 0L) {
            MandragoraDrop(st, 87, 96);
        } else if (npcId == 20223 && st.getQuestItemsCount(1421) > 0L) {
            MandragoraDrop(st, 70, 77);
        } else if (npcId == 20204 && st.getQuestItemsCount(1426) > 0L) {
            BeeDrop(st, 80, 95);
        } else if (npcId == 20229 && st.getQuestItemsCount(1426) > 0L) {
            BeeDrop(st, 92, 100);
        } else if (npcId == 20226 && st.getQuestItemsCount(1434) > 0L) {
            AntDrop(st, 87, 96);
        } else if (npcId == 20228 && st.getQuestItemsCount(1434) > 0L) {
            AntDrop(st, 90, 100);
        } else if (npcId == 20265 && st.getQuestItemsCount(1439) > 0L) {
            st.rollAndGive(1440, 1, 3, 30, 97.0);
        } else if (npcId == 20266 && st.getQuestItemsCount(1439) > 0L) {
            st.rollAndGive(1440, 1, 2, 30, 100.0);
        }
        return null;
    }

    
}
