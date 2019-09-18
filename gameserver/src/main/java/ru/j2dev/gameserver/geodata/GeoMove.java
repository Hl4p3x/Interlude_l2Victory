package ru.j2dev.gameserver.geodata;


import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.pathfind.PathFind;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: Diamond
 * @Date: 27/04/2009
 */
public class GeoMove {
    private static List<Location> findPath(final int fromX, final int fromY, int fromZ, final int toX, final int toY, int toZ, final boolean isPlayable, final int geoIndex) {
        if (Math.abs(fromZ - toZ) > 256) {
            return Collections.emptyList();
        }
        fromZ = GeoEngine.getHeight(fromX, fromY, fromZ, geoIndex);
        toZ = GeoEngine.getHeight(toX, toY, toZ, geoIndex);
        final PathFind n = new PathFind(fromX, fromY, fromZ, toX, toY, toZ, isPlayable, geoIndex);
        if (n.getPath() == null || n.getPath().isEmpty()) {
            return Collections.emptyList();
        }
        final List<Location> targetRecorder = new ArrayList<>(n.getPath().size() + 2);
        targetRecorder.add(new Location(fromX, fromY, fromZ));
        n.getPath().stream().map(Location::geo2world).forEach(targetRecorder::add);
        targetRecorder.add(new Location(toX, toY, toZ));
        if (Config.PATH_CLEAN) {
            pathClean(targetRecorder, geoIndex);
        }
        return targetRecorder;
    }

    public static List<List<Location>> findMovePath(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ, final boolean isPlayable, final int geoIndex) {
        return getNodePath(findPath(fromX, fromY, fromZ, toX, toY, toZ, isPlayable, geoIndex), geoIndex);
    }

    private static List<List<Location>> getNodePath(final List<Location> path, final int geoIndex) {
        final int size = path.size();
        if (size <= 1) {
            return Collections.emptyList();
        }
        final List<List<Location>> result = new ArrayList<>(size);
        for (int i = 1; i < size; ++i) {
            final Location p2 = path.get(i);
            final Location p3 = path.get(i - 1);
            final List<Location> moveList = GeoEngine.MoveList(p3.x, p3.y, p3.z, p2.x, p2.y, geoIndex, true);
            if (moveList == null) {
                return Collections.emptyList();
            }
            if (!moveList.isEmpty()) {
                result.add(moveList);
            }
        }
        return result;
    }

    public static List<Location> constructMoveList(Location begin, Location end) {
        begin = begin.world2geo();
        end = end.world2geo();
        final int diff_x = end.x - begin.x;
        final int diff_y = end.y - begin.y;
        final int diff_z = end.z - begin.z;
        final int dx = Math.abs(diff_x);
        final int dy = Math.abs(diff_y);
        final int dz = Math.abs(diff_z);
        final float steps = Math.max(Math.max(dx, dy), dz);
        if (steps == 0.0f) {
            return Collections.emptyList();
        }
        final float step_x = diff_x / steps;
        final float step_y = diff_y / steps;
        final float step_z = diff_z / steps;
        float next_x = begin.x;
        float next_y = begin.y;
        float next_z = begin.z;
        final List<Location> result = new ArrayList<>((int) steps + 1);
        result.add(new Location(begin.x, begin.y, begin.z));
        for (int i = 0; i < steps; ++i) {
            next_x += step_x;
            next_y += step_y;
            next_z += step_z;
            result.add(new Location((int) (next_x + 0.5f), (int) (next_y + 0.5f), (int) (next_z + 0.5f)));
        }
        return result;
    }

    private static void pathClean(final List<Location> path, final int geoIndex) {
        int size = path.size();
        if (size > 2) {
            for (int i = 2; i < size; ++i) {
                final Location p3 = path.get(i);
                final Location p4 = path.get(i - 1);
                final Location p5 = path.get(i - 2);
                if (p5.equals(p4) || p3.equals(p4) || IsPointInLine(p5, p4, p3)) {
                    path.remove(i - 1);
                    --size;
                    i = Math.max(2, i - 2);
                }
            }
        }
        for (int current = 0; current < path.size() - 2; ++current) {
            final Location one = path.get(current);
            for (int sub = current + 2; sub < path.size(); ++sub) {
                final Location two = path.get(sub);
                if (one.equals(two) || GeoEngine.canMoveWithCollision(one.x, one.y, one.z, two.x, two.y, two.z, geoIndex)) {
                    while (current + 1 < sub) {
                        path.remove(current + 1);
                        --sub;
                    }
                }
            }
        }
    }

    private static boolean IsPointInLine(final Location p1, final Location p2, final Location p3) {
        return (p1.x == p3.x && p3.x == p2.x) || (p1.y == p3.y && p3.y == p2.y) || (p1.x - p2.x) * (p1.y - p2.y) == (p2.x - p3.x) * (p2.y - p3.y);
    }

