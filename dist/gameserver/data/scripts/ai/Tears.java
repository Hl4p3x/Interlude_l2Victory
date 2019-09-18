package ai;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class Tears extends DefaultAI {
    private static final int Water_Dragon_Scale = 2369;
    private static final int Tears_Copy = 25535;
    final Skill Invincible;
    final Skill Freezing;
    ScheduledFuture<?> spawnTask;
    ScheduledFuture<?> despawnTask;
    List<NpcInstance> spawns;
    private boolean _isUsedInvincible;
    private int _scale_count;
    private long _last_scale_time;

    public Tears(final NpcInstance actor) {
        super(actor);
        spawns = new ArrayList<>();
        _isUsedInvincible = false;
        _scale_count = 0;
        _last_scale_time = 0L;
        final TIntObjectHashMap<Skill> skills = getActor().getTemplate().getSkills();
        Invincible = skills.get(5420);
        Freezing = skills.get(5238);
    }

    @Override
    protected void onEvtSeeSpell(final Skill skill, final Creature caster) {
        final NpcInstance actor = getActor();
        if (actor.isDead() || skill == null || caster == null) {
            return;
        }
        if (System.currentTimeMillis() - _last_scale_time > 5000L) {
            _scale_count = 0;
        }
        if (skill.getId() == 2369) {
            ++_scale_count;
            _last_scale_time = System.currentTimeMillis();
        }
        final Player player = caster.getPlayer();
        if (player == null) {
            return;
        }
        int count = 1;
        final Party party = player.getParty();
        if (party != null) {
            count = party.getMemberCount();
        }
        if (_scale_count >= count) {
            _scale_count = 0;
            actor.getEffectList().stopEffect(Invincible);
        }
    }

    @Override
    protected boolean createNewTask() {
        clearTasks();
        final Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }
        final double distance = actor.getDistance(target);
        final double actor_hp_precent = actor.getCurrentHpPercents();
        final int rnd_per = Rnd.get(100);
        if (actor_hp_precent < 15.0 && !_isUsedInvincible) {
            _isUsedInvincible = true;
            addTaskBuff(actor, Invincible);
            Functions.npcSay(actor, "\u0413\u043e\u0442\u043e\u0432\u044c\u0442\u0435\u0441\u044c \u043a \u0441\u043c\u0435\u0440\u0442\u0438!!!");
            return true;
        }
        if (rnd_per < 5 && spawnTask == null && despawnTask == null) {
            actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 5441, 1, 3000, 0L));
            spawnTask = ThreadPoolManager.getInstance().schedule(new SpawnMobsTask(), 3000L);
            return true;
        }
        if (!actor.isAMuted() && rnd_per < 75) {
            return chooseTaskAndTargets(null, target, distance);
        }
        return chooseTaskAndTargets(Freezing, target, distance);
    }

    private void spawnMobs() {
        final NpcInstance actor = getActor();
        for (int i = 0; i < 9; ++i) {
            try {
                final Location pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getGeoIndex());
                final SimpleSpawner sp = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(25535));
                sp.setLoc(pos);
                sp.setReflection(actor.getReflection());
                final NpcInstance copy = sp.doSpawn(true);
                spawns.add(copy);
                final Creature hated = actor.getAggroList().getRandomHated();
                if (hated != null) {
                    copy.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final Location pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getReflectionId());
        actor.teleToLocation(pos);
        final Creature hated = actor.getAggroList().getRandomHated();
        if (hated != null) {
            actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));
        }
        if (despawnTask != null) {
            despawnTask.cancel(false);
        }
        despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 30000L);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    private class DeSpawnTask extends RunnableImpl {
        @Override
        public void runImpl() {
            for (final NpcInstance npc : spawns) {
                if (npc != null) {
                    npc.deleteMe();
                }
            }
            spawns.clear();
            despawnTask = null;
        }
    }

    private class SpawnMobsTask extends RunnableImpl {
        @Override
        public void runImpl() {
            spawnMobs();
            spawnTask = null;
        }
    }
}
