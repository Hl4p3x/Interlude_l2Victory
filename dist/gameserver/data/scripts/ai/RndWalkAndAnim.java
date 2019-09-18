package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

public class RndWalkAndAnim extends DefaultAI {
    protected static final int PET_WALK_RANGE = 100;

    public RndWalkAndAnim(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isMoving()) {
            return false;
        }
        final int val = Rnd.get(100);
        if (val < 10) {
            randomWalk();
        } else if (val < 20) {
            actor.onRandomAnimation();
        }
        return false;
    }

    @Override
    protected boolean randomWalk() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return false;
        }
        final Location sloc = actor.getSpawnedLoc();
        final int x = sloc.x + Rnd.get(200) - 100;
        final int y = sloc.y + Rnd.get(200) - 100;
        final int z = GeoEngine.getHeight(x, y, sloc.z, actor.getGeoIndex());
        actor.setRunning();
        actor.moveToLocation(x, y, z, 0, true);
        return true;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }
}
