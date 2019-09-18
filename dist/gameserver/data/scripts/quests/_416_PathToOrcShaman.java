package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;

public class _416_PathToOrcShaman extends Quest {
    private static final int Hestui = 30585;
    private static final int HestuiTotemSpirit = 30592;
    private static final int SeerUmos = 30502;
    private static final int DudaMaraTotemSpirit = 30593;
    private static final int SeerMoira = 31979;
    private static final int GandiTotemSpirit = 32057;
    private static final int LeopardCarcass = 32090;
    private static final int FireCharm = 1616;
    private static final int KashaBearPelt = 1617;
    private static final int KashaBladeSpiderHusk = 1618;
    private static final int FieryEgg1st = 1619;
    private static final int HestuiMask = 1620;
    private static final int FieryEgg2nd = 1621;
    private static final int TotemSpiritClaw = 1622;
    private static final int TatarusLetterOfRecommendation = 1623;
    private static final int FlameCharm = 1624;
    private static final int GrizzlyBlood = 1625;
    private static final int BloodCauldron = 1626;
    private static final int SpiritNet = 1627;
    private static final int BoundDurkaSpirit = 1628;
    private static final int DurkaParasite = 1629;
    private static final int TotemSpiritBlood = 1630;
    private static final int MaskOfMedium = 1631;
    private static final int KashaBear = 20479;
    private static final int KashaBladeSpider = 20478;
    private static final int ScarletSalamander = 20415;
    private static final int GrizzlyBear = 20335;
    private static final int VenomousSpider = 20038;
    private static final int ArachnidTracker = 20043;
    private static final int QuestMonsterDurkaSpirit = 27056;
    private static final int QuestBlackLeopard = 27319;
    private static final int[][] DROPLIST_COND = {{1, 0, 20479, 1616, 1617, 1, 70, 1}, {1, 0, 20478, 1616, 1618, 1, 70, 1}, {1, 0, 20415, 1616, 1619, 1, 70, 1}, {6, 7, 20335, 1624, 1625, 3, 70, 1}};

