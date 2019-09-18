package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;

public class _333_BlackLionHunt extends Quest {
    private final int BLACK_LION_MARK = 1369;
    private final int CARGO_BOX1 = 3440;
    private final int UNDEAD_ASH = 3848;
    private final int BLOODY_AXE_INSIGNIAS = 3849;
    private final int DELU_FANG = 3850;
    private final int STAKATO_TALONS = 3851;
    private final int SOPHIAS_LETTER1 = 3671;
    private final int SOPHIAS_LETTER2 = 3672;
    private final int SOPHIAS_LETTER3 = 3673;
    private final int SOPHIAS_LETTER4 = 3674;
    private final int LIONS_CLAW = 3675;
    private final int LIONS_EYE = 3676;
    private final int GUILD_COIN = 3677;
    private final int COMPLETE_STATUE = 3461;
    private final int COMPLETE_TABLET = 3466;
    private final int ALACRITY_POTION = 735;
    private final int SCROLL_ESCAPE = 736;
    private final int SOULSHOT_D = 1463;
    private final int SPIRITSHOT_D = 2510;
    private final int HEALING_POTION = 1061;
    private final int OPEN_BOX_PRICE = 650;
    private final int GLUDIO_APPLE = 3444;
    private final int CORN_MEAL = 3445;
    private final int WOLF_PELTS = 3446;
    private final int MONNSTONE = 3447;
    private final int GLUDIO_WEETS_FLOWER = 3448;
    private final int SPIDERSILK_ROPE = 3449;
    private final int ALEXANDRIT = 3450;
    private final int SILVER_TEA = 3451;
    private final int GOLEM_PART = 3452;
    private final int FIRE_EMERALD = 3453;
    private final int SILK_FROCK = 3454;
    private final int PORCELAN_URN = 3455;
    private final int IMPERIAL_DIAMOND = 3456;
    private final int STATUE_SHILIEN_HEAD = 3457;
    private final int STATUE_SHILIEN_TORSO = 3458;
    private final int STATUE_SHILIEN_ARM = 3459;
    private final int STATUE_SHILIEN_LEG = 3460;
    private final int FRAGMENT_ANCIENT_TABLE1 = 3462;
    private final int FRAGMENT_ANCIENT_TABLE2 = 3463;
    private final int FRAGMENT_ANCIENT_TABLE3 = 3464;
    private final int FRAGMENT_ANCIENT_TABLE4 = 3465;
    private final int Sophya = 30735;
    private final int Redfoot = 30736;
    private final int Rupio = 30471;
    private final int Undrias = 30130;
    private final int Lockirin = 30531;
    private final int Morgan = 30737;
    int[][] DROPLIST = {{20160, 1, 1, 67, 29, UNDEAD_ASH}, {20171, 1, 1, 76, 31, UNDEAD_ASH}, {20197, 1, 1, 89, 25, UNDEAD_ASH}, {20200, 1, 1, 60, 28, UNDEAD_ASH}, {20201, 1, 1, 70, 29, UNDEAD_ASH}, {20202, 1, 0, 60, 24, UNDEAD_ASH}, {20198, 1, 1, 60, 35, UNDEAD_ASH}, {20207, 2, 1, 69, 29, BLOODY_AXE_INSIGNIAS}, {20208, 2, 1, 67, 32, BLOODY_AXE_INSIGNIAS}, {20209, 2, 1, 62, 33, BLOODY_AXE_INSIGNIAS}, {20210, 2, 1, 78, 23, BLOODY_AXE_INSIGNIAS}, {20211, 2, 1, 71, 22, BLOODY_AXE_INSIGNIAS}, {20251, 3, 1, 70, 30, DELU_FANG}, {20252, 3, 1, 67, 28, DELU_FANG}, {20253, 3, 1, 65, 26, DELU_FANG}, {27151, 3, 1, 69, 31, DELU_FANG}, {20157, 4, 1, 66, 32, STAKATO_TALONS}, {20230, 4, 1, 68, 26, STAKATO_TALONS}, {20232, 4, 1, 67, 28, STAKATO_TALONS}, {20234, 4, 1, 69, 32, STAKATO_TALONS}, {27152, 4, 1, 69, 32, STAKATO_TALONS}};
    private int[] statue_list = {STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG};
    private int[] tablet_list = {FRAGMENT_ANCIENT_TABLE1, FRAGMENT_ANCIENT_TABLE2, FRAGMENT_ANCIENT_TABLE3, FRAGMENT_ANCIENT_TABLE4};

