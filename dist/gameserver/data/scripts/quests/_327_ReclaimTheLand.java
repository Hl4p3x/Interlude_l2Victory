package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Drop;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _327_ReclaimTheLand extends Quest {
    private static final int Piotur = 30597;
    private static final int Iris = 30034;
    private static final int Asha = 30313;
    private static final int TUREK_DOGTAG = 1846;
    private static final int TUREK_MEDALLION = 1847;
    private static final int CLAY_URN_FRAGMENT = 1848;
    private static final int BRASS_TRINKET_PIECE = 1849;
    private static final int BRONZE_MIRROR_PIECE = 1850;
    private static final int JADE_NECKLACE_BEAD = 1851;
    private static final int ANCIENT_CLAY_URN = 1852;
    private static final int ANCIENT_BRASS_TIARA = 1853;
    private static final int ANCIENT_BRONZE_MIRROR = 1854;
    private static final int ANCIENT_JADE_NECKLACE = 1855;
    private static final int Exchange_Chance = 80;
    private static final Map<Integer, Drop> DROPLIST = new HashMap<>();
    private static final Map<Integer, Integer> EXP = new HashMap<>();

    public _327_ReclaimTheLand() {
        super(false);
        addStartNpc(Piotur);
        addTalkId(Iris);
        addTalkId(Asha);
        DROPLIST.put(20495, new Drop(1, 65535, 13).addItem(TUREK_MEDALLION));
        DROPLIST.put(20496, new Drop(1, 65535, 9).addItem(TUREK_DOGTAG));
        DROPLIST.put(20497, new Drop(1, 65535, 11).addItem(TUREK_MEDALLION));
        DROPLIST.put(20498, new Drop(1, 65535, 10).addItem(TUREK_DOGTAG));
        DROPLIST.put(20499, new Drop(1, 65535, 8).addItem(TUREK_DOGTAG));
        DROPLIST.put(20500, new Drop(1, 65535, 7).addItem(TUREK_DOGTAG));
        DROPLIST.put(20501, new Drop(1, 65535, 12).addItem(TUREK_MEDALLION));
        EXP.put(ANCIENT_CLAY_URN, 913);
        EXP.put(ANCIENT_BRASS_TIARA, 1065);
        EXP.put(ANCIENT_BRONZE_MIRROR, 1065);
        EXP.put(ANCIENT_JADE_NECKLACE, 1294);
        DROPLIST.keySet().forEach(this::addKillId);
        addQuestItem(TUREK_MEDALLION);
        addQuestItem(TUREK_DOGTAG);
    }

    private static boolean ExpReward(final QuestState st, final int item_id) {
        Integer exp = EXP.get(item_id);
        if (exp == null) {
            exp = 182;
        }
        final long exp_reward = st.getQuestItemsCount(item_id * exp);
        if (exp_reward == 0L) {
            return false;
        }
        st.takeItems(item_id, -1L);
        st.addExpAndSp(exp_reward, 0L);
        st.playSound("ItemSound.quest_middle");
        return true;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("piotur_q0327_03.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("piotur_q0327_06.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("trader_acellopy_q0327_02.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(CLAY_URN_FRAGMENT) >= 5L) {
            st.takeItems(CLAY_URN_FRAGMENT, 5L);
            if (!Rnd.chance(Exchange_Chance)) {
                return "trader_acellopy_q0327_10.htm";
            }
            st.giveItems(ANCIENT_CLAY_URN, 1L);
            st.playSound("ItemSound.quest_middle");
            return "trader_acellopy_q0327_03.htm";
        } else if ("trader_acellopy_q0327_04.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(BRASS_TRINKET_PIECE) >= 5L) {
            st.takeItems(BRASS_TRINKET_PIECE, 5L);
            if (!Rnd.chance(Exchange_Chance)) {
                return "trader_acellopy_q0327_10.htm";
            }
            st.giveItems(ANCIENT_BRASS_TIARA, 1L);
            st.playSound("ItemSound.quest_middle");
            return "trader_acellopy_q0327_05.htm";
        } else if ("trader_acellopy_q0327_06.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(BRONZE_MIRROR_PIECE) >= 5L) {
            st.takeItems(BRONZE_MIRROR_PIECE, 5L);
            if (!Rnd.chance(Exchange_Chance)) {
                return "trader_acellopy_q0327_10.htm";
            }
            st.giveItems(ANCIENT_BRONZE_MIRROR, 1L);
            st.playSound("ItemSound.quest_middle");
            return "trader_acellopy_q0327_07.htm";
        } else if ("trader_acellopy_q0327_08.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(JADE_NECKLACE_BEAD) >= 5L) {
            st.takeItems(JADE_NECKLACE_BEAD, 5L);
            if (!Rnd.chance(Exchange_Chance)) {
                return "trader_acellopy_q0327_09.htm";
            }
            st.giveItems(ANCIENT_JADE_NECKLACE, 1L);
            st.playSound("ItemSound.quest_middle");
            return "trader_acellopy_q0327_07.htm";
        } else if ("iris_q0327_03.htm".equalsIgnoreCase(event) && _state == 2) {
            if (!ExpReward(st, CLAY_URN_FRAGMENT)) {
                return "iris_q0327_02.htm";
            }
        } else if ("iris_q0327_04.htm".equalsIgnoreCase(event) && _state == 2) {
            if (!ExpReward(st, BRASS_TRINKET_PIECE)) {
                return "iris_q0327_02.htm";
            }
        } else if ("iris_q0327_05.htm".equalsIgnoreCase(event) && _state == 2) {
            if (!ExpReward(st, BRONZE_MIRROR_PIECE)) {
                return "iris_q0327_02.htm";
            }
        } else if ("iris_q0327_06.htm".equalsIgnoreCase(event) && _state == 2) {
            if (!ExpReward(st, JADE_NECKLACE_BEAD)) {
                return "iris_q0327_02.htm";
            }
        } else if ("iris_q0327_07.htm".equalsIgnoreCase(event) && _state == 2 && !ExpReward(st, ANCIENT_CLAY_URN) && !ExpReward(st, ANCIENT_BRASS_TIARA) && !ExpReward(st, ANCIENT_BRONZE_MIRROR) && !ExpReward(st, ANCIENT_JADE_NECKLACE)) {
            return "iris_q0327_02.htm";
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != Piotur) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 25) {
                st.exitCurrentQuest(true);
                return "piotur_q0327_01.htm";
            }
            st.setCond(0);
            return "piotur_q0327_02.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            if (npcId == Piotur) {
                final long reward = st.getQuestItemsCount(TUREK_DOGTAG) * 40L + st.getQuestItemsCount(TUREK_MEDALLION) * 50L;
                if (reward == 0L) {
                    return "piotur_q0327_04.htm";
                }
                st.takeItems(TUREK_DOGTAG, -1L);
                st.takeItems(TUREK_MEDALLION, -1L);
                st.giveItems(57, reward);
                st.playSound("ItemSound.quest_middle");
                return "piotur_q0327_05.htm";
            } else {
                if (npcId == Iris) {
                    return "iris_q0327_01.htm";
                }
                if (npcId == Asha) {
                    return "trader_acellopy_q0327_01.htm";
                }
                return "noquest";
            }
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        final Drop _drop = DROPLIST.get(npcId);
        if (_drop == null) {
            return null;
        }
        if (Rnd.chance(_drop.chance)) {
            final int n = Rnd.get(100);
            if (n < 25) {
                qs.giveItems(CLAY_URN_FRAGMENT, 1L);
            } else if (n < 50) {
                qs.giveItems(BRASS_TRINKET_PIECE, 1L);
            } else if (n < 75) {
                qs.giveItems(BRONZE_MIRROR_PIECE, 1L);
            } else {
                qs.giveItems(JADE_NECKLACE_BEAD, 1L);
            }
        }
        qs.giveItems(_drop.itemList[0], 1L);
        qs.playSound("ItemSound.quest_itemget");
        return null;
    }

    
}
