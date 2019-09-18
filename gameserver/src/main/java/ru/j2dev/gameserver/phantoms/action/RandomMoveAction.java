package ru.j2dev.gameserver.phantoms.action;

import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.utils.Location;

public class RandomMoveAction extends AbstractPhantomAction {
    @Override
    public long getDelay() {
        return 0L;
    }

    @Override
    public void run() {
        final Location loc = Location.findPointToStay(actor.getSpawnLoc(), PhantomConfig.randomMoveDistance, ReflectionManager.DEFAULT.getGeoIndex());
        actor.moveToLocation(loc, 50, true);
    }
}
