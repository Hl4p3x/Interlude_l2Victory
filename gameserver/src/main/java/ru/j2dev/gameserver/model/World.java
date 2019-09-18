package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class World {
    private static final Logger LOGGER = LoggerFactory.getLogger(World.class);
    /**
     * Map dimensions
     */
    public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
    public static final int MAP_MAX_X = (Config.GEO_X_LAST - 19 << 15) - 1;
    public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
    public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 17 << 15) - 1;
    public static final int MAP_MIN_Z = Config.MAP_MIN_Z;
    public static final int MAP_MAX_Z = Config.MAP_MAX_Z;
    public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
    public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
    public static final int SHIFT_BY = Config.SHIFT_BY;
    private static final int SHIFT_BY_Z = Config.SHIFT_BY_Z;
    /**
     * calculated offset used so top left region is 0,0
     */
    private static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
    private static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
    private static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);
    /**
     * Размерность массива регионов
     */
    private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
    private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
    private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;
    private static volatile WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];

    public static void init() {
        LOGGER.info("World Build: Creating regions: [" + (REGIONS_X + 1) + "][" + (REGIONS_Y + 1) + "][" + (REGIONS_Z + 1) + "].");
    }

    private static WorldRegion[][][] getRegions() {
        return _worldRegions;
    }

    private static int validX(int x) {
        if (x < 0) {
            x = 0;
        } else if (x > REGIONS_X) {
            x = REGIONS_X;
        }
        return x;
    }

    private static int validY(int y) {
        if (y < 0) {
            y = 0;
        } else if (y > REGIONS_Y) {
            y = REGIONS_Y;
        }
        return y;
    }

    private static int validZ(int z) {
        if (z < 0) {
            z = 0;
        } else if (z > REGIONS_Z) {
            z = REGIONS_Z;
        }
        return z;
    }

    public static int validCoordX(int x) {
        if (x < MAP_MIN_X) {
            x = MAP_MIN_X + 1;
        } else if (x > MAP_MAX_X) {
            x = MAP_MAX_X - 1;
        }
        return x;
    }

    public static int validCoordY(int y) {
        if (y < MAP_MIN_Y) {
            y = MAP_MIN_Y + 1;
        } else if (y > MAP_MAX_Y) {
            y = MAP_MAX_Y - 1;
        }
        return y;
    }

    public static int validCoordZ(int z) {
        if (z < MAP_MIN_Z) {
            z = MAP_MIN_Z + 1;
        } else if (z > MAP_MAX_Z) {
            z = MAP_MAX_Z - 1;
        }
        return z;
    }

    private static int regionX(final int x) {
        return (x >> SHIFT_BY) + OFFSET_X;
    }

    private static int regionY(final int y) {
        return (y >> SHIFT_BY) + OFFSET_Y;
    }

    private static int regionZ(final int z) {
        return (z >> SHIFT_BY_Z) + OFFSET_Z;
    }

    static boolean isNeighbour(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        return x1 <= x2 + 1 && x1 >= x2 - 1 && y1 <= y2 + 1 && y1 >= y2 - 1 && z1 <= z2 + 1 && z1 >= z2 - 1;
    }

    /**
     * @param loc локация для поиска региона
     *            <p>
     * @return Регион, соответствующий локации
     */
    public static WorldRegion getRegion(final Location loc) {
        return getRegion(validX(regionX(loc.x)), validY(regionY(loc.y)), validZ(regionZ(loc.z)));
    }

    /**
     * @param obj обьект для поиска региона
     *            <p>
     * @return Регион, соответствующий координатам обьекта
     */
    public static WorldRegion getRegion(final GameObject obj) {
        return getRegion(validX(regionX(obj.getX())), validY(regionY(obj.getY())), validZ(regionZ(obj.getZ())));
    }

    /**
     * @param x координата на карте регионов
     * @param y координата на карте регионов
     * @param z координата на карте регионов
     *          <p>
     * @return Регион, соответствующий координатам
     */
    public static WorldRegion getRegion(final int x, final int y, final int z) {
        final WorldRegion[][][] regions = _worldRegions;
        WorldRegion region;
        region = regions[x][y][z];
        if (region == null) {
            synchronized (regions) {
                region = regions[x][y][z];
                if (region == null) {
                    region = regions[x][y][z] = new WorldRegion(x, y, z);
                }
            }
        }
        return region;
    }

    public static Player getPlayer(final String name) {
        return GameObjectsStorage.getPlayer(name);
    }

    public static Player getPlayer(final int objId) {
        return GameObjectsStorage.getPlayer(objId);
    }

    /**
     * Проверяет, сменился ли регион в котором находится обьект Если сменился -
     * удаляет обьект из старого региона и добавляет в новый.
     *
     * @param object  обьект для проверки
     * @param dropper - если это L2ItemInstance, то будет анимация дропа с перса
     */
    public static void addVisibleObject(final GameObject object, final Creature dropper) {
        if (object == null || !object.isVisible() || object.isInObserverMode()) {
            return;
        }

        final WorldRegion region = getRegion(object);
        final WorldRegion currentRegion = object.getCurrentRegion();

        if (currentRegion == region) {
            return;
        }

        if (currentRegion == null) // Новый обьект (пример - игрок вошел в мир,
        // заспаунился моб, дропнули вещь)
        {
            // Добавляем обьект в список видимых
            object.setCurrentRegion(region);
            region.addObject(object);

            // Показываем обьект в текущем и соседних регионах
            // Если обьект игрок, показываем ему все обьекты в текущем и
            // соседних регионах
            for (int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); x++) {
                for (int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); y++) {
                    for (int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); z++) {
                        getRegion(x, y, z).addToPlayers(object, dropper);
                    }
                }
            }
        } else// Обьект уже существует, перешел из одного региона в другой
        {
            currentRegion.removeObject(object); // Удаляем обьект из старого
            // региона
            object.setCurrentRegion(region);
            region.addObject(object); // Добавляем обьект в список видимых

            // Убираем обьект из старых соседей.
            for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++) {
                for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++) {
                    for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++) {
                        if (!isNeighbour(region.getX(), region.getY(), region.getZ(), x, y, z)) {
                            getRegion(x, y, z).removeFromPlayers(object);
                        }
                    }
                }
            }

            // Показываем обьект, но в отличие от первого случая - только для
            // новых соседей.
            for (int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); x++) {
                for (int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); y++) {
                    for (int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); z++) {
                        if (!isNeighbour(currentRegion.getX(), currentRegion.getY(), currentRegion.getZ(), x, y, z)) {
                            getRegion(x, y, z).addToPlayers(object, dropper);
                        }
                    }
                }
            }
        }
    }

    /**
     * Удаляет обьект из текущего региона
     *
     * @param object обьект для удаления
     */
    public static void removeVisibleObject(final GameObject object) {
        if (object == null || object.isVisible() || object.isInObserverMode()) {
            return;
        }

        final WorldRegion currentRegion;
        if ((currentRegion = object.getCurrentRegion()) == null) {
            return;
        }

        object.setCurrentRegion(null);
        currentRegion.removeObject(object);

        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++) {
                    getRegion(x, y, z).removeFromPlayers(object);
                }
            }
        }
    }

    public static GameObject getAroundObjectById(final GameObject object, final int objId) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return null;
        }
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.getObjectId() == objId) {
                            return obj;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<GameObject> getAroundObjects(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final List<GameObject> result = new ArrayList<>(128);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            result.add(obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<GameObject> getAroundObjects(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<GameObject> result = new ArrayList<>(128);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add(obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Creature> getAroundCharacters(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final List<Creature> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isCreature() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            result.add((Creature) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Creature> getAroundCharacters(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<Creature> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isCreature() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add((Creature) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<NpcInstance> getAroundNpc(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final List<NpcInstance> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isNpc() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            result.add((NpcInstance) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<NpcInstance> getAroundNpc(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<NpcInstance> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isNpc() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add((NpcInstance) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Playable> getAroundPlayables(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final List<Playable> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayable() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            result.add((Playable) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Playable> getAroundPlayables(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<Playable> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayable() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add((Playable) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Player> getAroundPlayers(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final List<Player> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayer() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            result.add((Player) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Player> getAroundPlayers(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<Player> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayer() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add((Player) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<MonsterInstance> getAroundMonsters(final GameObject object, final int radius, final int height) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return Collections.emptyList();
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        final int ox = object.getX();
        final int oy = object.getY();
        final int oz = object.getZ();
        final int sqrad = radius * radius;
        final List<MonsterInstance> result = new ArrayList<>(64);
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayer() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            if (Math.abs(obj.getZ() - oz) > height) {
                                continue;
                            }
                            final int dx = Math.abs(obj.getX() - ox);
                            if (dx > radius) {
                                continue;
                            }
                            final int dy = Math.abs(obj.getY() - oy);
                            if (dy > radius) {
                                continue;
                            }
                            if (dx * dx + dy * dy > sqrad) {
                                continue;
                            }
                            result.add((MonsterInstance) obj);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean isNeighborsEmpty(final WorldRegion region) {
        for (int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); ++x) {
            for (int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); ++y) {
                for (int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); ++z) {
                    if (!getRegion(x, y, z).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void activate(final WorldRegion currentRegion) {
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    getRegion(x, y, z).setActive(true);
                }
            }
        }
    }

    public static void deactivate(final WorldRegion currentRegion) {
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    if (isNeighborsEmpty(getRegion(x, y, z))) {
                        getRegion(x, y, z).setActive(false);
                    }
                }
            }
        }
    }

    public static void showObjectsToPlayer(final Player player) {
        final WorldRegion currentRegion = player.isInObserverMode() ? player.getObserverRegion() : player.getCurrentRegion();
        if (currentRegion == null) {
            return;
        }
        final int oid = player.getObjectId();
        final int rid = player.getReflectionId();
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            player.sendPacket(player.addVisibleObject(obj, null));
                        }
                    }
                }
            }
        }
    }

    public static void removeObjectsFromPlayer(final Player player) {
        final WorldRegion currentRegion = player.isInObserverMode() ? player.getObserverRegion() : player.getCurrentRegion();
        if (currentRegion == null) {
            return;
        }
        final int oid = player.getObjectId();
        final int rid = player.getReflectionId();
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            player.sendPacket(player.removeVisibleObject(obj, null));
                        }
                    }
                }
            }
        }
    }

    public static void removeObjectFromPlayers(final GameObject object) {
        final WorldRegion currentRegion = object.getCurrentRegion();
        if (currentRegion == null) {
            return;
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        List<L2GameServerPacket> d = null;
        for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); ++x) {
            for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); ++y) {
                for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); ++z) {
                    for (final GameObject obj : getRegion(x, y, z)) {
                        if (obj.isPlayer() && obj.getObjectId() != oid) {
                            if (obj.getReflectionId() != rid) {
                                continue;
                            }
                            final Player player = (Player) obj;
                            player.sendPacket(player.removeVisibleObject(object, (d == null) ? (d = object.deletePacketList()) : d));
                        }
                    }
                }
            }
        }
    }

    static void addZone(final Zone zone) {
        final Reflection reflection = zone.getReflection();
        final Territory territory = zone.getTerritory();
        if (territory == null) {
            LOGGER.info("World: zone - " + zone.getName() + " not has territory.");
            return;
        }
        for (int x = validX(regionX(territory.getXmin())); x <= validX(regionX(territory.getXmax())); ++x) {
            for (int y = validY(regionY(territory.getYmin())); y <= validY(regionY(territory.getYmax())); ++y) {
                for (int z = validZ(regionZ(territory.getZmin())); z <= validZ(regionZ(territory.getZmax())); ++z) {
                    final WorldRegion region = getRegion(x, y, z);
                    region.addZone(zone);
                    for (final GameObject obj : region) {
                        if (obj.isCreature()) {
                            if (obj.getReflection() != reflection) {
                                continue;
                            }
                            ((Creature) obj).updateZones();
                        }
                    }
                }
            }
        }
    }

    static void removeZone(final Zone zone) {
        final Reflection reflection = zone.getReflection();
        final Territory territory = zone.getTerritory();
        if (territory == null) {
            LOGGER.info("World: zone - " + zone.getName() + " not has territory.");
            return;
        }
        for (int x = validX(regionX(territory.getXmin())); x <= validX(regionX(territory.getXmax())); ++x) {
            for (int y = validY(regionY(territory.getYmin())); y <= validY(regionY(territory.getYmax())); ++y) {
                for (int z = validZ(regionZ(territory.getZmin())); z <= validZ(regionZ(territory.getZmax())); ++z) {
                    final WorldRegion region = getRegion(x, y, z);
                    region.removeZone(zone);
                    for (final GameObject obj : region) {
                        if (obj.isCreature()) {
                            if (obj.getReflection() != reflection) {
                                continue;
                            }
                            ((Creature) obj).updateZones();
                        }
                    }
                }
            }
        }
    }

    public static void getZones(final List<Zone> inside, final Location loc, final Reflection reflection) {
        final WorldRegion region = getRegion(loc);
        final Zone[] zones = region.getZones();
        if (zones.length == 0) {
            return;
        }
        for (Zone zone : zones) {
            if (zone.checkIfInZone(loc.x, loc.y, loc.z, reflection)) {
                inside.add(zone);
            }
        }
    }

    public static boolean isWater(final Location loc, final Reflection reflection) {
        return getWater(loc, reflection) != null;
    }

    public static Zone getWater(final Location loc, final Reflection reflection) {
        final WorldRegion region = getRegion(loc);
        final Zone[] zones = region.getZones();
        if (zones.length == 0) {
            return null;
        }
        return Arrays.stream(zones).filter(zone -> zone != null && zone.getType() == ZoneType.water && zone.checkIfInZone(loc.x, loc.y, loc.z, reflection)).findFirst().orElse(null);
    }

    public static void broadcast(final L2GameServerPacket... packets) {
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(packets));
    }

    public static int[] getStats() {
        final int[] ret = new int[32];
        for (int x = 0; x <= REGIONS_X; ++x) {
            for (int y = 0; y <= REGIONS_Y; ++y) {
                for (int z = 0; z <= REGIONS_Z; ++z) {
                    final int n = 0;
                    ret[n]++;
                    final WorldRegion region = _worldRegions[x][y][z];
                    if (region != null) {
                        if (region.isActive()) {
                            final int n2 = 1;
                            ret[n2]++;
                        } else {
                            final int n3 = 2;
                            ret[n3]++;
                        }
                        for (final GameObject obj : region) {
                            final int n4 = 10;
                            ret[n4]++;
                            if (obj.isCreature()) {
                                final int n5 = 11;
                                ret[n5]++;
                                if (obj.isPlayer()) {
                                    final int n6 = 12;
                                    ret[n6]++;
                                    final Player p = (Player) obj;
                                    if (!p.isInOfflineMode()) {
                                        continue;
                                    }
                                    final int n7 = 13;
                                    ret[n7]++;
                                } else if (obj.isNpc()) {
                                    final int n8 = 14;
                                    ret[n8]++;
                                    if (obj.isMonster()) {
                                        final int n9 = 16;
                                        ret[n9]++;
                                        if (obj.isMinion()) {
                                            final int n10 = 17;
                                            ret[n10]++;
                                        }
                                    }
                                    final NpcInstance npc = (NpcInstance) obj;
                                    if (!npc.hasAI() || !npc.getAI().isActive()) {
                                        continue;
                                    }
                                    final int n11 = 15;
                                    ret[n11]++;
                                } else if (obj.isPlayable()) {
                                    final int n12 = 18;
                                    ret[n12]++;
                                } else {
                                    if (!obj.isDoor()) {
                                        continue;
                                    }
                                    final int n13 = 19;
                                    ret[n13]++;
                                }
                            } else {
                                if (!obj.isItem()) {
                                    continue;
                                }
                                final int n14 = 20;
                                ret[n14]++;
                            }
                        }
                    } else {
                        final int n15 = 3;
                        ret[n15]++;
                    }
                }
            }
        }
        return ret;
    }
}
