package ai.isle_of_prayer;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.EFunction;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class FafurionKindred extends Fighter {
    private static final int DETRACTOR1 = 22270;
    private static final int DETRACTOR2 = 22271;
    private static final int Spirit_of_the_Lake = 2368;
    private static final int Water_Dragon_Scale = 9691;
    private static final int Water_Dragon_Claw = 9700;
    private static final FuncTemplate ft = new FuncTemplate(null, EFunction.Mul, Stats.HEAL_EFFECTIVNESS, 144, 0.0);

    ScheduledFuture<?> poisonTask;
    ScheduledFuture<?> despawnTask;
    List<SimpleSpawner> spawns;

    public FafurionKindred(final NpcInstance actor) {
        super(actor);
        spawns = new ArrayList<>();
        actor.addStatFunc(FafurionKindred.ft.getFunc(this));
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        spawns.clear();
        ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500L);
        ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500L);
        ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500L);
        ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500L);
        poisonTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PoisonTask(), 3000L, 3000L);
        despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 300000L);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        cleanUp();
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtSeeSpell(final Skill skill, final Creature caster) {
        final NpcInstance actor = getActor();
        if (actor.isDead() || skill == null) {
            return;
        }
        if (skill.getId() == 2368) {
            actor.setCurrentHp(actor.getCurrentHp() + 3000.0, false);
        }
        actor.getAggroList().remove(caster, true);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    private void cleanUp() {
        if (poisonTask != null) {
            poisonTask.cancel(false);
            poisonTask = null;
        }
        if (despawnTask != null) {
            despawnTask.cancel(false);
            despawnTask = null;
        }
        for (final SimpleSpawner spawn : spawns) {
            spawn.deleteAll();
        }
        spawns.clear();
    }

    private void dropItem(final NpcInstance actor, final int id, final int count) {
        final ItemInstance item = ItemFunctions.createItem(id);
        item.setCount((long) count);
        item.dropToTheGround(actor, Location.findPointToStay(actor, 100));
    }

    private class SpawnTask extends RunnableImpl {
        private final int _id;

        public SpawnTask(final int id) {
            _id = id;
        }

        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            final SimpleSpawner sp = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(_id));
            sp.setLoc(Location.findPointToStay(actor, 100, 120));
            sp.setRespawnDelay(30L, 40L);
            sp.doSpawn(true);
            spawns.add(sp);
        }
    }

    private class PoisonTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            actor.reduceCurrentHp(500.0, actor, null, true, false, true, false, false, false, false);
        }
    }

    private class DeSpawnTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            dropItem(actor, 9691, Rnd.get(1, 2));
            if (Rnd.chance(36)) {
                dropItem(actor, 9700, Rnd.get(1, 3));
            }
            cleanUp();
            actor.deleteMe();
        }
    }
}
