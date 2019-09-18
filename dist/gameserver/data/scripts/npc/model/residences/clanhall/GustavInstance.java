package npc.model.residences.clanhall;

import npc.model.residences.SiegeGuardInstance;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SpawnExObject;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.NotifyAITask;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class GustavInstance extends SiegeGuardInstance implements _34SiegeGuard {
    private final AtomicBoolean _canDead;
    private Future<?> _teleportTask;

    public GustavInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _canDead = new AtomicBoolean();
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        _canDead.set(false);
        Functions.npcShoutCustomMessage(this, "clanhall.siege.GustavInstance.PREPARE_TO_DIE_FOREIGN_INVADERS_I_AM_GUSTAV_THE_ETERNAL_RULER_OF_THIS_FORTRESS_AND_I_HAVE_TAKEN_UP_MY_SWORD_TO_REPEL_THEE");
    }

    @Override
    public void onDeath(final Creature killer) {
        if (!_canDead.get()) {
            _canDead.set(true);
            setCurrentHp(1.0, true);
            World.getAroundCharacters(this).forEach(cha -> ThreadPoolManager.getInstance().execute(new NotifyAITask(cha, CtrlEvent.EVT_FORGET_OBJECT, this, null)));
            final ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
            if (siegeEvent == null) {
                return;
            }
            final SpawnExObject obj = siegeEvent.getFirstObject("boss");
            IntStream.range(0, 3).mapToObj(i -> obj.getSpawns().get(i).getFirstSpawned()).forEach(npc -> {
                Functions.npcSay(npc, ((_34SiegeGuard) npc).teleChatSay());
                npc.broadcastPacket(new MagicSkillUse(npc, npc, 4235, 1, 10000, 0L));
                _teleportTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        final Location loc = Location.findAroundPosition(177134, -18807, -2256, 50, 100, npc.getGeoIndex());
                        npc.teleToLocation(loc);
                        if (npc == GustavInstance.this) {
                            npc.reduceCurrentHp(npc.getCurrentHp(), npc, null, false, false, false, false, false, false, false);
                        }
                    }
                }, 10000L);
            });
        } else {
            if (_teleportTask != null) {
                _teleportTask.cancel(false);
                _teleportTask = null;
            }
            final SiegeEvent siegeEvent2 = getEvent(SiegeEvent.class);
            if (siegeEvent2 == null) {
                return;
            }
            siegeEvent2.processStep(getMostDamagedClan());
            super.onDeath(killer);
        }
    }

    public Clan getMostDamagedClan() {
        final ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
        Player temp = null;
        final Map<Player, Integer> damageMap = new HashMap<>();
        for (final HateInfo info : getAggroList().getPlayableMap().values()) {
            final Playable killer = (Playable) info.attacker;
            final int damage = info.damage;
            if (killer.isPet() || killer.isSummon()) {
                temp = killer.getPlayer();
            } else if (killer.isPlayer()) {
                temp = (Player) killer;
            }
            if (temp != null) {
                if (siegeEvent.getSiegeClan("attackers", temp.getClan()) == null) {
                    continue;
                }
                if (!damageMap.containsKey(temp)) {
                    damageMap.put(temp, damage);
                } else {
                    final int dmg = damageMap.get(temp) + damage;
                    damageMap.put(temp, dmg);
                }
            }
        }
        int mostDamage = 0;
        Player player = null;
        for (final Entry<Player, Integer> entry : damageMap.entrySet()) {
            final int damage2 = entry.getValue();
            final Player t = entry.getKey();
            if (damage2 > mostDamage) {
                mostDamage = damage2;
                player = t;
            }
        }
        return (player == null) ? null : player.getClan();
    }

    @Override
    public String teleChatSay() {
        return "This is unbelievable! Have I really been defeated? I shall return and take your head!";
    }

    @Override
    public boolean isEffectImmune() {
        return true;
    }
}
