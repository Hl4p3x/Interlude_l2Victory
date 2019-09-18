package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _299_GatherIngredientsforPie extends Quest {
    private static final int Emily = 30620;
    private static final int Lara = 30063;
    private static final int Bright = 30466;
    private static final int Wasp_Worker = 20934;
    private static final int Wasp_Leader = 20935;
    private static final int Varnish = 1865;
    private static final int Fruit_Basket = 7136;
    private static final int Avellan_Spice = 7137;
    private static final int Honey_Pouch = 7138;
    private static final int Wasp_Worker_Chance = 55;
    private static final int Wasp_Leader_Chance = 70;
    private static final int Reward_Varnish_Chance = 50;

    public _299_GatherIngredientsforPie() {
        super(false);
        addStartNpc(Emily);
        addTalkId(Lara);
        addTalkId(Bright);
        addKillId(Wasp_Worker);
        addKillId(Wasp_Leader);
        addQuestItem(Fruit_Basket);
        addQuestItem(Avellan_Spice);
        addQuestItem(Honey_Pouch);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("emilly_q0299_0104.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("emilly_q0299_0201.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(Honey_Pouch) < 100L) {
                return "emilly_q0299_0202.htm";
            }
            st.takeItems(Honey_Pouch, -1L);
            st.setCond(3);
        } else if ("lars_q0299_0301.htm".equalsIgnoreCase(event) && _state == 2 && cond == 3) {
            st.giveItems(Avellan_Spice, 1L);
            st.setCond(4);
        } else if ("emilly_q0299_0401.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(Avellan_Spice) < 1L) {
                return "emilly_q0299_0402.htm";
            }
            st.takeItems(Avellan_Spice, -1L);
            st.setCond(5);
        } else if ("guard_bright_q0299_0501.htm".equalsIgnoreCase(event) && _state == 2 && cond == 5) {
            st.giveItems(Fruit_Basket, 1L);
            st.setCond(6);
        } else if ("emilly_q0299_0601.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(Fruit_Basket) < 1L) {
                return "emilly_q0299_0602.htm";
            }
            st.takeItems(Fruit_Basket, -1L);
            if (Rnd.chance(Reward_Varnish_Chance)) {
                st.giveItems(Varnish, 50L, true);
            } else {
                st.giveItems(57, 25000L);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != Emily) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() >= 34) {
                st.setCond(0);
                return "emilly_q0299_0101.htm";
            }
            st.exitCurrentQuest(true);
            return "emilly_q0299_0102.htm";
        } else {
            final int cond = st.getCond();
            if (npcId == Emily && _state == 2) {
                if (cond == 1 && st.getQuestItemsCount(Honey_Pouch) <= 99L) {
                    return "emilly_q0299_0106.htm";
                }
                if (cond == 2 && st.getQuestItemsCount(Honey_Pouch) >= 100L) {
                    return "emilly_q0299_0105.htm";
                }
                if (cond == 3 && st.getQuestItemsCount(Avellan_Spice) == 0L) {
                    return "emilly_q0299_0203.htm";
                }
                if (cond == 4 && st.getQuestItemsCount(Avellan_Spice) == 1L) {
                    return "emilly_q0299_0301.htm";
                }
                if (cond == 5 && st.getQuestItemsCount(Fruit_Basket) == 0L) {
                    return "emilly_q0299_0403.htm";
                }
                if (cond == 6 && st.getQuestItemsCount(Fruit_Basket) == 1L) {
                    return "emilly_q0299_0501.htm";
                }
            }
            if (npcId == Lara && _state == 2 && cond == 3) {
                return "lars_q0299_0201.htm";
            }
            if (npcId == Lara && _state == 2 && cond == 4) {
                return "lars_q0299_0302.htm";
            }
            if (npcId == Bright && _state == 2 && cond == 5) {
                return "guard_bright_q0299_0401.htm";
            }
            if (npcId == Bright && _state == 2 && cond == 5) {
                return "guard_bright_q0299_0502.htm";
            }
            return "noquest";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2 || qs.getCond() != 1 || qs.getQuestItemsCount(Honey_Pouch) >= 100L) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if ((npcId == Wasp_Worker && Rnd.chance(Wasp_Worker_Chance)) || (npcId == Wasp_Leader && Rnd.chance(Wasp_Leader_Chance))) {
            qs.giveItems(Honey_Pouch, 1L);
            if (qs.getQuestItemsCount(Honey_Pouch) < 100L) {
                qs.playSound("ItemSound.quest_itemget");
            } else {
                qs.setCond(2);
                qs.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }

    
}
