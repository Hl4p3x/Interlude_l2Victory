package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;

public class FlyingSantaAI extends DefaultAI {
    private static final int ActorHeight = 100;
    private static final long ForceDeleteTime = 300000L;
    private static final double RandomFraseChance = 50.0;
    private static final String[] RandomFrase = {"\u0425\u043e-\u0425\u043e-\u0425\u043e", "\u0421 \u041f\u0440\u0430\u0437\u0434\u043d\u0438\u043a\u0430\u043c\u0438 \u0412\u0430\u0441!", "\u041b\u043e\u0432\u0438\u0442\u0435 \u043f\u043e\u0434\u0430\u0440\u043a\u0438 :D"};
    private static final double ItemDropChance = 50.0;
    private static final int[] ItemDropIds = {57};
    private static final int[] ItemDropCount = {1};
    private static final Location[][] _paths = {{new Location(82632, 149128, -3472), new Location(81051, 149288, -3472), new Location(81051, 148062, -3472), new Location(82904, 148056, -3472)}, {new Location(148408, 27928, -2272), new Location(146504, 27928, -2272)}, {new Location(146400, -54903, -2807), new Location(147694, -56555, -2807), new Location(148613, -55572, -2807)}};

    private int _curr_path_idx;
    private int _curr_point_idx;
    private ScheduledFuture<?> _delete_task;

    public FlyingSantaAI(final NpcInstance actor) {
        super(actor);
    }

    private static int getNearestPath(final Location curr) {
        int path_idx = -1;
        double curdistsq = Double.MAX_VALUE;
        for (int pathes = 0; pathes < FlyingSantaAI._paths.length; ++pathes) {
            for (int pnt_idx = 0; pnt_idx < FlyingSantaAI._paths[pathes].length; ++pnt_idx) {
                final double distsq = curr.distance(FlyingSantaAI._paths[pathes][pnt_idx]);
                if (distsq < curdistsq) {
                    curdistsq = distsq;
                    path_idx = pathes;
                }
            }
        }
        return (path_idx != -1) ? path_idx : 0;
    }

    @Override
    protected void onEvtSpawn() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }
        actor.setCollisionHeight(100.0);
        actor.setWalking();
        _curr_path_idx = getNearestPath(actor.getSpawnedLoc());
        _curr_point_idx = 0;
        addTaskMove(FlyingSantaAI._paths[_curr_path_idx][_curr_point_idx], false);
        _delete_task = ThreadPoolManager.getInstance().schedule(new UnspawnTask(actor), 300000L);
        doTask();
    }

    private boolean moveNext() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return false;
        }
        ++_curr_point_idx;
        if (_curr_point_idx < FlyingSantaAI._paths[_curr_path_idx].length) {
            if (Rnd.chance(50.0)) {
                Functions.npcShout(actor, FlyingSantaAI.RandomFrase[Rnd.get(FlyingSantaAI.RandomFrase.length)]);
            }
            if (Rnd.chance(50.0)) {
                final int item_idx = Rnd.get(FlyingSantaAI.ItemDropIds.length);
                final ItemInstance item = ItemFunctions.createItem(FlyingSantaAI.ItemDropIds[item_idx]);
                item.setCount((long) FlyingSantaAI.ItemDropCount[item_idx]);
                item.dropToTheGround(actor, Location.coordsRandomize(actor.getLoc(), 10, 50));
            }
            final Location next = FlyingSantaAI._paths[_curr_path_idx][_curr_point_idx];
            addTaskMove(next, false);
            return doTask();
        }
        if (_delete_task != null) {
            _delete_task.cancel(true);
            _delete_task = null;
        }
        actor.decayMe();
        actor.deleteMe();
        return false;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor == null || actor.isDead()) {
            return true;
        }
        if (_def_think) {
            doTask();
            return true;
        }
        return moveNext();
    }

    @Override
    public boolean isGlobalAI() {
        return false;
    }

    @Override
    protected void onEvtArrived() {
        super.onEvtArrived();
        moveNext();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }

    private class UnspawnTask implements Runnable {
        private final WeakReference<NpcInstance> _actor_ref;

        public UnspawnTask(final NpcInstance actor) {
            _actor_ref = new WeakReference<>(actor);
        }

        @Override
        public void run() {
            if (_actor_ref != null && _actor_ref.get() != null) {
                _actor_ref.get().decayMe();
                _actor_ref.get().deleteMe();
            }
        }
    }
}