    public static List<Location> applyGeoIndent(final List<Location> points, final int geoIndent) {
        if (geoIndent <= 0) {
            return points;
        }
        final long dx = points.get(points.size() - 1).getX() - points.get(0).getX();
        final long dy = points.get(points.size() - 1).getY() - points.get(0).getY();
        final long dz = points.get(points.size() - 1).getZ() - points.get(0).getZ();
        final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance <= geoIndent) {
            final Location point = points.get(0);
            points.clear();
            points.add(point);
            return points;
        }
        if (distance >= 1.0) {
            final double cut = geoIndent / distance;
            for (int num = (int) (points.size() * cut + 0.5), i = 1; i <= num && points.size() > 0; ++i) {
                points.remove(points.size() - 1);
            }
        }
        return points;
    }

    public static List<Location> straightLineGeoPath(final Location src, final Location dst) {
        final int diffX = dst.getX() - src.getX();
        final int diffY = dst.getY() - src.getY();
        final int diffZ = dst.getZ() - src.getZ();
        final int dx = Math.abs(diffX);
        final int dy = Math.abs(diffY);
        final int dz = Math.abs(diffZ);
        final float steps = Math.max(Math.max(dx, dy), dz);
        if (steps == 0.0f) {
            return Collections.emptyList();
        }
        final float stepX = diffX / steps;
        final float stepY = diffY / steps;
        final float stepZ = diffZ / steps;
        float nextX = src.getX();
        float nextY = src.getY();
        float nextZ = src.getZ();
        final List<Location> straightGeoLine = new ArrayList<>((int) steps + 1);
        straightGeoLine.add(new Location(src.getX(), src.getY(), src.getZ()));
        for (int i = 0; i < steps; ++i) {
            nextX += stepX;
            nextY += stepY;
            nextZ += stepZ;
            straightGeoLine.add(new Location((int) (nextX + 0.5f), (int) (nextY + 0.5f), (int) (nextZ + 0.5f)));
        }
        return straightGeoLine;
    }

    public static Location getIntersectPoint(final Location actorLoc, final Location targetLoc, final int targetSpd, final int timeMs) {
        if (timeMs == 0 || targetSpd == 0 || !PositionUtils.isFacing(actorLoc, targetLoc, 90)) {
            return new Location(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ());
        }
        final double angle = PositionUtils.convertHeadingToDegree(targetLoc.getHeading());
        final double radian = Math.toRadians(angle - 90.0);
        final double range = timeMs * (targetSpd / 1000.0);
        return new Location((int) (targetLoc.getX() - range * Math.sin(radian)), (int) (targetLoc.getY() + range * Math.cos(radian)), targetLoc.getZ());
    }

    public static List<Location> buildGeoLine(final Location geoFrom, final Location geoTo, final int geoIndex, final boolean air, final boolean water, final int waterZ, final int indent) {
        if (geoFrom.equals(geoTo)) {
            return Collections.emptyList();
        }
        final Location worldFrom = geoFrom.clone().geo2world();
        final Location worldTo = geoTo.clone().geo2world();
        if (water) {
            final Location worldToIndented = (indent > 0) ? worldTo.clone().indent(worldFrom, indent, true) : worldTo;
            final List<Location> geoStraightLineInWater = straightLineGeoPath(geoFrom, worldToIndented.clone().world2geo());
            if (!geoStraightLineInWater.isEmpty()) {
                return geoStraightLineInWater;
            }
            return null;
        } else if (air) {
            final Location worldToIndented = (indent > 0) ? worldTo.clone().indent(worldFrom, indent, true) : worldTo;
            final Location lastAvailableLoc = GeoEngine.moveCheckInAir(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), 16.0, geoIndex);
            if (lastAvailableLoc == null || lastAvailableLoc.equals(worldFrom)) {
                return null;
            }
            final List<Location> geoStraightLine = straightLineGeoPath(geoFrom, lastAvailableLoc.world2geo());
            if (geoStraightLine.isEmpty()) {
                return null;
            }
            return geoStraightLine;
        } else {
            final Location worldToIndented = (indent > 0) ? worldTo.clone().indent(worldFrom, indent, false) : worldTo;
            final List<Location> geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), geoIndex, false);
            if (geoPathLine == null) {
                return null;
            }
            if (geoPathLine.isEmpty()) {
                return null;
            }
            return geoPathLine;
        }
    }

    public static boolean buildGeoPath(final List<List<Location>> geoPathLines, final Location geoFrom, final Location geoTo, final int geoIndex, final int colRadius, final int colHeight, final int indent, final boolean pathfind, final boolean isPlayable, final boolean air, final boolean water, final int waterZ, final boolean straightLineIgnoreGeo) {
        geoPathLines.clear();
        if (geoFrom.equals(geoTo)) {
            return true;
        }
        final Location worldFrom = geoFrom.clone().geo2world();
        final Location worldTo = geoTo.clone().geo2world();
        final Location worldToIndented = (indent > 0) ? worldTo.clone().indent(worldFrom, indent, !water && !air) : worldTo;
        if (straightLineIgnoreGeo || !Config.ALLOW_GEODATA) {
            final List<Location> geoStraightLine = straightLineGeoPath(geoFrom, worldToIndented.world2geo());
            if (geoStraightLine.isEmpty()) {
                return false;
            }
            geoPathLines.add(geoStraightLine);
            return true;
        } else if (air) {
            if (GeoEngine.canSeeCoord(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ() + colHeight + 64, worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), true, geoIndex)) {
                final List<Location> geoStraightLine = straightLineGeoPath(geoFrom, worldToIndented.world2geo());
                if (geoStraightLine.isEmpty()) {
                    return false;
                }
                geoPathLines.add(geoStraightLine);
                return true;
            } else {
                final Location lastAvailableLoc = GeoEngine.moveCheckInAir(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), colRadius, geoIndex);
                if (lastAvailableLoc == null || lastAvailableLoc.equals(worldFrom)) {
                    return false;
                }
                final List<Location> geoStraightLine2 = straightLineGeoPath(geoFrom, lastAvailableLoc.world2geo());
                if (geoStraightLine2.isEmpty()) {
                    return false;
                }
                geoPathLines.add(geoStraightLine2);
                return true;
            }
        } else {
            if (water) {
                final Location lastWorldLocInWater = GeoEngine.moveInWaterCheck(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), waterZ, geoIndex);
                final List<Location> geoStraightLineInWater = straightLineGeoPath(geoFrom, lastWorldLocInWater.clone().world2geo());
                if (!geoStraightLineInWater.isEmpty()) {
                    geoPathLines.add(geoStraightLineInWater);
                }
                final int dz = worldToIndented.getZ() - lastWorldLocInWater.getZ();
                if (!lastWorldLocInWater.clone().world2geo().equals(worldToIndented.clone().world2geo())) {
                    if (pathfind) {
                        final List<List<Location>> geoFoundPathLines = findMovePath(lastWorldLocInWater.getX(), lastWorldLocInWater.getY(), lastWorldLocInWater.getZ(), worldTo.getX(), worldTo.getY(), worldTo.getZ(), isPlayable, geoIndex);
                        if (!geoFoundPathLines.isEmpty()) {
                            if (indent > 0) {
                                List<Location> lastGeoFoundPathLine = geoFoundPathLines.remove(geoFoundPathLines.size() - 1);
                                lastGeoFoundPathLine = applyGeoIndent(lastGeoFoundPathLine, indent >> 4);
                                if (!lastGeoFoundPathLine.isEmpty()) {
                                    geoFoundPathLines.add(lastGeoFoundPathLine);
                                }
                            }
                            if (!geoFoundPathLines.isEmpty()) {
                                geoPathLines.addAll(geoFoundPathLines);
                            }
                        }
                    } else {
                        final List<Location> geoLineOnLand = GeoEngine.MoveList(lastWorldLocInWater.getX(), lastWorldLocInWater.getY(), lastWorldLocInWater.getZ(), worldTo.getX(), worldTo.getY(), geoIndex, false);
                        if (geoLineOnLand != null && !geoLineOnLand.isEmpty()) {
                            geoPathLines.add(geoLineOnLand);
                        }
                    }
                }
                return !geoPathLines.isEmpty();
            }
            List<Location> geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldTo.getX(), worldTo.getY(), geoIndex, true);
            if (geoPathLine != null) {
                if (geoPathLine.isEmpty()) {
                    return false;
                }
                geoPathLine = applyGeoIndent(geoPathLine, indent >> 4);
                if (geoPathLine.isEmpty()) {
                    return false;
                }
                geoPathLines.add(geoPathLine);
                return true;
            } else {
                if (pathfind) {
                    final List<List<Location>> geoFoundPathLines2 = findMovePath(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldTo.getX(), worldTo.getY(), worldTo.getZ(), isPlayable, geoIndex);
                    if (!geoFoundPathLines2.isEmpty()) {
                        if (indent > 0) {
                            List<Location> lastGeoFoundPathLine2 = geoFoundPathLines2.remove(geoFoundPathLines2.size() - 1);
                            lastGeoFoundPathLine2 = applyGeoIndent(lastGeoFoundPathLine2, indent >> 4);
                            if (!lastGeoFoundPathLine2.isEmpty()) {
                                geoFoundPathLines2.add(lastGeoFoundPathLine2);
                            }
                        }
                        if (!geoFoundPathLines2.isEmpty()) {
                            geoPathLines.addAll(geoFoundPathLines2);
                            return true;
                        }
                    }
                }
                geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), geoIndex, false);
                if (geoPathLine == null) {
                    return false;
                }
                if (geoPathLine.size() < 2) {
                    return false;
                }
                geoPathLines.add(geoPathLine);
                return true;
            }
        }
    }
}