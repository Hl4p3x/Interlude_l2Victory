package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;
import ru.j2dev.gameserver.utils.Location;

public class RndTeleportFighter extends Fighter {
    private long _lastTeleport;

    public RndTeleportFighter(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean maybeMoveToHome() {
        final NpcInstance actor = getActor();
        if (System.currentTimeMillis() - _lastTeleport < 10000L) {
            return false;
        }
        final boolean randomWalk = actor.hasRandomWalk();
        final Location sloc = actor.getSpawnedLoc();
        if (sloc == null) {
            return false;
        }
        if (randomWalk && (!Config.RND_WALK || Rnd.chance(Config.RND_WALK_RATE))) {
            return false;
        }
        if (!randomWalk && actor.isInRangeZ(sloc, (long) Config.MAX_DRIFT_RANGE)) {
            return false;
        }
        final int x = sloc.x + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
        final int y = sloc.y + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
        final int z = GeoEngine.getHeight(x, y, sloc.z, actor.getGeoIndex());
        if (sloc.z - z > 64) {
            return false;
        }
        final SpawnRange spawnRange = actor.getSpawnRange();
        boolean isInside = true;
        if (spawnRange != null && spawnRange instanceof Territory) {
            isInside = ((Territory) spawnRange).isInside(x, y);
        }
        if (isInside) {
            actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 500, 0L));
            ThreadPoolManager.getInstance().schedule(new Teleport(new Location(x, y, z)), 500L);
            _lastTeleport = System.currentTimeMillis();
        }
        return isInside;
    }
}
