package ru.j2dev.gameserver.utils;

import org.jdom2.Element;
import ru.j2dev.commons.geometry.Point2D;
import ru.j2dev.commons.geometry.Point3D;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;

import java.io.Serializable;

public class Location extends Point3D implements SpawnRange, Serializable {
    public static final Location EMPTY_LOCATION = new Location(0, 0, 0);
    public int h;

    public Location() {
    }

    public Location(final int x, final int y, final int z, final int heading) {
        super(x, y, z);
        h = heading;
    }

    public Location(final int x, final int y, final int z) {
        this(x, y, z, 0);
    }

    public Location(final GameObject obj) {
        this(obj.getX(), obj.getY(), obj.getZ(), obj.getHeading());
    }

    public static Location parseLoc(final String s) throws IllegalArgumentException {
        final String[] xyzh = s.split("[\\s,;]+");
        if (xyzh.length < 3) {
            throw new IllegalArgumentException("Can't parse location from string: " + s);
        }
        final int x = Integer.parseInt(xyzh[0]);
        final int y = Integer.parseInt(xyzh[1]);
        final int z = Integer.parseInt(xyzh[2]);
        final int h = (xyzh.length < 4) ? 0 : Integer.parseInt(xyzh[3]);
        return new Location(x, y, z, h);
    }

    public static Location parse(final Element element) {
        final int x = Integer.parseInt(element.getAttributeValue("x"));
        final int y = Integer.parseInt(element.getAttributeValue("y"));
        final int z = Integer.parseInt(element.getAttributeValue("z"));
        final int h = (element.getAttributeValue("h") == null) ? 0 : Integer.parseInt(element.getAttributeValue("h"));
        return new Location(x, y, z, h);
    }

    public static Location findFrontPosition(final GameObject obj, final GameObject obj2, final int radiusmin, final int radiusmax) {
        if (radiusmax == 0 || radiusmax < radiusmin) {
            return new Location(obj);
        }
        final double collision = obj.getColRadius() + obj2.getColRadius();
        int minangle = 0;
        int maxangle = 360;
        if (!obj.equals(obj2)) {
            final double angle = PositionUtils.calculateAngleFrom(obj, obj2);
            minangle = (int) angle - 45;
            maxangle = (int) angle + 45;
        }
        final Location pos = new Location();
        for (int i = 0; i < 100; ++i) {
            final int randomRadius = Rnd.get(radiusmin, radiusmax);
            final int randomAngle = Rnd.get(minangle, maxangle);
            pos.x = obj.getX() + (int) ((collision + randomRadius) * Math.cos(Math.toRadians(randomAngle)));
            pos.y = obj.getY() + (int) ((collision + randomRadius) * Math.sin(Math.toRadians(randomAngle)));
            pos.z = obj.getZ();
            final int tempz = GeoEngine.getHeight(pos.x, pos.y, pos.z, obj.getGeoIndex());
            if (Math.abs(pos.z - tempz) < 200 && GeoEngine.getNSWE(pos.x, pos.y, tempz, obj.getGeoIndex()) == 15) {
                pos.z = tempz;
                if (!obj.equals(obj2)) {
                    pos.h = PositionUtils.getHeadingTo(pos, obj2.getLoc());
                } else {
                    pos.h = obj.getHeading();
                }
                return pos;
            }
        }
        return new Location(obj);
    }

    public static Location findAroundPosition(final int x, final int y, final int z, final int radiusmin, final int radiusmax, final int geoIndex) {
        for (int i = 0; i < 100; ++i) {
            final Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
            final int tempz = GeoEngine.getHeight(pos.x, pos.y, pos.z, geoIndex);
            if (GeoEngine.canMoveToCoord(x, y, z, pos.x, pos.y, tempz, geoIndex) && GeoEngine.canMoveToCoord(pos.x, pos.y, tempz, x, y, z, geoIndex)) {
                pos.z = tempz;
                return pos;
            }
        }
        return new Location(x, y, z);
    }

    public static Location findAroundPosition(final Location loc, final int radius, final int geoIndex) {
        return findAroundPosition(loc.x, loc.y, loc.z, 0, radius, geoIndex);
    }

    public static Location findAroundPosition(final Location loc, final int radiusmin, final int radiusmax, final int geoIndex) {
        return findAroundPosition(loc.x, loc.y, loc.z, radiusmin, radiusmax, geoIndex);
    }

    public static Location findAroundPosition(final GameObject obj, final Location loc, final int radiusmin, final int radiusmax) {
        return findAroundPosition(loc.x, loc.y, loc.z, radiusmin, radiusmax, obj.getGeoIndex());
    }

    public static Location findAroundPosition(final GameObject obj, final int radiusmin, final int radiusmax) {
        return findAroundPosition(obj, obj.getLoc(), radiusmin, radiusmax);
    }

    public static Location findAroundPosition(final GameObject obj, final int radius) {
        return findAroundPosition(obj, 0, radius);
    }

    public static Location findPointToStay(final int x, final int y, final int z, final int radiusmin, final int radiusmax, final int geoIndex) {
        for (int i = 0; i < 100; ++i) {
            final Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
            final int tempz = GeoEngine.getHeight(pos.x, pos.y, pos.z, geoIndex);
            if (Math.abs(pos.z - tempz) < 200 && GeoEngine.getNSWE(pos.x, pos.y, tempz, geoIndex) == 15) {
                pos.z = tempz;
                return pos;
            }
        }
        return new Location(x, y, z);
    }

