package quests;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.tables.PetDataTable.L2Pet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class _421_LittleWingAdventures extends Quest {
    private static final int Cronos = 30610;
    private static final int Mimyu = 30747;
    private static final int Fairy_Tree_of_Wind = 27185;
    private static final int Fairy_Tree_of_Star = 27186;
    private static final int Fairy_Tree_of_Twilight = 27187;
    private static final int Fairy_Tree_of_Abyss = 27188;
    private static final int Soul_of_Tree_Guardian = 27189;
    private static final int Dragonflute_of_Wind = L2Pet.HATCHLING_WIND.getControlItemId();
    private static final int Dragonflute_of_Star = L2Pet.HATCHLING_STAR.getControlItemId();
    private static final int Dragonflute_of_Twilight = L2Pet.HATCHLING_TWILIGHT.getControlItemId();
    private static final int Dragon_Bugle_of_Wind = L2Pet.STRIDER_WIND.getControlItemId();
    private static final int Dragon_Bugle_of_Star = L2Pet.STRIDER_STAR.getControlItemId();
    private static final int Dragon_Bugle_of_Twilight = L2Pet.STRIDER_TWILIGHT.getControlItemId();
    private static final int Fairy_Leaf = 4325;
    private static final int Min_Fairy_Tree_Attaks = 110;

    public _421_LittleWingAdventures() {
        super(false);
        addStartNpc(Cronos);
        addTalkId(Mimyu);
        addKillId(Fairy_Tree_of_Wind);
        addKillId(Fairy_Tree_of_Star);
        addKillId(Fairy_Tree_of_Twilight);
        addKillId(Fairy_Tree_of_Abyss);
        addAttackId(Fairy_Tree_of_Wind);
        addAttackId(Fairy_Tree_of_Star);
        addAttackId(Fairy_Tree_of_Twilight);
        addAttackId(Fairy_Tree_of_Abyss);
        addQuestItem(Fairy_Leaf);
    }

    private static ItemInstance GetDragonflute(final QuestState st) {
        final List<ItemInstance> Dragonflutes = st.getPlayer().getInventory().getItems().stream().filter(item -> item != null && (item.getItemId() == Dragonflute_of_Wind || item.getItemId() == Dragonflute_of_Star || item.getItemId() == Dragonflute_of_Twilight)).collect(Collectors.toList());
        if (Dragonflutes.isEmpty()) {
            return null;
        }
        if (Dragonflutes.size() == 1) {
            return Dragonflutes.get(0);
        }
        if (st.getState() == 1) {
            return null;
        }
        final int dragonflute_id = st.getInt("dragonflute");
        return Dragonflutes.stream().filter(item2 -> item2.getObjectId() == dragonflute_id).findFirst().orElse(null);
    }

    private static boolean HatchlingSummoned(final QuestState st, final boolean CheckObjID) {
        final Summon _pet = st.getPlayer().getPet();
        if (_pet == null) {
            return false;
        }
        if (CheckObjID) {
            final int dragonflute_id = st.getInt("dragonflute");
            if (dragonflute_id == 0) {
                return false;
            }
            if (_pet.getControlItemObjId() != dragonflute_id) {
                return false;
            }
        }
        final ItemInstance dragonflute = GetDragonflute(st);
        return dragonflute != null && PetDataTable.getControlItemId(_pet.getNpcId()) == dragonflute.getItemId();
    }

    private static boolean CheckTree(final QuestState st, final int Fairy_Tree_id) {
        return st.getInt(String.valueOf(Fairy_Tree_id)) == 1000000;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final ItemInstance dragonflute = GetDragonflute(st);
        final int dragonflute_id = st.getInt("dragonflute");
        final int cond = st.getCond();
        if ("30610_05.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if (("30747_03.htm".equalsIgnoreCase(event) || "30747_04.htm".equalsIgnoreCase(event)) && _state == 2 && cond == 1) {
            if (dragonflute == null) {
                return "noquest";
            }
            if (dragonflute.getObjectId() != dragonflute_id) {
                if (Rnd.chance(10)) {
                    st.takeItems(dragonflute.getItemId(), 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                }
                return "30747_00.htm";
            }
            if (!HatchlingSummoned(st, false)) {
                return "30747_04.htm".equalsIgnoreCase(event) ? "30747_04a.htm" : "30747_02.htm";
            }
            if ("30747_04.htm".equalsIgnoreCase(event)) {
                st.setCond(2);
                st.takeItems(Fairy_Leaf, -1L);
                st.giveItems(Fairy_Leaf, 4L);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final ItemInstance dragonflute = GetDragonflute(st);
        final int dragonflute_id = st.getInt("dragonflute");
        if (_state == 1) {
            if (npcId != Cronos) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 45) {
                st.exitCurrentQuest(true);
                return "30610_01.htm";
            }
            if (dragonflute == null) {
                st.exitCurrentQuest(true);
                return "30610_02.htm";
            }
            if (dragonflute.getEnchantLevel() < 55) {
                st.exitCurrentQuest(true);
                return "30610_03.htm";
            }
            st.setCond(0);
            st.set("dragonflute", String.valueOf(dragonflute.getObjectId()));
            return "30610_04.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            if (npcId != Cronos) {
                if (npcId == Mimyu) {
                    if (st.getQuestItemsCount(Dragon_Bugle_of_Wind) + st.getQuestItemsCount(Dragon_Bugle_of_Star) + st.getQuestItemsCount(Dragon_Bugle_of_Twilight) > 0L) {
                        return "30747_00b.htm";
                    }
                    if (dragonflute == null) {
                        return "noquest";
                    }
                    if (cond == 1) {
                        return "30747_01.htm";
                    }
                    if (cond == 2) {
                        if (!HatchlingSummoned(st, false)) {
                            return "30747_09.htm";
                        }
                        if (st.getQuestItemsCount(Fairy_Leaf) == 0L) {
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(true);
                            return "30747_11.htm";
                        }
                        return "30747_10.htm";
                    } else if (cond == 3) {
                        if (dragonflute.getObjectId() != dragonflute_id) {
                            return "30747_00a.htm";
                        }
                        if (st.getQuestItemsCount(Fairy_Leaf) > 0L) {
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(true);
                            return "30747_11.htm";
                        }
                        if (!CheckTree(st, Fairy_Tree_of_Wind) || !CheckTree(st, Fairy_Tree_of_Star) || !CheckTree(st, Fairy_Tree_of_Twilight) || !CheckTree(st, Fairy_Tree_of_Abyss)) {
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(true);
                            return "30747_11.htm";
                        }
                        if (st.getInt("welldone") == 0) {
                            if (!HatchlingSummoned(st, false)) {
                                return "30747_09.htm";
                            }
                            st.set("welldone", "1");
                            return "30747_12.htm";
                        } else {
                            if (HatchlingSummoned(st, false) || st.getPlayer().getPet() != null) {
                                return "30747_13a.htm";
                            }
                            dragonflute.setItemId(Dragon_Bugle_of_Wind + dragonflute.getItemId() - Dragonflute_of_Wind);
                            st.getPlayer().sendPacket(new InventoryUpdate().addModifiedItem(dragonflute));
                            st.playSound("ItemSound.quest_finish");
                            st.exitCurrentQuest(true);
                            return "30747_13.htm";
                        }
                    }
                }
                return "noquest";
            }
            if (dragonflute == null) {
                return "30610_02.htm";
            }
            return (dragonflute.getObjectId() == dragonflute_id) ? "30610_07.htm" : "30610_06.htm";
        }
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 2 || st.getCond() != 2 || !HatchlingSummoned(st, true) || st.getQuestItemsCount(Fairy_Leaf) == 0L) {
            return null;
        }
        final String npcID = String.valueOf(npc.getNpcId());
        final Integer attaked_times = st.getInt(npcID);
        if (CheckTree(st, npc.getNpcId())) {
            return null;
        }
        if (attaked_times > Min_Fairy_Tree_Attaks) {
            st.set(npcID, "1000000");
            Functions.npcSay(npc, "Give me the leaf!");
            st.takeItems(Fairy_Leaf, 1L);
            if (CheckTree(st, Fairy_Tree_of_Wind) && CheckTree(st, Fairy_Tree_of_Star) && CheckTree(st, Fairy_Tree_of_Twilight) && CheckTree(st, Fairy_Tree_of_Abyss)) {
                st.setCond(3);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else {
            st.set(npcID, String.valueOf(attaked_times + 1));
        }
        return null;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        ThreadPoolManager.getInstance().schedule(new GuardiansSpawner(npc, st, Rnd.get(15, 20)), 1000L);
        return null;
    }

    

    public class GuardiansSpawner extends RunnableImpl {
        private SimpleSpawner _spawn;
        private String agressor;
        private String agressors_pet;
        private List<String> agressors_party;
        private int tiks;

        public GuardiansSpawner(final NpcInstance npc, final QuestState st, final int _count) {
            _spawn = null;
            agressors_pet = null;
            agressors_party = null;
            tiks = 0;
            final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(Soul_of_Tree_Guardian);
            if (template == null) {
                return;
            }
            try {
                _spawn = new SimpleSpawner(template);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < _count; ++i) {
                _spawn.setLoc(Location.findPointToStay(npc, 50, 200));
                _spawn.setHeading(Rnd.get(0, 65535));
                _spawn.setAmount(1);
                _spawn.doSpawn(true);
                agressor = st.getPlayer().getName();
                if (st.getPlayer().getPet() != null) {
                    agressors_pet = st.getPlayer().getPet().getName();
                }
                if (st.getPlayer().getParty() != null) {
                    agressors_party = new ArrayList<>();
                    for (final Player _member : st.getPlayer().getParty().getPartyMembers()) {
                        if (!_member.equals(st.getPlayer())) {
                            agressors_party.add(_member.getName());
                        }
                    }
                }
            }
            _spawn.stopRespawn();
            updateAgression();
        }

        private void AddAgression(final Playable player, final int aggro) {
            if (player == null) {
                return;
            }
            for (final NpcInstance mob : _spawn.getAllSpawned()) {
                mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, aggro);
            }
        }

        private void updateAgression() {
            final Player _player = World.getPlayer(agressor);
            if (_player != null) {
                if (agressors_pet != null && _player.getPet() != null && _player.getPet().getName().equalsIgnoreCase(agressors_pet)) {
                    AddAgression(_player.getPet(), 10);
                }
                AddAgression(_player, 2);
            }
            if (agressors_party != null) {
                for (final String _agressor : agressors_party) {
                    AddAgression(World.getPlayer(_agressor), 1);
                }
            }
        }

        @Override
        public void runImpl() {
            if (_spawn == null) {
                return;
            }
            ++tiks;
            if (tiks < 600) {
                updateAgression();
                ThreadPoolManager.getInstance().schedule(this, 1000L);
                return;
            }
            _spawn.deleteAll();
        }
    }
}
