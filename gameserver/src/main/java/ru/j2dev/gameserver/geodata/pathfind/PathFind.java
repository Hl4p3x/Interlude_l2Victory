package ru.j2dev.gameserver.geodata.pathfind;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.geodata.pathfind.PathFindBuffers.GeoNode;
import ru.j2dev.gameserver.geodata.pathfind.PathFindBuffers.PathFindBuffer;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class PathFind {
    private final short[] hNSWE;
    private final Location startPoint;
    private final Location endPoint;
    private int geoIndex;
    private PathFindBuffer buff;
    private List<Location> path;
    private GeoNode startNode;
    private GeoNode endNode;
    private GeoNode currentNode;

    public PathFind(final int x, final int y, final int z, final int destX, final int destY, final int destZ, final boolean isPlayable, final int geoIndex) {
        this.geoIndex = 0;
        hNSWE = new short[2];
        this.geoIndex = geoIndex;
        startPoint = ((Config.PATHFIND_BOOST == 0) ? new Location(x, y, z) : GeoEngine.moveCheckWithCollision(x, y, z, destX, destY, true, geoIndex));
        endPoint = ((Config.PATHFIND_BOOST != 2 || Math.abs(destZ - z) > 200) ? new Location(destX, destY, destZ) : GeoEngine.moveCheckBackwardWithCollision(destX, destY, destZ, startPoint.x, startPoint.y, true, geoIndex));
        startPoint.world2geo();
        endPoint.world2geo();
        startPoint.z = GeoEngine.NgetHeight(startPoint.x, startPoint.y, startPoint.z, geoIndex);
        endPoint.z = GeoEngine.NgetHeight(endPoint.x, endPoint.y, endPoint.z, geoIndex);
        if(Math.abs(z - destZ) > (isPlayable ? Config.NPC_PATH_FIND_MAX_HEIGHT : Config.PLAYABLE_PATH_FIND_MAX_HEIGHT)) {
            return;
        }
        final int xdiff = Math.abs(endPoint.x - startPoint.x);
        final int ydiff = Math.abs(endPoint.y - startPoint.y);
        if (xdiff == 0 && ydiff == 0) {
            if (Math.abs(endPoint.z - startPoint.z) < 32) {
                (path = new ArrayList<>()).add(0, startPoint);
            }
            return;
        }
        final int mapSize = 2 * Math.max(xdiff, ydiff);
        if ((buff = PathFindBuffers.alloc(mapSize)) != null) {
            buff.offsetX = startPoint.x - buff.mapSize / 2;
            buff.offsetY = startPoint.y - buff.mapSize / 2;
            final PathFindBuffer buff = this.buff;
            ++buff.totalUses;
            if (isPlayable) {
                final PathFindBuffer buff2 = this.buff;
                ++buff2.playableUses;
            }
            findPath();
            this.buff.free();
            PathFindBuffers.recycle(this.buff);
        }
    }

    private List<Location> findPath() {
        startNode = buff.nodes[startPoint.x - buff.offsetX][startPoint.y - buff.offsetY].set(startPoint.x, startPoint.y, (short) startPoint.z);
        GeoEngine.NgetHeightAndNSWE(startPoint.x, startPoint.y, (short) startPoint.z, hNSWE, geoIndex);
        startNode.z = hNSWE[0];
        startNode.nswe = hNSWE[1];
        startNode.costFromStart = 0.0f;
        startNode.state = 1;
        startNode.parent = null;
        endNode = buff.nodes[endPoint.x - buff.offsetX][endPoint.y - buff.offsetY].set(endPoint.x, endPoint.y, (short) endPoint.z);
        startNode.costToEnd = pathCostEstimate(startNode);
        startNode.totalCost = startNode.costFromStart + startNode.costToEnd;
        buff.open.add(startNode);
        final long nanos = System.nanoTime();
        long searhTime;
        int itr = 0;
        while ((searhTime = System.nanoTime() - nanos) < Config.PATHFIND_MAX_TIME && (currentNode = buff.open.poll()) != null) {
            ++itr;
            if (currentNode.x == endPoint.x && currentNode.y == endPoint.y && Math.abs(currentNode.z - endPoint.z) < 64) {
                path = tracePath(currentNode);
                break;
            }
            handleNode(currentNode);
            currentNode.state = -1;
        }
        final PathFindBuffer buff = this.buff;
        buff.totalTime += searhTime;
        final PathFindBuffer buff2 = this.buff;
        buff2.totalItr += itr;
        if (path != null) {
            final PathFindBuffer buff3 = this.buff;
            ++buff3.successUses;
        } else if (searhTime > Config.PATHFIND_MAX_TIME) {
            final PathFindBuffer buff4 = this.buff;
            ++buff4.overtimeUses;
        }
        return path;
    }

    private List<Location> tracePath(GeoNode f) {
        final List<Location> locations = new ArrayList<>();
        do {
            locations.add(0, f.getLoc());
            f = f.parent;
        } while (f.parent != null);
        return locations;
    }

    private void handleNode(final GeoNode node) {
        final int clX = node.x;
        final int clY = node.y;
        final short clZ = node.z;
        getHeightAndNSWE(clX, clY, clZ);
        final short NSWE = hNSWE[1];
        if (Config.PATHFIND_DIAGONAL) {
            if ((NSWE & 0x4) == 0x4 && (NSWE & 0x1) == 0x1) {
                getHeightAndNSWE(clX + 1, clY, clZ);
                if ((hNSWE[1] & 0x4) == 0x4) {
                    getHeightAndNSWE(clX, clY + 1, clZ);
                    if ((hNSWE[1] & 0x1) == 0x1) {
                        handleNeighbour(clX + 1, clY + 1, node, true);
                    }
                }
            }
            if ((NSWE & 0x4) == 0x4 && (NSWE & 0x2) == 0x2) {
                getHeightAndNSWE(clX - 1, clY, clZ);
                if ((hNSWE[1] & 0x4) == 0x4) {
                    getHeightAndNSWE(clX, clY + 1, clZ);
                    if ((hNSWE[1] & 0x2) == 0x2) {
                        handleNeighbour(clX - 1, clY + 1, node, true);
                    }
                }
            }
            if ((NSWE & 0x8) == 0x8 && (NSWE & 0x1) == 0x1) {
                getHeightAndNSWE(clX + 1, clY, clZ);
                if ((hNSWE[1] & 0x8) == 0x8) {
                    getHeightAndNSWE(clX, clY - 1, clZ);
                    if ((hNSWE[1] & 0x1) == 0x1) {
                        handleNeighbour(clX + 1, clY - 1, node, true);
                    }
                }
            }
            if ((NSWE & 0x8) == 0x8 && (NSWE & 0x2) == 0x2) {
                getHeightAndNSWE(clX - 1, clY, clZ);
                if ((hNSWE[1] & 0x8) == 0x8) {
                    getHeightAndNSWE(clX, clY - 1, clZ);
                    if ((hNSWE[1] & 0x2) == 0x2) {
                        handleNeighbour(clX - 1, clY - 1, node, true);
                    }
                }
            }
        }
        if ((NSWE & 0x1) == 0x1) {
            handleNeighbour(clX + 1, clY, node, false);
        }
        if ((NSWE & 0x2) == 0x2) {
            handleNeighbour(clX - 1, clY, node, false);
        }
        if ((NSWE & 0x4) == 0x4) {
            handleNeighbour(clX, clY + 1, node, false);
        }
        if ((NSWE & 0x8) == 0x8) {
            handleNeighbour(clX, clY - 1, node, false);
        }
    }

    private float pathCostEstimate(final GeoNode n) {
        final int diffx = endNode.x - n.x;
        final int diffy = endNode.y - n.y;
        final int diffz = endNode.z - n.z;
        return (float) Math.sqrt(diffx * diffx + diffy * diffy + diffz * diffz / 256);
    }

    private float traverseCost(final GeoNode from, final GeoNode n, final boolean d) {
        if (n.nswe != 15 || Math.abs(n.z - from.z) > 16) {
            return 3.0f;
        }
        getHeightAndNSWE(n.x + 1, n.y, n.z);
        if (hNSWE[1] != 15 || Math.abs(n.z - hNSWE[0]) > 16) {
            return 2.0f;
        }
        getHeightAndNSWE(n.x - 1, n.y, n.z);
        if (hNSWE[1] != 15 || Math.abs(n.z - hNSWE[0]) > 16) {
            return 2.0f;
        }
        getHeightAndNSWE(n.x, n.y + 1, n.z);
        if (hNSWE[1] != 15 || Math.abs(n.z - hNSWE[0]) > 16) {
            return 2.0f;
        }
        getHeightAndNSWE(n.x, n.y - 1, n.z);
        if (hNSWE[1] != 15 || Math.abs(n.z - hNSWE[0]) > 16) {
            return 2.0f;
        }
        return d ? 1.414f : 1.0f;
    }

    private void handleNeighbour(final int x, final int y, final GeoNode from, final boolean d) {
        final int nX = x - buff.offsetX;
        final int nY = y - buff.offsetY;
        if (nX >= buff.mapSize || nX < 0 || nY >= buff.mapSize || nY < 0) {
            return;
        }
        GeoNode n = buff.nodes[nX][nY];
        if (n.isSet()) {
            n = n.set(x, y, from.z);
            GeoEngine.NgetHeightAndNSWE(x, y, from.z, hNSWE, geoIndex);
            n.z = hNSWE[0];
            n.nswe = hNSWE[1];
        }
        final int height = Math.abs(n.z - from.z);
        if (height > Config.PATHFIND_MAX_Z_DIFF || n.nswe == 0) {
            return;
        }
        final float newCost = from.costFromStart + traverseCost(from, n, d);
        if ((n.state == 1 || n.state == -1) && n.costFromStart <= newCost) {
            return;
        }
        if (n.state == 0) {
            n.costToEnd = pathCostEstimate(n);
        }
        n.parent = from;
        n.costFromStart = newCost;
        n.totalCost = n.costFromStart + n.costToEnd;
        if (n.state == 1) {
            return;
        }
        n.state = 1;
        buff.open.add(n);
    }

    private void getHeightAndNSWE(final int x, final int y, final short z) {
        final int nX = x - buff.offsetX;
        final int nY = y - buff.offsetY;
        if (nX >= buff.mapSize || nX < 0 || nY >= buff.mapSize || nY < 0) {
            hNSWE[1] = 0;
            return;
        }
        GeoNode n = buff.nodes[nX][nY];
        if (n.isSet()) {
            n = n.set(x, y, z);
            GeoEngine.NgetHeightAndNSWE(x, y, z, hNSWE, geoIndex);
            n.z = hNSWE[0];
            n.nswe = hNSWE[1];
        } else {
            hNSWE[0] = n.z;
            hNSWE[1] = n.nswe;
        }
    }

    public List<Location> getPath() {
        return path;
    }
}