    public static Location findPointToStay(final Location loc, final int radius, final int geoIndex) {
        return findPointToStay(loc.x, loc.y, loc.z, 0, radius, geoIndex);
    }

    public static Location findPointToStay(final Location loc, final int radiusmin, final int radiusmax, final int geoIndex) {
        return findPointToStay(loc.x, loc.y, loc.z, radiusmin, radiusmax, geoIndex);
    }

    public static Location findPointToStay(final GameObject obj, final Location loc, final int radiusmin, final int radiusmax) {
        return findPointToStay(loc.x, loc.y, loc.z, radiusmin, radiusmax, obj.getGeoIndex());
    }

    public static Location findPointToStay(final GameObject obj, final int radiusmin, final int radiusmax) {
        return findPointToStay(obj, obj.getLoc(), radiusmin, radiusmax);
    }

    public static Location findPointToStay(final GameObject obj, final int radius) {
        return findPointToStay(obj, 0, radius);
    }

    public static Location coordsRandomize(final Location loc, final int radiusmin, final int radiusmax) {
        return coordsRandomize(loc.x, loc.y, loc.z, loc.h, radiusmin, radiusmax);
    }

    public static Location coordsRandomize(final int x, final int y, final int z, final int heading, final int radiusmin, final int radiusmax) {
        if (radiusmax == 0 || radiusmax < radiusmin) {
            return new Location(x, y, z, heading);
        }
        final int radius = Rnd.get(radiusmin, radiusmax);
        final double angle = Rnd.nextDouble() * 2.0 * Math.PI;
        return new Location((int) (x + radius * Math.cos(angle)), (int) (y + radius * Math.sin(angle)), z, heading);
    }

    public static Location findNearest(final Creature creature, final Location[] locs) {
        Location defloc = null;
        for (final Location loc : locs) {
            if (defloc == null) {
                defloc = loc;
            } else if (creature.getDistance(loc) < creature.getDistance(defloc)) {
                defloc = loc;
            }
        }
        return defloc;
    }

    public static int getRandomHeading() {
        return Rnd.get(65535);
    }

    public int getHeading() {
        return h;
    }

    public int getH() {
        return h;
    }

    public Location setH(final int h) {
        this.h = h;
        return this;
    }

    public Location changeZ(final int zDiff) {
        z += zDiff;
        return this;
    }

    public Location correctGeoZ() {
        z = GeoEngine.getHeight(x, y, z, 0);
        return this;
    }

    public Location correctGeoZ(final int refIndex) {
        z = GeoEngine.getHeight(x, y, z, refIndex);
        return this;
    }

    public Location setX(final int x) {
        this.x = x;
        return this;
    }

    public Location setY(final int y) {
        this.y = y;
        return this;
    }

    public Location setZ(final int z) {
        this.z = z;
        return this;
    }

    public Location set(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Location set(final int x, final int y, final int z, final int h) {
        set(x, y, z);
        this.h = h;
        return this;
    }

    public Location set(final Location loc) {
        x = loc.x;
        y = loc.y;
        z = loc.z;
        h = loc.h;
        return this;
    }

    public Location world2geo() {
        x = x - World.MAP_MIN_X >> 4;
        y = y - World.MAP_MIN_Y >> 4;
        return this;
    }

    public Location geo2world() {
        x = (x << 4) + World.MAP_MIN_X + 8;
        y = (y << 4) + World.MAP_MIN_Y + 8;
        return this;
    }

    public double distance(final Location loc) {
        return distance(loc.x, loc.y);
    }

    public double distance(final int x, final int y) {
        final long dx = this.x - x;
        final long dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distance3D(final Location loc) {
        return distance3D(loc.x, loc.y, loc.z);
    }

    public double distance3D(final int x, final int y, final int z) {
        final long dx = this.x - x;
        final long dy = this.y - y;
        final long dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public Location clone() {
        return new Location(x, y, z, h);
    }

    @Override
    public final String toString() {
        return x + "," + y + "," + z + "," + h;
    }

    public boolean isNull() {
        return x == 0 || y == 0 || z == 0;
    }

    public final String toXYZString() {
        return x + " " + y + " " + z;
    }

    public final String toXYZHString() {
        return x + " " + y + " " + z + " " + h;
    }

    public Location indent(final Location to, final int indent, final boolean includeZ) {
        if (indent <= 0) {
            return this;
        }
        final long dx = getX() - to.getX();
        final long dy = getY() - to.getY();
        final long dz = getZ() - to.getZ();
        final double distance = includeZ ? Math.sqrt(dx * dx + dy * dy + dz * dz) : Math.sqrt(dx * dx + dy * dy);
        if (distance <= indent) {
            set(to.getX(), to.getY(), to.getZ());
            return this;
        }
        if (distance >= 1.0) {
            final double cut = indent / distance;
            setX(getX() - (int) (dx * cut + 0.5));
            setY(getY() - (int) (dy * cut + 0.5));
            setZ(getZ() - (int) (dz * cut + 0.5));
        }
        return this;
    }

    public boolean equalsGeo(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Point2D) {
            final Point2D otherPoint2D = (Point2D) o;
            return otherPoint2D.x - World.MAP_MIN_X >> 4 == x - World.MAP_MIN_X >> 4 && otherPoint2D.y - World.MAP_MIN_Y >> 4 == y - World.MAP_MIN_Y >> 4 && (!(o instanceof Point3D) || ((Point3D) o).z == z);
        }
        return false;
    }

    public Location indent(final Location to, final int indent) {
        return indent(to, indent, true);
    }

    @Override
    public Location getRandomLoc(final int ref) {
        return this;
    }
}