    public _416_PathToOrcShaman() {
        super(false);
        addStartNpc(30585);
        addTalkId(30592, 30502, 30593, 31979, 32057, 32090);
        for (int i = 0; i < _416_PathToOrcShaman.DROPLIST_COND.length; ++i) {
            addKillId(_416_PathToOrcShaman.DROPLIST_COND[i][2]);
            addQuestItem(_416_PathToOrcShaman.DROPLIST_COND[i][4]);
        }
        addKillId(20038, 20043, 27056, 27319);
        addQuestItem(1616, 1620, 1621, 1622, 1623, 1624, 1626, 1627, 1628, 1629, 1630);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("tataru_zu_hestui_q0416_06.htm".equalsIgnoreCase(event)) {
            st.giveItems(1616, 1L);
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("hestui_totem_spirit_q0416_03.htm".equalsIgnoreCase(event)) {
            st.takeItems(1620, -1L);
            st.takeItems(1621, -1L);
            st.giveItems(1622, 1L);
            st.setCond(4);
        } else if ("tataru_zu_hestui_q0416_11.htm".equalsIgnoreCase(event)) {
            st.takeItems(1622, -1L);
            st.giveItems(1623, 1L);
            st.setCond(5);
        } else if ("tataru_zu_hestui_q0416_11c.htm".equalsIgnoreCase(event)) {
            st.takeItems(1622, -1L);
            st.setCond(12);
        } else if ("dudamara_totem_spirit_q0416_03.htm".equalsIgnoreCase(event)) {
            st.takeItems(1626, -1L);
            st.giveItems(1627, 1L);
            st.setCond(9);
        } else if ("seer_umos_q0416_07.htm".equalsIgnoreCase(event)) {
            st.takeItems(1630, -1L);
            if (st.getPlayer().getClassId().getLevel() == 1) {
                st.giveItems(1631, 1L);
                if (!st.getPlayer().getVarB("prof1")) {
                    st.getPlayer().setVar("prof1", "1", -1L);
                    st.addExpAndSp(3200L, 2600L);
                }
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("totem_spirit_gandi_q0416_02.htm".equalsIgnoreCase(event)) {
            st.setCond(14);
        } else if ("dead_leopard_q0416_04.htm".equalsIgnoreCase(event)) {
            st.setCond(18);
        } else if ("totem_spirit_gandi_q0416_05.htm".equalsIgnoreCase(event)) {
            st.setCond(21);
        }
        if ("QuestMonsterDurkaSpirit_Fail".equalsIgnoreCase(event)) {
            GameObjectsStorage.getNpcs(npcInstance -> npcInstance.getNpcId() == 27056).forEach(GameObject::deleteMe);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30585) {
            if (st.getQuestItemsCount(1631) != 0L) {
                htmltext = "seer_umos_q0416_04.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 0) {
                if (st.getPlayer().getClassId().getId() != 49) {
                    if (st.getPlayer().getClassId().getId() == 50) {
                        htmltext = "tataru_zu_hestui_q0416_02a.htm";
                    } else {
                        htmltext = "tataru_zu_hestui_q0416_02.htm";
                    }
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() < 18) {
                    htmltext = "tataru_zu_hestui_q0416_03.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "tataru_zu_hestui_q0416_01.htm";
                }
            } else if (cond == 1) {
                htmltext = "tataru_zu_hestui_q0416_07.htm";
            } else if (cond == 2) {
                htmltext = "tataru_zu_hestui_q0416_08.htm";
                st.takeItems(1617, -1L);
                st.takeItems(1618, -1L);
                st.takeItems(1619, -1L);
                st.takeItems(1616, -1L);
                st.giveItems(1620, 1L);
                st.giveItems(1621, 1L);
                st.setCond(3);
            } else if (cond == 3) {
                htmltext = "tataru_zu_hestui_q0416_09.htm";
            } else if (cond == 4) {
                htmltext = "tataru_zu_hestui_q0416_10.htm";
            } else if (cond == 5) {
                htmltext = "tataru_zu_hestui_q0416_12.htm";
            } else if (cond > 5) {
                htmltext = "tataru_zu_hestui_q0416_13.htm";
            }
        } else if (npcId == 30592) {
            if (cond == 3) {
                htmltext = "hestui_totem_spirit_q0416_01.htm";
            } else if (cond == 4) {
                htmltext = "hestui_totem_spirit_q0416_04.htm";
            }
        } else if (npcId == 30592 && st.getCond() > 0 && (st.getQuestItemsCount(1625) > 0L || st.getQuestItemsCount(1624) > 0L || st.getQuestItemsCount(1626) > 0L || st.getQuestItemsCount(1627) > 0L || st.getQuestItemsCount(1628) > 0L || st.getQuestItemsCount(1630) > 0L || st.getQuestItemsCount(1623) > 0L)) {
            htmltext = "hestui_totem_spirit_q0416_05.htm";
        } else if (npcId == 30502) {
            switch (cond) {
                case 5:
                    st.takeItems(1623, -1L);
                    st.giveItems(1624, 1L);
                    htmltext = "seer_umos_q0416_01.htm";
                    st.setCond(6);
                    break;
                case 6:
                    htmltext = "seer_umos_q0416_02.htm";
                    break;
                case 7:
                    st.takeItems(1625, -1L);
                    st.takeItems(1624, -1L);
                    st.giveItems(1626, 1L);
                    htmltext = "seer_umos_q0416_03.htm";
                    st.setCond(8);
                    break;
                case 8:
                    htmltext = "seer_umos_q0416_04.htm";
                    break;
                case 9:
                case 10:
                    htmltext = "seer_umos_q0416_05.htm";
                    break;
                case 11:
                    htmltext = "seer_umos_q0416_06.htm";
                    break;
            }
        } else if (npcId == 31979) {
            if (cond == 12) {
                htmltext = "seer_moirase_q0416_01.htm";
                st.setCond(13);
            } else if (cond > 12 && cond < 21) {
                htmltext = "seer_moirase_q0416_02.htm";
            } else if (cond == 21) {
                htmltext = "seer_moirase_q0416_03.htm";
                if (st.getPlayer().getClassId().getLevel() == 1) {
                    st.giveItems(1631, 1L);
                    if (!st.getPlayer().getVarB("prof1")) {
                        st.getPlayer().setVar("prof1", "1", -1L);
                        st.addExpAndSp(295862L, 18194L);
                    }
                }
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 32057) {
            if (cond == 13) {
                htmltext = "totem_spirit_gandi_q0416_01.htm";
            } else if (cond > 13 && cond < 20) {
                htmltext = "totem_spirit_gandi_q0416_03.htm";
            } else if (cond == 20) {
                htmltext = "totem_spirit_gandi_q0416_04.htm";
            }
        } else if (npcId == 32090) {
            if (cond <= 14) {
                htmltext = "dead_leopard_q0416_01a.htm";
            } else if (cond == 15) {
                htmltext = "dead_leopard_q0416_01.htm";
                st.setCond(16);
            } else if (cond == 16) {
                htmltext = "dead_leopard_q0416_01.htm";
            } else if (cond == 17) {
                htmltext = "dead_leopard_q0416_02.htm";
            } else if (cond == 18) {
                htmltext = "dead_leopard_q0416_05.htm";
            } else if (cond == 19) {
                htmltext = "dead_leopard_q0416_06.htm";
                st.setCond(20);
            } else {
                htmltext = "dead_leopard_q0416_06.htm";
            }
        } else if (npcId == 30593) {
            switch (cond) {
                case 8:
                    htmltext = "dudamara_totem_spirit_q0416_01.htm";
                    break;
                case 9:
                    htmltext = "dudamara_totem_spirit_q0416_04.htm";
                    break;
                case 10:
                    st.takeItems(1628, -1L);
                    st.giveItems(1630, 1L);
                    htmltext = "dudamara_totem_spirit_q0416_05.htm";
                    st.setCond(11);
                    break;
                case 11:
                    htmltext = "dudamara_totem_spirit_q0416_06.htm";
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        for (int i = 0; i < _416_PathToOrcShaman.DROPLIST_COND.length; ++i) {
            if (cond == _416_PathToOrcShaman.DROPLIST_COND[i][0] && npcId == _416_PathToOrcShaman.DROPLIST_COND[i][2] && (_416_PathToOrcShaman.DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(_416_PathToOrcShaman.DROPLIST_COND[i][3]) > 0L)) {
                if (_416_PathToOrcShaman.DROPLIST_COND[i][5] == 0) {
                    st.rollAndGive(_416_PathToOrcShaman.DROPLIST_COND[i][4], _416_PathToOrcShaman.DROPLIST_COND[i][7], (double) _416_PathToOrcShaman.DROPLIST_COND[i][6]);
                } else if (st.rollAndGive(_416_PathToOrcShaman.DROPLIST_COND[i][4], _416_PathToOrcShaman.DROPLIST_COND[i][7], _416_PathToOrcShaman.DROPLIST_COND[i][7], _416_PathToOrcShaman.DROPLIST_COND[i][5], (double) _416_PathToOrcShaman.DROPLIST_COND[i][6]) && _416_PathToOrcShaman.DROPLIST_COND[i][1] != cond && _416_PathToOrcShaman.DROPLIST_COND[i][1] != 0) {
                    st.setCond(_416_PathToOrcShaman.DROPLIST_COND[i][1]);
                }
            }
        }
        if (st.getQuestItemsCount(1617) != 0L && st.getQuestItemsCount(1618) != 0L && st.getQuestItemsCount(1619) != 0L) {
            st.setCond(2);
        } else if (cond == 9 && (npcId == 20038 || npcId == 20043)) {
            if (st.getQuestItemsCount(1629) < 8L) {
                st.giveItems(1629, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
            if ((st.getQuestItemsCount(1629) == 8L || (st.getQuestItemsCount(1629) >= 5L && Rnd.chance((double) (st.getQuestItemsCount(1629) * 10L)))) && GameObjectsStorage.getByNpcId(27056) == null) {
                st.takeItems(1629, -1L);
                st.addSpawn(27056);
                st.startQuestTimer("QuestMonsterDurkaSpirit_Fail", 300000L);
            }
        } else if (npcId == 27056) {
            st.cancelQuestTimer("QuestMonsterDurkaSpirit_Fail");
            GameObjectsStorage.getNpcs(npcInstance -> npcInstance.getNpcId() == 27056).forEach(GameObject::deleteMe);
            if (cond == 9) {
                st.takeItems(1627, -1L);
                st.takeItems(1629, -1L);
                st.giveItems(1628, 1L);
                st.playSound("ItemSound.quest_middle");
                st.setCond(10);
            }
        } else if (npcId == 27319) {
            if (cond == 14 && Rnd.chance(50)) {
                Functions.npcSayCustomMessage(GameObjectsStorage.getByNpcId(32090), new CustomMessage("quests._416_PathToOrcShaman.LeopardCarcass", st.getPlayer(), new Object[0]).toString(), st.getPlayer());
                st.setCond(15);
            } else if (cond == 16 && Rnd.chance(50)) {
                st.setCond(17);
            } else if (cond == 18 && Rnd.chance(50)) {
                st.setCond(19);
            }
        }
        return null;
    }
}
