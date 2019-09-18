package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _417_PathToScavenger extends Quest {
    int RING_OF_RAVEN;
    int PIPIS_LETTER;
    int ROUTS_TP_SCROLL;
    int SUCCUBUS_UNDIES;
    int MIONS_LETTER;
    int BRONKS_INGOT;
    int CHARIS_AXE;
    int ZIMENFS_POTION;
    int BRONKS_PAY;
    int CHALIS_PAY;
    int ZIMENFS_PAY;
    int BEAR_PIC;
    int TARANTULA_PIC;
    int HONEY_JAR;
    int BEAD;
    int BEAD_PARCEL;
    int Pippi;
    int Raut;
    int Shari;
    int Mion;
    int Bronk;
    int Zimenf;
    int Toma;
    int Torai;
    int HunterTarantula;
    int HoneyBear;
    int PlunderTarantula;
    int HunterBear;

    public _417_PathToScavenger() {
        super(false);
        RING_OF_RAVEN = 1642;
        PIPIS_LETTER = 1643;
        ROUTS_TP_SCROLL = 1644;
        SUCCUBUS_UNDIES = 1645;
        MIONS_LETTER = 1646;
        BRONKS_INGOT = 1647;
        CHARIS_AXE = 1648;
        ZIMENFS_POTION = 1649;
        BRONKS_PAY = 1650;
        CHALIS_PAY = 1651;
        ZIMENFS_PAY = 1652;
        BEAR_PIC = 1653;
        TARANTULA_PIC = 1654;
        HONEY_JAR = 1655;
        BEAD = 1656;
        BEAD_PARCEL = 1657;
        Pippi = 30524;
        Raut = 30316;
        Shari = 30517;
        Mion = 30519;
        Bronk = 30525;
        Zimenf = 30538;
        Toma = 30556;
        Torai = 30557;
        HunterTarantula = 20403;
        HoneyBear = 27058;
        PlunderTarantula = 20508;
        HunterBear = 20777;
        addStartNpc(Pippi);
        addTalkId(Raut);
        addTalkId(Shari);
        addTalkId(Mion);
        addTalkId(Bronk);
        addTalkId(Zimenf);
        addTalkId(Toma);
        addTalkId(Torai);
        addKillId(HunterTarantula);
        addKillId(HoneyBear);
        addKillId(PlunderTarantula);
        addKillId(HunterBear);
        addQuestItem(CHALIS_PAY);
        addQuestItem(ZIMENFS_PAY);
        addQuestItem(BRONKS_PAY);
        addQuestItem(PIPIS_LETTER);
        addQuestItem(CHARIS_AXE);
        addQuestItem(ZIMENFS_POTION);
        addQuestItem(BRONKS_INGOT);
        addQuestItem(MIONS_LETTER);
        addQuestItem(HONEY_JAR);
        addQuestItem(BEAR_PIC);
        addQuestItem(BEAD_PARCEL);
        addQuestItem(BEAD);
        addQuestItem(TARANTULA_PIC);
        addQuestItem(SUCCUBUS_UNDIES);
        addQuestItem(ROUTS_TP_SCROLL);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        int n;
        switch (event) {
            case "1":
                st.set("id", "0");
                if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 53 && st.getQuestItemsCount(RING_OF_RAVEN) == 0L) {
                    st.setCond(1);
                    st.setState(2);
                    st.playSound("ItemSound.quest_accept");
                    st.giveItems(PIPIS_LETTER, 1L);
                    htmltext = "collector_pipi_q0417_05.htm";
                } else if (st.getPlayer().getClassId().getId() != 53) {
                    if (st.getPlayer().getClassId().getId() == 54) {
                        htmltext = "collector_pipi_q0417_02a.htm";
                    } else {
                        htmltext = "collector_pipi_q0417_08.htm";
                    }
                } else if (st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 53) {
                    htmltext = "collector_pipi_q0417_02.htm";
                } else if (st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 53 && st.getQuestItemsCount(RING_OF_RAVEN) == 1L) {
                    htmltext = "collector_pipi_q0417_04.htm";
                }
                break;
            case "30519_1":
                if (st.getQuestItemsCount(PIPIS_LETTER) > 0L) {
                    st.takeItems(PIPIS_LETTER, 1L);
                    st.setCond(2);
                    n = Rnd.get(3);
                    switch (n) {
                        case 0:
                            htmltext = "trader_mion_q0417_02.htm";
                            st.giveItems(ZIMENFS_POTION, 1L);
                            break;
                        case 1:
                            htmltext = "trader_mion_q0417_03.htm";
                            st.giveItems(CHARIS_AXE, 1L);
                            break;
                        case 2:
                            htmltext = "trader_mion_q0417_04.htm";
                            st.giveItems(BRONKS_INGOT, 1L);
                            break;
                    }
                } else {
                    htmltext = "noquest";
                }
                break;
            case "30519_2":
                htmltext = "trader_mion_q0417_06.htm";
                break;
            case "30519_3":
                htmltext = "trader_mion_q0417_07.htm";
                st.set("id", String.valueOf(st.getInt("id") + 1));
                break;
            case "30519_4":
                n = Rnd.get(2);
                if (n == 0) {
                    htmltext = "trader_mion_q0417_06.htm";
                } else if (n == 1) {
                    htmltext = "trader_mion_q0417_11.htm";
                }
                break;
            case "30519_5":
                if (st.getQuestItemsCount(ZIMENFS_POTION, CHARIS_AXE, BRONKS_INGOT) > 0L) {
                    if (st.getInt("id") / 10 < 2) {
                        htmltext = "trader_mion_q0417_07.htm";
                        st.set("id", String.valueOf(st.getInt("id") + 1));
                    } else if (st.getInt("id") / 10 >= 2 && cond == 0) {
                        htmltext = "trader_mion_q0417_09.htm";
                        if (st.getInt("id") / 10 < 3) {
                            st.set("id", String.valueOf(st.getInt("id") + 1));
                        }
                    } else if (st.getInt("id") / 10 >= 3 && cond > 0) {
                        htmltext = "trader_mion_q0417_10.htm";
                        st.giveItems(MIONS_LETTER, 1L);
                        st.takeItems(CHARIS_AXE, 1L);
                        st.takeItems(ZIMENFS_POTION, 1L);
                        st.takeItems(BRONKS_INGOT, 1L);
                    }
                } else {
                    htmltext = "noquest";
                }
                break;
            case "30519_6":
                if (st.getQuestItemsCount(ZIMENFS_PAY) > 0L || st.getQuestItemsCount(CHALIS_PAY) > 0L || st.getQuestItemsCount(BRONKS_PAY) > 0L) {
                    n = Rnd.get(3);
                    st.takeItems(ZIMENFS_PAY, 1L);
                    st.takeItems(CHALIS_PAY, 1L);
                    st.takeItems(BRONKS_PAY, 1L);
                    switch (n) {
                        case 0:
                            htmltext = "trader_mion_q0417_02.htm";
                            st.giveItems(ZIMENFS_POTION, 1L);
                            break;
                        case 1:
                            htmltext = "trader_mion_q0417_03.htm";
                            st.giveItems(CHARIS_AXE, 1L);
                            break;
                        case 2:
                            htmltext = "trader_mion_q0417_04.htm";
                            st.giveItems(BRONKS_INGOT, 1L);
                            break;
                    }
                } else {
                    htmltext = "noquest";
                }
                break;
            case "30316_1":
                if (st.getQuestItemsCount(BEAD_PARCEL) > 0L) {
                    htmltext = "raut_q0417_02.htm";
                    st.takeItems(BEAD_PARCEL, 1L);
                    st.giveItems(ROUTS_TP_SCROLL, 1L);
                    st.setCond(10);
                } else {
                    htmltext = "noquest";
                }
                break;
            case "30316_2":
                if (st.getQuestItemsCount(BEAD_PARCEL) > 0L) {
                    htmltext = "raut_q0417_03.htm";
                    st.takeItems(BEAD_PARCEL, 1L);
                    st.giveItems(ROUTS_TP_SCROLL, 1L);
                    st.setCond(10);
                } else {
                    htmltext = "noquest";
                }
                break;
            case "30557_1":
                htmltext = "torai_q0417_02.htm";
                break;
            case "30557_2":
                if (st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0L) {
                    htmltext = "torai_q0417_03.htm";
                    st.takeItems(ROUTS_TP_SCROLL, 1L);
                    st.giveItems(SUCCUBUS_UNDIES, 1L);
                    st.setCond(11);
                } else {
                    htmltext = "noquest";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            st.setState(2);
        }
        if (npcId == Pippi) {
            if (cond == 0) {
                htmltext = "collector_pipi_q0417_01.htm";
            } else if (st.getQuestItemsCount(PIPIS_LETTER) > 0L) {
                htmltext = "collector_pipi_q0417_06.htm";
            } else if (st.getQuestItemsCount(PIPIS_LETTER) == 0L && id == 2) {
                htmltext = "collector_pipi_q0417_01.htm";
            } else if (st.getQuestItemsCount(PIPIS_LETTER) == 0L) {
                htmltext = "collector_pipi_q0417_07.htm";
            }
        } else {
            if (cond == 0) {
                return "noquest";
            }
            if (npcId == Mion) {
                if (st.getQuestItemsCount(PIPIS_LETTER) > 0L) {
                    htmltext = "trader_mion_q0417_01.htm";
                } else if (st.getQuestItemsCount(CHARIS_AXE, BRONKS_INGOT, ZIMENFS_POTION) > 0L && st.getInt("id") / 10 == 0) {
                    htmltext = "trader_mion_q0417_05.htm";
                } else if (st.getQuestItemsCount(CHARIS_AXE, BRONKS_INGOT, ZIMENFS_POTION) > 0L && st.getInt("id") / 10 > 0) {
                    htmltext = "trader_mion_q0417_08.htm";
                } else if (st.getQuestItemsCount(CHALIS_PAY, BRONKS_PAY, ZIMENFS_PAY) > 0L && st.getInt("id") < 50) {
                    htmltext = "trader_mion_q0417_12.htm";
                } else if (st.getQuestItemsCount(CHALIS_PAY, BRONKS_PAY, ZIMENFS_PAY) > 0L && st.getInt("id") >= 50) {
                    htmltext = "trader_mion_q0417_15.htm";
                    st.giveItems(MIONS_LETTER, 1L);
                    st.takeItems(CHALIS_PAY, -1L);
                    st.takeItems(ZIMENFS_PAY, -1L);
                    st.takeItems(BRONKS_PAY, -1L);
                    st.setCond(4);
                } else if (st.getQuestItemsCount(MIONS_LETTER) > 0L) {
                    htmltext = "trader_mion_q0417_13.htm";
                } else if (st.getQuestItemsCount(BEAR_PIC) > 0L || st.getQuestItemsCount(TARANTULA_PIC) > 0L || st.getQuestItemsCount(BEAD_PARCEL) > 0L || st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0L || st.getQuestItemsCount(SUCCUBUS_UNDIES) > 0L) {
                    htmltext = "trader_mion_q0417_14.htm";
                }
            } else if (npcId == Shari) {
                if (st.getQuestItemsCount(CHARIS_AXE) > 0L) {
                    if (st.getInt("id") < 20) {
                        htmltext = "trader_chali_q0417_01.htm";
                    } else {
                        htmltext = "trader_chali_q0417_02.htm";
                    }
                    st.takeItems(CHARIS_AXE, 1L);
                    st.giveItems(CHALIS_PAY, 1L);
                    if (st.getInt("id") >= 50) {
                        st.setCond(3);
                    }
                    st.set("id", st.getInt("id") + 10);
                } else if (st.getQuestItemsCount(CHALIS_PAY) == 1L) {
                    htmltext = "trader_chali_q0417_03.htm";
                }
            } else if (npcId == Bronk) {
                if (st.getQuestItemsCount(BRONKS_INGOT) == 1L) {
                    if (st.getInt("id") < 20) {
                        htmltext = "head_blacksmith_bronk_q0417_01.htm";
                    } else {
                        htmltext = "head_blacksmith_bronk_q0417_02.htm";
                    }
                    st.takeItems(BRONKS_INGOT, 1L);
                    st.giveItems(BRONKS_PAY, 1L);
                    if (st.getInt("id") >= 50) {
                        st.setCond(3);
                    }
                    st.set("id", st.getInt("id") + 10);
                } else if (st.getQuestItemsCount(BRONKS_PAY) == 1L) {
                    htmltext = "head_blacksmith_bronk_q0417_03.htm";
                }
            } else if (npcId == Zimenf) {
                if (st.getQuestItemsCount(ZIMENFS_POTION) == 1L) {
                    if (st.getInt("id") < 20) {
                        htmltext = "zimenf_priest_of_earth_q0417_01.htm";
                    } else {
                        htmltext = "zimenf_priest_of_earth_q0417_02.htm";
                    }
                    st.takeItems(ZIMENFS_POTION, 1L);
                    st.giveItems(ZIMENFS_PAY, 1L);
                    if (st.getInt("id") >= 50) {
                        st.setCond(3);
                    }
                    st.set("id", st.getInt("id") + 10);
                } else if (st.getQuestItemsCount(ZIMENFS_PAY) == 1L) {
                    htmltext = "zimenf_priest_of_earth_q0417_03.htm";
                }
            } else if (npcId == Toma) {
                if (st.getQuestItemsCount(MIONS_LETTER) == 1L) {
                    htmltext = "master_toma_q0417_01.htm";
                    st.takeItems(MIONS_LETTER, 1L);
                    st.giveItems(BEAR_PIC, 1L);
                    st.setCond(5);
                    st.set("id", String.valueOf(0));
                } else if (st.getQuestItemsCount(BEAR_PIC) == 1L && st.getQuestItemsCount(HONEY_JAR) < 5L) {
                    htmltext = "master_toma_q0417_02.htm";
                } else if (st.getQuestItemsCount(BEAR_PIC) == 1L && st.getQuestItemsCount(HONEY_JAR) >= 5L) {
                    htmltext = "master_toma_q0417_03.htm";
                    st.takeItems(HONEY_JAR, st.getQuestItemsCount(HONEY_JAR));
                    st.takeItems(BEAR_PIC, 1L);
                    st.giveItems(TARANTULA_PIC, 1L);
                    st.setCond(7);
                } else if (st.getQuestItemsCount(TARANTULA_PIC) == 1L && st.getQuestItemsCount(BEAD) < 20L) {
                    htmltext = "master_toma_q0417_04.htm";
                } else if (st.getQuestItemsCount(TARANTULA_PIC) == 1L && st.getQuestItemsCount(BEAD) >= 20L) {
                    htmltext = "master_toma_q0417_05.htm";
                    st.takeItems(BEAD, st.getQuestItemsCount(BEAD));
                    st.takeItems(TARANTULA_PIC, 1L);
                    st.giveItems(BEAD_PARCEL, 1L);
                    st.setCond(9);
                } else if (st.getQuestItemsCount(BEAD_PARCEL) > 0L) {
                    htmltext = "master_toma_q0417_06.htm";
                } else if (st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0L || st.getQuestItemsCount(SUCCUBUS_UNDIES) > 0L) {
                    htmltext = "master_toma_q0417_07.htm";
                }
            } else if (npcId == Raut) {
                if (st.getQuestItemsCount(BEAD_PARCEL) == 1L) {
                    htmltext = "raut_q0417_01.htm";
                } else if (st.getQuestItemsCount(ROUTS_TP_SCROLL) == 1L) {
                    htmltext = "raut_q0417_04.htm";
                } else if (st.getQuestItemsCount(SUCCUBUS_UNDIES) == 1L) {
                    htmltext = "raut_q0417_05.htm";
                    st.takeItems(SUCCUBUS_UNDIES, 1L);
                    if (st.getPlayer().getClassId().getLevel() == 1) {
                        st.giveItems(RING_OF_RAVEN, 1L);
                        if (!st.getPlayer().getVarB("prof1")) {
                            st.getPlayer().setVar("prof1", "1", -1L);
                            st.addExpAndSp(3200L, 7080L);
                        }
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
            } else if (npcId == Torai && st.getQuestItemsCount(ROUTS_TP_SCROLL) == 1L) {
                htmltext = "torai_q0417_01.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final MonsterInstance mob = (MonsterInstance) npc;
        final boolean cond = st.getCond() > 0;
        if (npcId == HunterBear) {
            if (cond && st.getQuestItemsCount(BEAR_PIC) == 1L && st.getQuestItemsCount(HONEY_JAR) < 5L && Rnd.chance(20)) {
                st.addSpawn(HoneyBear);
            }
        } else if (npcId == HoneyBear) {
            if (cond && st.getQuestItemsCount(BEAR_PIC) == 1L && st.getQuestItemsCount(HONEY_JAR) < 5L && mob.isSpoiled()) {
                st.giveItems(HONEY_JAR, 1L);
                if (st.getQuestItemsCount(HONEY_JAR) == 5L) {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(6);
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if ((npcId == HunterTarantula || npcId == PlunderTarantula) && cond && st.getQuestItemsCount(TARANTULA_PIC) == 1L && st.getQuestItemsCount(BEAD) < 20L && mob.isSpoiled() && Rnd.chance(50)) {
            st.giveItems(BEAD, 1L);
            if (st.getQuestItemsCount(BEAD) == 20L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(8);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