    public _333_BlackLionHunt() {
        super(false);
        addStartNpc(Sophya);
        addTalkId(Redfoot);
        addTalkId(Rupio);
        addTalkId(Undrias);
        addTalkId(Lockirin);
        addTalkId(Morgan);
        Arrays.stream(DROPLIST).map(aDROPLIST -> aDROPLIST[0]).forEach(this::addKillId);
        addQuestItem(LIONS_CLAW, LIONS_EYE, GUILD_COIN, UNDEAD_ASH, BLOODY_AXE_INSIGNIAS, DELU_FANG, STAKATO_TALONS, SOPHIAS_LETTER1, SOPHIAS_LETTER2, SOPHIAS_LETTER3, SOPHIAS_LETTER4);
    }

    

    public void giveRewards(final QuestState st, final int item, final long count) {
        st.giveItems(57, 35L * count);
        st.takeItems(item, count);
        if (count >= 20L) {
            st.giveItems(LIONS_CLAW, count / 20L * (long) st.getRateQuestsReward());
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int part = st.getInt("part");
        if ("start".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            return "30735-01.htm";
        }
        if ("p1_t".equalsIgnoreCase(event)) {
            st.set("part", "1");
            st.giveItems(SOPHIAS_LETTER1, 1L);
            return "30735-02.htm";
        }
        if ("p2_t".equalsIgnoreCase(event)) {
            st.set("part", "2");
            st.giveItems(SOPHIAS_LETTER2, 1L);
            return "30735-03.htm";
        }
        if ("p3_t".equalsIgnoreCase(event)) {
            st.set("part", "3");
            st.giveItems(SOPHIAS_LETTER3, 1L);
            return "30735-04.htm";
        }
        if ("p4_t".equalsIgnoreCase(event)) {
            st.set("part", "4");
            st.giveItems(SOPHIAS_LETTER4, 1L);
            return "30735-05.htm";
        }
        if ("exit".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            return "30735-exit.htm";
        }
        if ("continue".equalsIgnoreCase(event)) {
            long claw = st.getQuestItemsCount(LIONS_CLAW) / 10L;
            final long check_eye = st.getQuestItemsCount(LIONS_EYE);
            if (claw <= 0L) {
                return "30735-start.htm";
            }
            st.giveItems(LIONS_EYE, claw);
            final long eye = st.getQuestItemsCount(LIONS_EYE);
            st.takeItems(LIONS_CLAW, claw * 10L);
            int ala_count = 3;
            int soul_count = 100;
            int soe_count = 20;
            int heal_count = 20;
            int spir_count = 50;
            if (eye > 9L) {
                ala_count = 4;
                soul_count = 400;
                soe_count = 30;
                heal_count = 50;
                spir_count = 200;
            } else if (eye > 4L) {
                spir_count = 100;
                soul_count = 200;
                heal_count = 25;
            }
            while (claw > 0L) {
                final int n = Rnd.get(5);
                switch (n) {
                    case 0:
                        st.giveItems(ALACRITY_POTION, Math.round(ala_count * st.getRateQuestsReward()));
                        break;
                    case 1:
                        st.giveItems(SOULSHOT_D, Math.round(soul_count * st.getRateQuestsReward()));
                        break;
                    case 2:
                        st.giveItems(SCROLL_ESCAPE, Math.round(soe_count * st.getRateQuestsReward()));
                        break;
                    case 3:
                        st.giveItems(SPIRITSHOT_D, Math.round(spir_count * st.getRateQuestsReward()));
                        break;
                    case 4:
                        st.giveItems(HEALING_POTION, Math.round(heal_count * st.getRateQuestsReward()));
                        break;
                }
                --claw;
            }
            if (check_eye > 0L) {
                return "30735-06.htm";
            }
            return "30735-06.htm";
        } else {
            if ("leave".equalsIgnoreCase(event)) {
                int order;
                switch (part) {
                    case 1:
                        order = SOPHIAS_LETTER1;
                        break;
                    case 2:
                        order = SOPHIAS_LETTER2;
                        break;
                    case 3:
                        order = SOPHIAS_LETTER3;
                        break;
                    case 4:
                        order = SOPHIAS_LETTER4;
                        break;
                    default:
                        order = 0;
                        break;
                }
                st.set("part", "0");
                if (order > 0) {
                    st.takeItems(order, 1L);
                }
                return "30735-07.htm";
            }
            if (!"f_info".equalsIgnoreCase(event)) {
                if ("f_give".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(CARGO_BOX1) <= 0L) {
                        return "red_foor-no_box.htm";
                    }
                    if (st.getQuestItemsCount(57) < OPEN_BOX_PRICE) {
                        return "red_foor-no_adena.htm";
                    }
                    st.takeItems(CARGO_BOX1, 1L);
                    st.takeItems(57, (long) OPEN_BOX_PRICE);
                    final int rand = Rnd.get(1, 162);
                    if (rand < 21) {
                        st.giveItems(GLUDIO_APPLE, 1L);
                        return "red_foor-02.htm";
                    }
                    if (rand < 41) {
                        st.giveItems(CORN_MEAL, 1L);
                        return "red_foor-03.htm";
                    }
                    if (rand < 61) {
                        st.giveItems(WOLF_PELTS, 1L);
                        return "red_foor-04.htm";
                    }
                    if (rand < 74) {
                        st.giveItems(MONNSTONE, 1L);
                        return "red_foor-05.htm";
                    }
                    if (rand < 86) {
                        st.giveItems(GLUDIO_WEETS_FLOWER, 1L);
                        return "red_foor-06.htm";
                    }
                    if (rand < 98) {
                        st.giveItems(SPIDERSILK_ROPE, 1L);
                        return "red_foor-07.htm";
                    }
                    if (rand < 99) {
                        st.giveItems(ALEXANDRIT, 1L);
                        return "red_foor-08.htm";
                    }
                    if (rand < 109) {
                        st.giveItems(SILVER_TEA, 1L);
                        return "red_foor-09.htm";
                    }
                    if (rand < 119) {
                        st.giveItems(GOLEM_PART, 1L);
                        return "red_foor-10.htm";
                    }
                    if (rand < 123) {
                        st.giveItems(FIRE_EMERALD, 1L);
                        return "red_foor-11.htm";
                    }
                    if (rand < 127) {
                        st.giveItems(SILK_FROCK, 1L);
                        return "red_foor-12.htm";
                    }
                    if (rand < 131) {
                        st.giveItems(PORCELAN_URN, 1L);
                        return "red_foor-13.htm";
                    }
                    if (rand < 132) {
                        st.giveItems(IMPERIAL_DIAMOND, 1L);
                        return "red_foor-13.htm";
                    }
                    if (rand < 147) {
                        final int random_stat = Rnd.get(4);
                        if (random_stat == 3) {
                            st.giveItems(STATUE_SHILIEN_HEAD, 1L);
                            return "red_foor-14.htm";
                        }
                        if (random_stat == 0) {
                            st.giveItems(STATUE_SHILIEN_TORSO, 1L);
                            return "red_foor-14.htm";
                        }
                        if (random_stat == 1) {
                            st.giveItems(STATUE_SHILIEN_ARM, 1L);
                            return "red_foor-14.htm";
                        }
                        if (random_stat == 2) {
                            st.giveItems(STATUE_SHILIEN_LEG, 1L);
                            return "red_foor-14.htm";
                        }
                    } else if (rand <= 162) {
                        final int random_tab = Rnd.get(4);
                        if (random_tab == 0) {
                            st.giveItems(FRAGMENT_ANCIENT_TABLE1, 1L);
                            return "red_foor-15.htm";
                        }
                        if (random_tab == 1) {
                            st.giveItems(FRAGMENT_ANCIENT_TABLE2, 1L);
                            return "red_foor-15.htm";
                        }
                        if (random_tab == 2) {
                            st.giveItems(FRAGMENT_ANCIENT_TABLE3, 1L);
                            return "red_foor-15.htm";
                        }
                        if (random_tab == 3) {
                            st.giveItems(FRAGMENT_ANCIENT_TABLE4, 1L);
                            return "red_foor-15.htm";
                        }
                    }
                } else if ("r_give_statue".equalsIgnoreCase(event) || "r_give_tablet".equalsIgnoreCase(event)) {
                    int[] items = statue_list;
                    int item = COMPLETE_STATUE;
                    String pieces = "rupio-01.htm";
                    String brockes = "rupio-02.htm";
                    String complete = "rupio-03.htm";
                    if ("r_give_tablet".equalsIgnoreCase(event)) {
                        items = tablet_list;
                        item = COMPLETE_TABLET;
                        pieces = "rupio-04.htm";
                        brockes = "rupio-05.htm";
                        complete = "rupio-06.htm";
                    }
                    int count = 0;
                    for (int id = items[0]; id <= items[items.length - 1]; ++id) {
                        if (st.getQuestItemsCount(id) > 0L) {
                            ++count;
                        }
                    }
                    if (count > 3) {
                        for (int id = items[0]; id <= items[items.length - 1]; ++id) {
                            st.takeItems(id, 1L);
                        }
                        if (Rnd.chance(2)) {
                            st.giveItems(item, 1L);
                            return complete;
                        }
                        return brockes;
                    } else {
                        if (count < 4 && count != 0) {
                            return pieces;
                        }
                        return "rupio-07.htm";
                    }
                } else if ("l_give".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(COMPLETE_TABLET) > 0L) {
                        st.takeItems(COMPLETE_TABLET, 1L);
                        st.giveItems(57, 30000L);
                        return "lockirin-01.htm";
                    }
                    return "lockirin-02.htm";
                } else if ("u_give".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(COMPLETE_STATUE) > 0L) {
                        st.takeItems(COMPLETE_STATUE, 1L);
                        st.giveItems(57, 30000L);
                        return "undiras-01.htm";
                    }
                    return "undiras-02.htm";
                } else if ("m_give".equalsIgnoreCase(event)) {
                    if (st.getQuestItemsCount(CARGO_BOX1) <= 0L) {
                        return "morgan-03.htm";
                    }
                    final long coins = st.getQuestItemsCount(GUILD_COIN);
                    long count2 = coins / 40L;
                    if (count2 > 2L) {
                        count2 = 2L;
                    }
                    st.giveItems(GUILD_COIN, 1L);
                    st.giveItems(57, (1L + count2) * 100L);
                    st.takeItems(CARGO_BOX1, 1L);
                    final int rand2 = Rnd.get(0, 3);
                    if (rand2 == 0) {
                        return "morgan-01.htm";
                    }
                    if (rand2 == 1) {
                        return "morgan-02.htm";
                    }
                    return "morgan-02.htm";
                } else {
                    if ("start_parts".equalsIgnoreCase(event)) {
                        return "30735-08.htm";
                    }
                    if ("m_reward".equalsIgnoreCase(event)) {
                        return "morgan-05.htm";
                    }
                    if ("u_info".equalsIgnoreCase(event)) {
                        return "undiras-03.htm";
                    }
                    if ("l_info".equalsIgnoreCase(event)) {
                        return "lockirin-03.htm";
                    }
                    if ("p_redfoot".equalsIgnoreCase(event)) {
                        return "30735-09.htm";
                    }
                    if ("p_trader_info".equalsIgnoreCase(event)) {
                        return "30735-10.htm";
                    }
                    if ("start_chose_parts".equalsIgnoreCase(event)) {
                        return "30735-11.htm";
                    }
                    if ("p1_explanation".equalsIgnoreCase(event)) {
                        return "30735-12.htm";
                    }
                    if ("p2_explanation".equalsIgnoreCase(event)) {
                        return "30735-13.htm";
                    }
                    if ("p3_explanation".equalsIgnoreCase(event)) {
                        return "30735-14.htm";
                    }
                    if ("p4_explanation".equalsIgnoreCase(event)) {
                        return "30735-15.htm";
                    }
                    if ("f_more_help".equalsIgnoreCase(event)) {
                        return "red_foor-16.htm";
                    }
                    if ("r_exit".equalsIgnoreCase(event)) {
                        return "30735-16.htm";
                    }
                }
                return event;
            }
            final int text = st.getInt("text");
            if (text < 4) {
                st.set("text", String.valueOf(text + 1));
                return "red_foor_text_" + Rnd.get(1, 19) + ".htm";
            }
            return "red_foor-01.htm";
        }
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final String htmltext = "noquest";
        if (cond == 0) {
            st.setCond(0);
            st.set("part", "0");
            st.set("text", "0");
            if (npcId == Sophya) {
                if (st.getQuestItemsCount(BLACK_LION_MARK) <= 0L) {
                    st.exitCurrentQuest(true);
                    return "30735-19.htm";
                }
                if (st.getPlayer().getLevel() > 24) {
                    return "30735-17.htm";
                }
                st.exitCurrentQuest(true);
                return "30735-18.htm";
            }
        } else {
            final int part = st.getInt("part");
            switch (npcId) {
                case Sophya:
                    int item;
                    switch (part) {
                        case 1:
                            item = UNDEAD_ASH;
                            break;
                        case 2:
                            item = BLOODY_AXE_INSIGNIAS;
                            break;
                        case 3:
                            item = DELU_FANG;
                            break;
                        default:
                            if (part != 4) {
                                return "30735-20.htm";
                            }
                            item = STAKATO_TALONS;
                            break;
                    }
                    final long count = st.getQuestItemsCount(item);
                    final long box = st.getQuestItemsCount(CARGO_BOX1);
                    if (box > 0L && count > 0L) {
                        giveRewards(st, item, count);
                        return "30735-21.htm";
                    }
                    if (box > 0L) {
                        return "30735-22.htm";
                    }
                    if (count > 0L) {
                        giveRewards(st, item, count);
                        return "30735-23.htm";
                    }
                    return "30735-24.htm";
                case Redfoot:
                    if (st.getQuestItemsCount(CARGO_BOX1) > 0L) {
                        return "red_foor_text_20.htm";
                    }
                    return "red_foor_text_21.htm";
                case Rupio: {
                    int count2 = 0;
                    for (int i = 3457; i <= 3460; ++i) {
                        if (st.getQuestItemsCount(i) > 0L) {
                            ++count2;
                        }
                    }
                    for (int i = 3462; i <= 3465; ++i) {
                        if (st.getQuestItemsCount(i) > 0L) {
                            ++count2;
                        }
                    }
                    if (count2 > 0) {
                        return "rupio-08.htm";
                    }
                    return "rupio-07.htm";
                }
                case Undrias: {
                    if (st.getQuestItemsCount(COMPLETE_STATUE) > 0L) {
                        return "undiras-04.htm";
                    }
                    int count2 = 0;
                    for (int i = 3457; i <= 3460; ++i) {
                        if (st.getQuestItemsCount(i) > 0L) {
                            ++count2;
                        }
                    }
                    if (count2 > 0) {
                        return "undiras-05.htm";
                    }
                    return "undiras-02.htm";
                }
                case Lockirin: {
                    if (st.getQuestItemsCount(COMPLETE_TABLET) > 0L) {
                        return "lockirin-04.htm";
                    }
                    int count2 = 0;
                    for (int i = 3462; i <= 3465; ++i) {
                        if (st.getQuestItemsCount(i) > 0L) {
                            ++count2;
                        }
                    }
                    if (count2 > 0) {
                        return "lockirin-05.htm";
                    }
                    return "lockirin-06.htm";
                }
                case Morgan:
                    if (st.getQuestItemsCount(CARGO_BOX1) > 0L) {
                        return "morgan-06.htm";
                    }
                    return "morgan-07.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        boolean on_npc = false;
        int part = 0;
        int allowDrop = 0;
        int chancePartItem = 0;
        int chanceBox = 0;
        int partItem = 0;
        for (int[] aDROPLIST : DROPLIST) {
            if (aDROPLIST[0] == npcId) {
                part = aDROPLIST[1];
                allowDrop = aDROPLIST[2];
                chancePartItem = aDROPLIST[3];
                chanceBox = aDROPLIST[4];
                partItem = aDROPLIST[5];
                on_npc = true;
            }
        }
        if (on_npc) {
            final int rand = Rnd.get(1, 100);
            final int rand2 = Rnd.get(1, 100);
            if (allowDrop == 1 && st.getInt("part") == part && rand < chancePartItem) {
                st.giveItems(partItem, (npcId == 27152) ? 8L : 1L);
                st.playSound("ItemSound.quest_itemget");
                if (rand2 < chanceBox) {
                    st.giveItems(CARGO_BOX1, 1L);
                    if (rand > chancePartItem) {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        }
        if (Rnd.chance(4) && (npcId == 20251 || npcId == 20252 || npcId == 20253)) {
            st.addSpawn(21105);
            st.addSpawn(21105);
        }
        if (npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234) {
            if (Rnd.chance(2)) {
                st.addSpawn(27152);
            }
            if (Rnd.chance(15)) {
                st.giveItems(CARGO_BOX1, 1L);
            }
        }
        return null;
    }
}
