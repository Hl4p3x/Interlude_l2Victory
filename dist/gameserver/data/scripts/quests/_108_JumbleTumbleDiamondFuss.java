package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _108_JumbleTumbleDiamondFuss extends Quest {
    int GOUPHS_CONTRACT;
    int REEPS_CONTRACT;
    int ELVEN_WINE;
    int BRONPS_DICE;
    int BRONPS_CONTRACT;
    int AQUAMARINE;
    int CHRYSOBERYL;
    int GEM_BOX1;
    int COAL_PIECE;
    int BRONPS_LETTER;
    int BERRY_TART;
    int BAT_DIAGRAM;
    int STAR_DIAMOND;
    int SILVERSMITH_HAMMER;

    public _108_JumbleTumbleDiamondFuss() {
        super(false);
        GOUPHS_CONTRACT = 1559;
        REEPS_CONTRACT = 1560;
        ELVEN_WINE = 1561;
        BRONPS_DICE = 1562;
        BRONPS_CONTRACT = 1563;
        AQUAMARINE = 1564;
        CHRYSOBERYL = 1565;
        GEM_BOX1 = 1566;
        COAL_PIECE = 1567;
        BRONPS_LETTER = 1568;
        BERRY_TART = 1569;
        BAT_DIAGRAM = 1570;
        STAR_DIAMOND = 1571;
        SILVERSMITH_HAMMER = 1511;
        addStartNpc(30523);
        addTalkId(30516);
        addTalkId(30521);
        addTalkId(30522);
        addTalkId(30526);
        addTalkId(30529);
        addTalkId(30555);
        addKillId(20323);
        addKillId(20324);
        addKillId(20480);
        addQuestItem(GEM_BOX1, STAR_DIAMOND, GOUPHS_CONTRACT, REEPS_CONTRACT, ELVEN_WINE, BRONPS_CONTRACT, AQUAMARINE, CHRYSOBERYL, COAL_PIECE, BRONPS_DICE, BRONPS_LETTER, BERRY_TART, BAT_DIAGRAM);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "collector_gouph_q0108_03.htm":
                st.setCond(1);
                st.setState(2);
                st.giveItems(GOUPHS_CONTRACT, 1L);
                st.playSound("ItemSound.quest_accept");
                break;
            case "carrier_torocco_q0108_02.htm":
                st.takeItems(REEPS_CONTRACT, 1L);
                st.giveItems(ELVEN_WINE, 1L);
                st.setCond(3);
                break;
            case "blacksmith_bronp_q0108_02.htm":
                st.takeItems(BRONPS_DICE, 1L);
                st.giveItems(BRONPS_CONTRACT, 1L);
                st.setCond(5);
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        switch (npcId) {
            case 30523:
                if (cond == 0) {
                    if (st.getPlayer().getRace() != Race.dwarf) {
                        htmltext = "collector_gouph_q0108_00.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() >= 10) {
                        htmltext = "collector_gouph_q0108_02.htm";
                    } else {
                        htmltext = "collector_gouph_q0108_01.htm";
                        st.exitCurrentQuest(true);
                    }
                } else if (cond == 0 && st.getQuestItemsCount(GOUPHS_CONTRACT) > 0L) {
                    htmltext = "collector_gouph_q0108_04.htm";
                } else if (cond > 1 && cond < 7 && (st.getQuestItemsCount(REEPS_CONTRACT) > 0L || st.getQuestItemsCount(ELVEN_WINE) > 0L || st.getQuestItemsCount(BRONPS_DICE) > 0L || st.getQuestItemsCount(BRONPS_CONTRACT) > 0L)) {
                    htmltext = "collector_gouph_q0108_05.htm";
                } else if (cond == 7 && st.getQuestItemsCount(GEM_BOX1) > 0L) {
                    htmltext = "collector_gouph_q0108_06.htm";
                    st.takeItems(GEM_BOX1, 1L);
                    st.giveItems(COAL_PIECE, 1L);
                    st.setCond(8);
                } else if (cond > 7 && cond < 12 && (st.getQuestItemsCount(BRONPS_LETTER) > 0L || st.getQuestItemsCount(COAL_PIECE) > 0L || st.getQuestItemsCount(BERRY_TART) > 0L || st.getQuestItemsCount(BAT_DIAGRAM) > 0L)) {
                    htmltext = "collector_gouph_q0108_07.htm";
                } else if (cond == 12 && st.getQuestItemsCount(STAR_DIAMOND) > 0L) {
                    htmltext = "collector_gouph_q0108_08.htm";
                    st.takeItems(STAR_DIAMOND, 1L);
                    st.giveItems(SILVERSMITH_HAMMER, 1L);
                    st.getPlayer().addExpAndSp(34565L, 2962L);
                    st.giveItems(57, 14666L, false);
                    if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3")) {
                        st.getPlayer().setVar("p1q3", "1", -1L);
                        st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                        st.giveItems(1060, 100L);
                        for (int item = 4412; item <= 4417; ++item) {
                            st.giveItems(item, 10L);
                        }
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                }
                break;
            case 30516:
                if (cond == 1 && st.getQuestItemsCount(GOUPHS_CONTRACT) > 0L) {
                    htmltext = "trader_reep_q0108_01.htm";
                    st.giveItems(REEPS_CONTRACT, 1L);
                    st.takeItems(GOUPHS_CONTRACT, 1L);
                    st.setCond(2);
                } else if (cond >= 2) {
                    htmltext = "trader_reep_q0108_02.htm";
                }
                break;
            case 30555:
                if (cond == 2 && st.getQuestItemsCount(REEPS_CONTRACT) == 1L) {
                    htmltext = "carrier_torocco_q0108_01.htm";
                } else if (cond == 3 && st.getQuestItemsCount(ELVEN_WINE) > 0L) {
                    htmltext = "carrier_torocco_q0108_03.htm";
                } else if (cond == 7 && st.getQuestItemsCount(GEM_BOX1) == 1L) {
                    htmltext = "carrier_torocco_q0108_04.htm";
                } else {
                    htmltext = "carrier_torocco_q0108_05.htm";
                }
                break;
            case 30529:
                if (cond == 3 && st.getQuestItemsCount(ELVEN_WINE) > 0L) {
                    st.takeItems(ELVEN_WINE, 1L);
                    st.giveItems(BRONPS_DICE, 1L);
                    htmltext = "miner_maron_q0108_01.htm";
                    st.setCond(4);
                } else if (cond == 4) {
                    htmltext = "miner_maron_q0108_02.htm";
                } else {
                    htmltext = "miner_maron_q0108_03.htm";
                }
                break;
            case 30526:
                if (cond == 4 && st.getQuestItemsCount(BRONPS_DICE) > 0L) {
                    htmltext = "blacksmith_bronp_q0108_01.htm";
                } else if (cond == 5 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0L && (st.getQuestItemsCount(AQUAMARINE) < 10L || st.getQuestItemsCount(CHRYSOBERYL) < 10L)) {
                    htmltext = "blacksmith_bronp_q0108_03.htm";
                } else if (cond == 6 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0L && st.getQuestItemsCount(AQUAMARINE) == 10L && st.getQuestItemsCount(CHRYSOBERYL) == 10L) {
                    htmltext = "blacksmith_bronp_q0108_04.htm";
                    st.takeItems(BRONPS_CONTRACT, -1L);
                    st.takeItems(AQUAMARINE, -1L);
                    st.takeItems(CHRYSOBERYL, -1L);
                    st.giveItems(GEM_BOX1, 1L);
                    st.setCond(7);
                } else if (cond == 7 && st.getQuestItemsCount(GEM_BOX1) > 0L) {
                    htmltext = "blacksmith_bronp_q0108_05.htm";
                } else if (cond == 8 && st.getQuestItemsCount(COAL_PIECE) > 0L) {
                    htmltext = "blacksmith_bronp_q0108_06.htm";
                    st.takeItems(COAL_PIECE, 1L);
                    st.giveItems(BRONPS_LETTER, 1L);
                    st.setCond(9);
                } else if (cond == 9 && st.getQuestItemsCount(BRONPS_LETTER) > 0L) {
                    htmltext = "blacksmith_bronp_q0108_07.htm";
                } else {
                    htmltext = "blacksmith_bronp_q0108_08.htm";
                }
                break;
            case 30521:
                if (cond == 9 && st.getQuestItemsCount(BRONPS_LETTER) > 0L) {
                    htmltext = "warehouse_murphrin_q0108_01.htm";
                    st.takeItems(BRONPS_LETTER, 1L);
                    st.giveItems(BERRY_TART, 1L);
                    st.setCond(10);
                } else if (cond == 10 && st.getQuestItemsCount(BERRY_TART) > 0L) {
                    htmltext = "warehouse_murphrin_q0108_02.htm";
                } else {
                    htmltext = "warehouse_murphrin_q0108_03.htm";
                }
                break;
            case 30522:
                if (cond == 10 && st.getQuestItemsCount(BERRY_TART) > 0L) {
                    htmltext = "warehouse_airy_q0108_01.htm";
                    st.takeItems(BERRY_TART, 1L);
                    st.giveItems(BAT_DIAGRAM, 1L);
                    st.setCond(11);
                } else if (cond == 11 && st.getQuestItemsCount(BAT_DIAGRAM) > 0L) {
                    htmltext = "warehouse_airy_q0108_02.htm";
                } else if (cond == 12 && st.getQuestItemsCount(STAR_DIAMOND) > 0L) {
                    htmltext = "warehouse_airy_q0108_03.htm";
                } else {
                    htmltext = "warehouse_airy_q0108_04.htm";
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 20323 || npcId == 20324) {
            if (cond == 5 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0L) {
                if (st.getQuestItemsCount(AQUAMARINE) < 10L && Rnd.chance(80)) {
                    st.giveItems(AQUAMARINE, 1L);
                    if (st.getQuestItemsCount(AQUAMARINE) < 10L) {
                        st.playSound("ItemSound.quest_itemget");
                    } else {
                        st.playSound("ItemSound.quest_middle");
                        if (st.getQuestItemsCount(AQUAMARINE) == 10L && st.getQuestItemsCount(CHRYSOBERYL) == 10L) {
                            st.setCond(6);
                        }
                    }
                }
                if (st.getQuestItemsCount(CHRYSOBERYL) < 10L && Rnd.chance(80)) {
                    st.giveItems(CHRYSOBERYL, 1L);
                    if (st.getQuestItemsCount(CHRYSOBERYL) < 10L) {
                        st.playSound("ItemSound.quest_itemget");
                    } else {
                        st.playSound("ItemSound.quest_middle");
                        if (st.getQuestItemsCount(AQUAMARINE) == 10L && st.getQuestItemsCount(CHRYSOBERYL) == 10L) {
                            st.setCond(6);
                        }
                    }
                }
            }
        } else if (npcId == 20480 && cond == 11 && st.getQuestItemsCount(BAT_DIAGRAM) > 0L && st.getQuestItemsCount(STAR_DIAMOND) == 0L && Rnd.chance(50)) {
            st.takeItems(BAT_DIAGRAM, 1L);
            st.giveItems(STAR_DIAMOND, 1L);
            st.setCond(12);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
