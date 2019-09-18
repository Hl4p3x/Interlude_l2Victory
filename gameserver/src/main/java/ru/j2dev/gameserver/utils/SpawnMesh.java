package ru.j2dev.gameserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;

public class SpawnMesh extends Polygon implements SpawnRange {
    private static final Logger LOG = LoggerFactory.getLogger(SpawnMesh.class);

    @Override
    public Location getRandomLoc(final int geoIndex) {
        final Location loc = new Location(0, 0, 0);
        final int width = getXmax() - getXmin();
        final int height = getYmax() - getYmin();
        final int dropZ = getZmin() + (getZmax() - getZmin()) / 2;
        final int heading = Location.getRandomHeading();
        final int maxAttempts = Math.max(2048, (height >> 4) * (width >> 4));
        int tempz;
        int attempts = 0;
        Label_0288:
        do {
            loc.setX(Rnd.get(getXmin(), getXmax()));
            loc.setY(Rnd.get(getYmin(), getYmax()));
            loc.setZ(dropZ);
            loc.setH(heading);
            if (isInside(loc.getX(), loc.getY())) {
                tempz = GeoEngine.getHeight(loc, geoIndex);
                if (getZmin() != getZmax()) {
                    if (tempz < getZmin()) {
                        continue;
                    }
                    if (tempz > getZmax()) {
                        continue;
                    }
                } else {
                    if (tempz < getZmin() - Config.MAX_Z_DIFF) {
                        continue;
                    }
                    if (tempz > getZmin() + Config.MAX_Z_DIFF) {
                        continue;
                    }
                }
                loc.setZ(tempz);
                final int geoX = loc.getX() - World.MAP_MIN_X >> 4;
                final int geoY = loc.getY() - World.MAP_MIN_Y >> 4;
                for (int gx = geoX - 1; gx <= geoX + 1; ++gx) {
                    for (int gy = geoY - 1; gy <= geoY + 1; ++gy) {
                        if (GeoEngine.NgetNSWE(gx, gy, tempz, geoIndex) != 15) {
                            continue Label_0288;
                        }
                    }
                }
                return loc;
            }
        } while (attempts++ < maxAttempts);
        if (Config.ALT_DEBUG_ENABLED) {
            LOG.warn("Cant find suitable point in " + toString() + " z[" + getZmin() + " " + getZmax() + "] last: " + loc);
        }
        return loc;
    }
}
