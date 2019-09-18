package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Drop;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _340_SubjugationofLizardmen extends Quest {
    private static final int WEITSZ = 30385;
    private static final int LEVIAN = 30037;
    private static final int ADONIUS = 30375;
    private static final int CHEST_OF_BIFRONS = 30989;
    private static final int LANGK_LIZARDMAN = 20008;
    private static final int LANGK_LIZARDMAN_SCOUT = 20010;
    private static final int LANGK_LIZARDMAN_WARRIOR = 20014;
    private static final int LANGK_LIZARDMAN_SHAMAN = 21101;
    private static final int LANGK_LIZARDMAN_LEADER = 20356;
    private static final int LANGK_LIZARDMAN_SENTINEL = 21100;
    private static final int LANGK_LIZARDMAN_LIEUTENANT = 20357;
    private static final int SERPENT_DEMON_BIFRONS = 25146;
    private static final int ROSARY = 4257;
    private static final int HOLY_SYMBOL = 4256;
    private static final int TRADE_CARGO = 4255;
    private static final int EVIL_SPIRIT_OF_DARKNESS = 7190;
    private static final Map<Integer, Drop> DROPLIST = new HashMap<>();

    public _340_SubjugationofLizardmen() {
        super(false);
        addStartNpc(WEITSZ);
        addTalkId(LEVIAN);
        addTalkId(ADONIUS);
        addTalkId(CHEST_OF_BIFRONS);
        DROPLIST.put(LANGK_LIZARDMAN, new Drop(1, 30, 30).addItem(TRADE_CARGO));
        DROPLIST.put(LANGK_LIZARDMAN_SCOUT, new Drop(1, 30, 33).addItem(TRADE_CARGO));
        DROPLIST.put(LANGK_LIZARDMAN_WARRIOR, new Drop(1, 30, 36).addItem(TRADE_CARGO));
        DROPLIST.put(LANGK_LIZARDMAN_SHAMAN, new Drop(3, 1, 12).addItem(HOLY_SYMBOL).addItem(ROSARY));
        DROPLIST.put(LANGK_LIZARDMAN_LEADER, new Drop(3, 1, 12).addItem(HOLY_SYMBOL).addItem(ROSARY));
        DROPLIST.put(LANGK_LIZARDMAN_SENTINEL, new Drop(3, 1, 12).addItem(HOLY_SYMBOL).addItem(ROSARY));
        DROPLIST.put(LANGK_LIZARDMAN_LIEUTENANT, new Drop(3, 1, 12).addItem(HOLY_SYMBOL).addItem(ROSARY));
        addKillId(SERPENT_DEMON_BIFRONS);
        DROPLIST.keySet().forEach(this::addKillId);
        addQuestItem(TRADE_CARGO);
        addQuestItem(HOLY_SYMBOL);
        addQuestItem(ROSARY);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("30385-4.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30385-6.htm".equalsIgnoreCase(event) && _state == 2 && cond == 1 && st.getQuestItemsCount(TRADE_CARGO) >= 30L) {
            st.setCond(2);
            st.takeItems(TRADE_CARGO, -1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30375-2.htm".equalsIgnoreCase(event) && _state == 2 && cond == 2) {
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("30989-2.htm".equalsIgnoreCase(event) && _state == 2 && cond == 5) {
            st.setCond(6);
            st.giveItems(EVIL_SPIRIT_OF_DARKNESS, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30037-4.htm".equalsIgnoreCase(event) && _state == 2 && cond == 6 && st.getQuestItemsCount(EVIL_SPIRIT_OF_DARKNESS) > 0L) {
            st.setCond(7);
            st.takeItems(EVIL_SPIRIT_OF_DARKNESS, -1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("30385-10.htm".equalsIgnoreCase(event) && _state == 2 && cond == 7) {
            st.giveItems(57, 14700L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("30385-7.htm".equalsIgnoreCase(event) && _state == 2 && cond == 1 && st.getQuestItemsCount(TRADE_CARGO) >= 30L) {
            st.takeItems(TRADE_CARGO, -1L);
            st.giveItems(57, 4090L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != WEITSZ) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 17) {
                st.exitCurrentQuest(true);
                return "30385-1.htm";
            }
            st.setCond(0);
            return "30385-2.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final int cond = st.getCond();
            if (npcId == WEITSZ && cond == 1) {
                return (st.getQuestItemsCount(TRADE_CARGO) < 30L) ? "30385-8.htm" : "30385-5.htm";
            }
            if (npcId == WEITSZ && cond == 2) {
                return "30385-11.htm";
            }
            if (npcId == WEITSZ && cond == 7) {
                return "30385-9.htm";
            }
            if (npcId == ADONIUS && cond == 2) {
                return "30375-1.htm";
            }
            if (npcId == ADONIUS && cond == 3) {
                if (st.getQuestItemsCount(ROSARY) == 0L || st.getQuestItemsCount(HOLY_SYMBOL) == 0L) {
                    return "30375-4.htm";
                }
                st.takeItems(ROSARY, -1L);
                st.takeItems(HOLY_SYMBOL, -1L);
                st.playSound("ItemSound.quest_middle");
                st.setCond(4);
                return "30375-3.htm";
            } else {
                if (npcId == ADONIUS && cond == 4) {
                    return "30375-5.htm";
                }
                if (npcId == LEVIAN && cond == 4) {
                    st.setCond(5);
                    st.playSound("ItemSound.quest_middle");
                    return "30037-1.htm";
                }
                if (npcId == LEVIAN && cond == 5) {
                    return "30037-2.htm";
                }
                if (npcId == LEVIAN && cond == 6 && st.getQuestItemsCount(EVIL_SPIRIT_OF_DARKNESS) > 0L) {
                    return "30037-3.htm";
                }
                if (npcId == LEVIAN && cond == 7) {
                    return "30037-5.htm";
                }
                if (npcId == CHEST_OF_BIFRONS && cond == 5) {
                    return "30989-1.htm";
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
        if (npcId == SERPENT_DEMON_BIFRONS) {
            qs.addSpawn(CHEST_OF_BIFRONS);
            return null;
        }
        final Drop _drop = DROPLIST.get(npcId);
        if (_drop == null) {
            return null;
        }
        final int cond = qs.getCond();
        for (final int item_id : _drop.itemList) {
            final long _count = qs.getQuestItemsCount(item_id);
            if (cond == _drop.condition && _count < _drop.maxcount && Rnd.chance(_drop.chance)) {
                qs.giveItems(item_id, 1L);
                if (_count + 1L == _drop.maxcount) {
                    qs.playSound("ItemSound.quest_middle");
                } else {
                    qs.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }

    
}
