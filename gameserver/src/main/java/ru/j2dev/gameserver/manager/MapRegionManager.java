package ru.j2dev.gameserver.manager;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.templates.mapregion.RegionData;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;

public class MapRegionManager extends AbstractHolder {
    private RegionData[][][] map;

    private MapRegionManager() {
        map = new RegionData[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][0];
    }

    public static MapRegionManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private int regionX(final int x) {
        return x - World.MAP_MIN_X >> 15;
    }

    private int regionY(final int y) {
        return y - World.MAP_MIN_Y >> 15;
    }

    public void addRegionData(final RegionData rd) {
        for (int x = regionX(rd.getTerritory().getXmin()); x <= regionX(rd.getTerritory().getXmax()); ++x) {
            for (int y = regionY(rd.getTerritory().getYmin()); y <= regionY(rd.getTerritory().getYmax()); ++y) {
                map[x][y] = ArrayUtils.add(map[x][y], rd);
            }
        }
    }

    public <T extends RegionData> T getRegionData(final Class<T> clazz, final GameObject o) {
        return getRegionData(clazz, o.getX(), o.getY(), o.getZ());
    }

    public <T extends RegionData> T getRegionData(final Class<T> clazz, final Location loc) {
        return getRegionData(clazz, loc.getX(), loc.getY(), loc.getZ());
    }

    @SuppressWarnings("unchecked")
    public <T extends RegionData> T getRegionData(final Class<T> clazz, final int x, final int y, final int z) {
        return (T) Arrays.stream(map[regionX(x)][regionY(y)]).filter(rd -> rd.getClass() == clazz).filter(rd -> rd.getTerritory().isInside(x, y, z)).findFirst().orElse(null);
    }

    @Override
    public int size() {
        return World.WORLD_SIZE_X * World.WORLD_SIZE_Y;
    }

    @Override
    public void clear() {
    }

    private static class LazyHolder {
        private static final MapRegionManager INSTANCE = new MapRegionManager();
    }
}
