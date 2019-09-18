package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

public class MammonMerchant extends DefaultAI {
    private static final Location[] _teleports = {new Location(-52172, 78884, -4741, 0), new Location(-41350, 209876, -5087, 16384), new Location(-21657, 77164, -5173, 0), new Location(45029, 123802, -5413, 49152), new Location(83175, 208998, -5439, 0), new Location(111337, 173804, -5439, 0), new Location(118343, 132578, -4831, 0), new Location(172373, -17833, -4901, 0)};
    private static final long TELEPORT_PERIOD = 1800000L;
    private long _lastTeleport;

    public MammonMerchant(final NpcInstance actor) {
        super(actor);
        _lastTeleport = System.currentTimeMillis();
    }

    protected Location getRndTeleportLoc() {
        return _teleports[Rnd.get(_teleports.length)];
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance thisActor = getActor();
        if (thisActor.isDead()) {
            return true;
        }
        if (System.currentTimeMillis() - _lastTeleport < TELEPORT_PERIOD) {
            return false;
        }
        final Location loc = getRndTeleportLoc();
        if (thisActor.getLoc().equals(loc)) {
            return false;
        }
        ThreadPoolManager.getInstance().schedule(new Teleport(loc), 1000L);
        _lastTeleport = System.currentTimeMillis();
        return true;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

}
