package quests;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class _625_TheFinestIngredientsPart2 extends Quest {
    private static final int Jeremy = 31521;
    private static final int Yetis_Table = 31542;
    private static final int RB_Icicle_Emperor_Bumbalump = 25296;
    private static final int Soy_Sauce_Jar = 7205;
    private static final int Food_for_Bumbalump = 7209;
    private static final int Special_Yeti_Meat = 7210;
    private static final int Reward_First = 4589;
    private static final int Reward_Last = 4594;

    public _625_TheFinestIngredientsPart2() {
        super(true);
        addStartNpc(Jeremy);
        addTalkId(Yetis_Table);
        addKillId(RB_Icicle_Emperor_Bumbalump);
        addQuestItem(Food_for_Bumbalump, Special_Yeti_Meat);
    }

    private static boolean BumbalumpSpawned() {
        return GameObjectsStorage.getByNpcId(RB_Icicle_Emperor_Bumbalump) != null;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final int cond = st.getCond();
        if ("jeremy_q0625_0104.htm".equalsIgnoreCase(event) && _state == 1) {
            if (st.getQuestItemsCount(Soy_Sauce_Jar) == 0L) {
                st.exitCurrentQuest(true);
                return "jeremy_q0625_0102.htm";
            }
            st.setState(2);
            st.setCond(1);
            st.takeItems(Soy_Sauce_Jar, 1L);
            st.giveItems(Food_for_Bumbalump, 1L);
            st.playSound("ItemSound.quest_accept");
        } else if ("jeremy_q0625_0301.htm".equalsIgnoreCase(event) && _state == 2 && cond == 3) {
            st.exitCurrentQuest(true);
            if (st.getQuestItemsCount(Special_Yeti_Meat) == 0L) {
                return "jeremy_q0625_0302.htm";
            }
            st.takeItems(Special_Yeti_Meat, 1L);
            st.giveItems(Rnd.get(Reward_First, Reward_Last), 5L, true);
        } else if ("yetis_table_q0625_0201.htm".equalsIgnoreCase(event) && _state == 2 && cond == 1) {
            if (ServerVariables.getLong(_625_TheFinestIngredientsPart2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                return "yetis_table_q0625_0204.htm";
            }
            if (st.getQuestItemsCount(Food_for_Bumbalump) == 0L) {
                return "yetis_table_q0625_0203.htm";
            }
            if (BumbalumpSpawned()) {
                return "yetis_table_q0625_0202.htm";
            }
            st.takeItems(Food_for_Bumbalump, 1L);
            st.setCond(2);
            ThreadPoolManager.getInstance().schedule(new BumbalumpSpawner(), 1000L);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != Jeremy) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 73) {
                st.exitCurrentQuest(true);
                return "jeremy_q0625_0103.htm";
            }
            if (st.getQuestItemsCount(Soy_Sauce_Jar) == 0L) {
                st.exitCurrentQuest(true);
                return "jeremy_q0625_0102.htm";
            }
            st.setCond(0);
            return "jeremy_q0625_0101.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final int cond = st.getCond();
            if (npcId == Jeremy) {
                if (cond == 1) {
                    return "jeremy_q0625_0105.htm";
                }
                if (cond == 2) {
                    return "jeremy_q0625_0202.htm";
                }
                if (cond == 3) {
                    return "jeremy_q0625_0201.htm";
                }
            }
            if (npcId == Yetis_Table) {
                if (ServerVariables.getLong(_625_TheFinestIngredientsPart2.class.getSimpleName(), 0L) + 10800000L > System.currentTimeMillis()) {
                    return "yetis_table_q0625_0204.htm";
                }
                if (cond == 1) {
                    return "yetis_table_q0625_0101.htm";
                }
                if (cond == 2) {
                    if (BumbalumpSpawned()) {
                        return "yetis_table_q0625_0202.htm";
                    }
                    ThreadPoolManager.getInstance().schedule(new BumbalumpSpawner(), 1000L);
                    return "yetis_table_q0625_0201.htm";
                } else if (cond == 3) {
                    return "yetis_table_q0625_0204.htm";
                }
            }
            return "noquest";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 || st.getCond() == 2) {
            if (st.getQuestItemsCount(Food_for_Bumbalump) > 0L) {
                st.takeItems(Food_for_Bumbalump, 1L);
            }
            st.giveItems(Special_Yeti_Meat, 1L);
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }

    

    private static class DeathListener implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            ServerVariables.set(_625_TheFinestIngredientsPart2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
        }
    }

    public class BumbalumpSpawner extends RunnableImpl {
        private SimpleSpawner _spawn;
        private int tiks;

        public BumbalumpSpawner() {
            _spawn = null;
            tiks = 0;
            if (BumbalumpSpawned()) {
                return;
            }
            final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(RB_Icicle_Emperor_Bumbalump);
            if (template == null) {
                return;
            }
            try {
                _spawn = new SimpleSpawner(template);
            } catch (Exception E) {
                return;
            }
            _spawn.setLocx(158240);
            _spawn.setLocy(-121536);
            _spawn.setLocz(-2253);
            _spawn.setHeading(Rnd.get(0, 65535));
            _spawn.setAmount(1);
            _spawn.doSpawn(true);
            _spawn.stopRespawn();
            for (final NpcInstance _npc : _spawn.getAllSpawned()) {
                _npc.addListener((Listener) new DeathListener());
            }
        }

        public void Say(final String test) {
            for (final NpcInstance _npc : _spawn.getAllSpawned()) {
                Functions.npcSay(_npc, test);
            }
        }

        @Override
        public void runImpl() {
            if (_spawn == null) {
                return;
            }
            if (tiks == 0) {
                Say("I will crush you!");
            }
            if (tiks < 1200 && BumbalumpSpawned()) {
                ++tiks;
                if (tiks == 1200) {
                    Say("May the gods forever condemn you! Your power weakens!");
                }
                ThreadPoolManager.getInstance().schedule(this, 1000L);
                return;
            }
            _spawn.deleteAll();
        }
    }
}
