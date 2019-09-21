package ru.j2dev.gameserver.geodata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.geometry.Shape;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.utils.GeoOptimizer;
import ru.j2dev.gameserver.geodata.utils.GeoOptimizer.BlockLink;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Diamond
 * @CoAuthor: DRiN
 * @Date: 01/03/2009
 */
public class GeoEngine {
    public static final byte EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15, NSWE_NONE = 0;
    public static final byte BLOCKTYPE_FLAT = 0;
    public static final byte BLOCKTYPE_COMPLEX = 1;
    public static final byte BLOCKTYPE_MULTILEVEL = 2;
    public static final int BLOCKS_IN_MAP = 256 * 256;
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoEngine.class);
    /**
     * Даный массив содержит эталонную геодату. <BR>
     * Первые 2 [][] (byte[*][*][][]) являются x и y региона.<BR>
     */
    private static final ByteBuffer[][] rawgeo = new ByteBuffer[World.WORLD_SIZE_X][World.WORLD_SIZE_Y];
    /**
     * Даный массив содержит всю геодату на сервере. <BR>
     * Первые 2 [][] (byte[*][*][][]) являются x и y региона.<BR>
     * Третий [] (byte[][][*][]) является блоком геодаты.<BR>
     * Четвертый [] (byte[][][][*]) является контейнером для всех блоков в регионе.<BR>
     */
    private static final byte[][][][][] geodata = new byte[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][1][][];
    public static int MAX_LAYERS = 1; // меньше 1 быть не должно, что бы создавались временные массивы как минимум short[2]

    public static short getType(int x, int y, int geoIndex) {
        return NgetType(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, geoIndex);
    }

    public static int getHeight(Location loc, int geoIndex) {
        return getHeight(loc.x, loc.y, loc.z, geoIndex);
    }

    public static int getHeight(int x, int y, int z, int geoIndex) {
        return NgetHeight(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, geoIndex);
    }

    public static boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz, int geoIndex) {
        return canMove(x, y, z, tx, ty, tz, false, geoIndex);
    }

    public static byte getNSWE(int x, int y, int z, int geoIndex) {
        return NgetNSWE(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, geoIndex);
    }

    public static Location moveCheck(int x, int y, int z, int tx, int ty, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, false, false, false, geoIndex);
    }

    public static Location moveCheck(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, false, false, returnPrev, geoIndex);
    }

    public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, true, false, false, geoIndex);
    }

    public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, true, false, returnPrev, geoIndex);
    }

    public static Location moveCheckBackward(int x, int y, int z, int tx, int ty, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, false, true, false, geoIndex);
    }

    public static Location moveCheckBackward(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, false, true, returnPrev, geoIndex);
    }

    public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, true, true, false, geoIndex);
    }

    public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex) {
        return MoveCheck(x, y, z, tx, ty, true, true, returnPrev, geoIndex);
    }

    public static Location moveInWaterCheck(int x, int y, int z, int tx, int ty, int tz, int waterZ, int geoIndex) {
        return MoveInWaterCheck(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, tx - World.MAP_MIN_X >> 4, ty - World.MAP_MIN_Y >> 4, tz, waterZ, geoIndex);
    }

    public static Location moveCheckForAI(Location loc1, Location loc2, int geoIndex) {
        return MoveCheckForAI(loc1.x - World.MAP_MIN_X >> 4, loc1.y - World.MAP_MIN_Y >> 4, loc1.z, loc2.x - World.MAP_MIN_X >> 4, loc2.y - World.MAP_MIN_Y >> 4, geoIndex);
    }

    public static Location moveCheckInAir(int x, int y, int z, int tx, int ty, int tz, double collision, int geoIndex) {
        int gx = x - World.MAP_MIN_X >> 4;
        int gy = y - World.MAP_MIN_Y >> 4;
        int tgx = tx - World.MAP_MIN_X >> 4;
        int tgy = ty - World.MAP_MIN_Y >> 4;

        int nz = NgetHeight(tgx, tgy, tz, geoIndex);

        // Не даем опуститься ниже, чем пол + 32
        if (tz <= nz + 32) {
            tz = nz + 32;
        }

        Location result = canSee(gx, gy, z, tgx, tgy, tz, true, geoIndex);
        if (result.equals(gx, gy, z)) {
            return null;
        }

        return result.geo2world();
    }

    public static boolean canSeeTarget(GameObject actor, GameObject target, boolean air) {
        if (target == null) {
            return false;
        }
        // Костыль конечно, но решает кучу проблем с дверьми
        if (target instanceof GeoCollision || actor.equals(target)) {
            return true;
        }
        return canSeeCoord(actor, target.getX(), target.getY(), target.getZ() + (int) target.getColHeight() + 32, air);
    }

    public static boolean canSeeCoord(GameObject actor, int tx, int ty, int tz, boolean air) {
        return actor != null && canSeeCoord(actor.getX(), actor.getY(), actor.getZ() + (int) actor.getColHeight() + 32, tx, ty, tz, air, actor.getGeoIndex());
    }

    public static boolean canSeeCoord(int x, int y, int z, int tx, int ty, int tz, boolean air, int geoIndex) {
        int mx = x - World.MAP_MIN_X >> 4;
        int my = y - World.MAP_MIN_Y >> 4;
        int tmx = tx - World.MAP_MIN_X >> 4;
        int tmy = ty - World.MAP_MIN_Y >> 4;
        return canSee(mx, my, z, tmx, tmy, tz, air, geoIndex).equals(tmx, tmy, tz) && canSee(tmx, tmy, tz, mx, my, z, air, geoIndex).equals(mx, my, z);
    }

    public static boolean canMoveWithCollision(int x, int y, int z, int tx, int ty, int tz, int geoIndex) {
        return canMove(x, y, z, tx, ty, tz, true, geoIndex);
    }

    /**
     * @param NSWE
     * @param x
     * @param y
     * @param tx
     * @param ty
     * @return True if NSWE dont block given direction
     */
    public static boolean checkNSWE(byte NSWE, int x, int y, int tx, int ty) {
        if (NSWE == NSWE_ALL) {
            return true;
        }
        if (NSWE == NSWE_NONE) {
            return false;
        }
        if (tx > x) {
            if ((NSWE & EAST) == 0) {
                return false;
            }
        } else if (tx < x) {
            if ((NSWE & WEST) == 0) {
                return false;
            }
        }
        if (ty > y) {
            if ((NSWE & SOUTH) == 0) {
                return false;
            }
        } else if (ty < y) {
            if ((NSWE & NORTH) == 0) {
                return false;
            }
        }
        return true;
    }

    public static String geoXYZ2Str(int _x, int _y, int _z) {
        return "(" + String.valueOf((_x << 4) + World.MAP_MIN_X + 8) + " " + String.valueOf((_y << 4) + World.MAP_MIN_Y + 8) + " " + _z + ")";
    }

    public static String NSWE2Str(byte nswe) {
        String result = "";
        if ((nswe & NORTH) == NORTH) {
            result += "N";
        }
        if ((nswe & SOUTH) == SOUTH) {
            result += "S";
        }
        if ((nswe & WEST) == WEST) {
            result += "W";
        }
        if ((nswe & EAST) == EAST) {
            result += "E";
        }
        return result.isEmpty() ? "X" : result;
    }

    private static boolean NLOS_WATER(int x, int y, int z, int next_x, int next_y, int next_z, int geoIndex) {
        short[] layers1 = new short[MAX_LAYERS + 1];
        short[] layers2 = new short[MAX_LAYERS + 1];
        NGetLayers(x, y, layers1, geoIndex);
        NGetLayers(next_x, next_y, layers2, geoIndex);

        if (layers1[0] == 0 || layers2[0] == 0) {
            return true;
        }

        short h;

        // Находим ближайший к целевой клетке слой
        short z2 = Short.MIN_VALUE;
        for (int i = 1; i <= layers2[0]; i++) {
            h = (short) ((short) (layers2[i] & 0x0fff0) >> 1);
            if (Math.abs(next_z - z2) > Math.abs(next_z - h)) {
                z2 = h;
            }
        }

        // Луч проходит над преградой
        if (next_z + 32 >= z2) {
            return true;
        }

        // Либо перед нами стена, либо над нами потолок. Ищем слой пониже, для уточнения
        short z3 = Short.MIN_VALUE;
        for (int i = 1; i <= layers2[0]; i++) {
            h = (short) ((short) (layers2[i] & 0x0fff0) >> 1);
            if (h < z2 + Config.MIN_LAYER_HEIGHT && Math.abs(next_z - z3) > Math.abs(next_z - h)) {
                z3 = h;
            }
        }

        // Ниже нет слоев, значит это стена
        if (z3 == Short.MIN_VALUE) {
            return false;
        }

        // Собираем данные о предыдущей клетке, игнорируя верхние слои
        short z1 = Short.MIN_VALUE;
        byte NSWE1 = NSWE_ALL;
        for (int i = 1; i <= layers1[0]; i++) {
            h = (short) ((short) (layers1[i] & 0x0fff0) >> 1);
            if (h < z + Config.MIN_LAYER_HEIGHT && Math.abs(z - z1) > Math.abs(z - h)) {
                z1 = h;
                NSWE1 = (byte) (layers1[i] & 0x0F);
            }
        }

        // Если есть NSWE, то считаем за стену
        return checkNSWE(NSWE1, x, y, next_x, next_y);
    }

    private static int FindNearestLowerLayer(short[] layers, int z, boolean regionEdge) {
        short h, nearest_layer_h = Short.MIN_VALUE;
        int nearest_layer = Integer.MIN_VALUE;
        int zCheck = regionEdge ? z + Config.REGION_EDGE_MAX_Z_DIFF : z;
        for (int i = 1; i <= layers[0]; i++) {
            h = (short) ((short) (layers[i] & 0x0fff0) >> 1);
            if (h < zCheck && nearest_layer_h < h) {
                nearest_layer_h = h;
                nearest_layer = layers[i];
            }
        }
        return nearest_layer;
    }

    private static short CheckNoOneLayerInRangeAndFindNearestLowerLayer(short[] layers, int z0, int z1) {
        int z_min, z_max;
        if (z0 > z1) {
            z_min = z1;
            z_max = z0;
        } else {
            z_min = z0;
            z_max = z1;
        }
        short h, nearest_layer = Short.MIN_VALUE, nearest_layer_h = Short.MIN_VALUE;
        for (int i = 1; i <= layers[0]; i++) {
            h = (short) ((short) (layers[i] & 0x0fff0) >> 1);
            if (z_min <= h && h <= z_max) {
                return Short.MIN_VALUE;
            }
            if (h < z_max && nearest_layer_h < h) {
                nearest_layer_h = h;
                nearest_layer = layers[i];
            }
        }
        return nearest_layer;
    }

    public static boolean canSeeWallCheck(short layer, short nearest_lower_neighbor, byte directionNSWE, int curr_z, boolean air) {
        short nearest_lower_neighborh = (short) ((short) (nearest_lower_neighbor & 0x0fff0) >> 1);
        if (air) {
            return nearest_lower_neighborh < curr_z;
        }
        short layerh = (short) ((short) (layer & 0x0fff0) >> 1);
        int zdiff = nearest_lower_neighborh - layerh;
        return (layer & 0x0F & directionNSWE) != 0 || zdiff > -Config.MAX_Z_DIFF && zdiff != 0;
    }

    /**
     * проверка видимости
     *
     * @return возвращает последнюю точку которую видно (в формате геокоординат)
     * в результате (Location) h является кодом, если >= 0 то успешно достигли последней точки, если меньше то не последней
     */
    public static Location canSee(int _x, int _y, int _z, int _tx, int _ty, int _tz, boolean air, int geoIndex) {
        int diff_x = _tx - _x, diff_y = _ty - _y, diff_z = _tz - _z;
        int dx = Math.abs(diff_x), dy = Math.abs(diff_y);

        float steps = Math.max(dx, dy);
        int curr_x = _x, curr_y = _y, curr_z = _z;
        short[] curr_layers = new short[MAX_LAYERS + 1];
        NGetLayers(curr_x, curr_y, curr_layers, geoIndex);

        Location result = new Location(_x, _y, _z, -1);

        if (steps == 0) {
            if (CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, curr_z + diff_z) != Short.MIN_VALUE) {
                result.set(_tx, _ty, _tz, 1);
            }
            return result;
        }

        float step_x = diff_x / steps, step_y = diff_y / steps, step_z = diff_z / steps;
        float half_step_z = step_z / 2.0f;
        float next_x = curr_x, next_y = curr_y, next_z = curr_z;
        int i_next_x, i_next_y, i_next_z, middle_z;
        short[] tmp_layers = new short[MAX_LAYERS + 1];
        short src_nearest_lower_layer, dst_nearest_lower_layer, tmp_nearest_lower_layer;

        for (int i = 0; i < steps; i++) {
            if (curr_layers[0] == 0) {
                result.set(_tx, _ty, _tz, 0);
                return result; // Здесь нет геодаты, разрешаем
            }

            next_x += step_x;
            next_y += step_y;
            next_z += step_z;
            i_next_x = (int) (next_x + 0.5f);
            i_next_y = (int) (next_y + 0.5f);
            i_next_z = (int) (next_z + 0.5f);
            middle_z = (int) (curr_z + half_step_z);

            if ((src_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, middle_z)) == Short.MIN_VALUE) {
                return result.setH(-10); // либо есть преграждающая поверхность, либо нет снизу слоя и значит это "пустота", то что за стеной или за колоной
            }

            NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
            if (curr_layers[0] == 0) {
                result.set(_tx, _ty, _tz, 0);
                return result; // Здесь нет геодаты, разрешаем
            }

            if ((dst_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, i_next_z, middle_z)) == Short.MIN_VALUE) {
                return result.setH(-11); // либо есть преграда, либо нет снизу слоя и значит это "пустота", то что за стеной или за колоной
            }

            if (curr_x == i_next_x) {
                //движемся по вертикали
                if (!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air)) {
                    return result.setH(-20);
                }
            } else if (curr_y == i_next_y) {
                //движемся по горизонтали
                if (!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air)) {
                    return result.setH(-21);
                }
            } else {
                //движемся по диагонали
                NGetLayers(curr_x, i_next_y, tmp_layers, geoIndex);
                if (tmp_layers[0] == 0) {
                    result.set(_tx, _ty, _tz, 0);
                    return result; // Здесь нет геодаты, разрешаем
                }
                if ((tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z)) == Short.MIN_VALUE) {
                    return result.setH(-30); // либо есть преграда, либо нет снизу слоя и значит это "пустота", то что за стеной или за колоной
                }

                if (!(canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air) && canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))) {
                    NGetLayers(i_next_x, curr_y, tmp_layers, geoIndex);
                    if (tmp_layers[0] == 0) {
                        result.set(_tx, _ty, _tz, 0);
                        return result; // Здесь нет геодаты, разрешаем
                    }
                    if ((tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z)) == Short.MIN_VALUE) {
                        return result.setH(-31); // либо есть преграда, либо нет снизу слоя и значит это "пустота", то что за стеной или за колоной
                    }
                    if (!canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air)) {
                        return result.setH(-32);
                    }
                    if (!canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air)) {
                        return result.setH(-33);
                    }
                }
            }

            result.set(curr_x, curr_y, curr_z);
            curr_x = i_next_x;
            curr_y = i_next_y;
            curr_z = i_next_z;
        }

        result.set(_tx, _ty, _tz, 0xFF);
        return result;
    }

    private static Location MoveInWaterCheck(int x, int y, int z, int tx, int ty, int tz, int waterZ, int geoIndex) {
        int dx = tx - x;
        int dy = ty - y;
        int dz = tz - z;
        int inc_x = sign(dx);
        int inc_y = sign(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx + dy == 0) {
            return new Location(x, y, z).geo2world();
        }
        float inc_z_for_x = dx == 0 ? 0 : dz / dx;
        float inc_z_for_y = dy == 0 ? 0 : dz / dy;
        int prev_x;
        int prev_y;
        int prev_z;
        float next_x = x;
        float next_y = y;
        float next_z = z;
        if (dx >= dy) // dy/dx <= 1
        {
            int delta_A = 2 * dy;
            int d = delta_A - dx;
            int delta_B = delta_A - 2 * dx;
            for (int i = 0; i < dx; i++) {
                prev_x = x;
                prev_y = y;
                prev_z = z;
                x = (int) next_x;
                y = (int) next_y;
                z = (int) next_z;
                if (d > 0) {
                    d += delta_B;
                    next_x += inc_x;
                    next_z += inc_z_for_x;
                    next_y += inc_y;
                    next_z += inc_z_for_y;
                } else {
                    d += delta_A;
                    next_x += inc_x;
                    next_z += inc_z_for_x;
                }

                if (next_z >= waterZ || !NLOS_WATER(x, y, z, (int) next_x, (int) next_y, (int) next_z, geoIndex)) {
                    return new Location(prev_x, prev_y, prev_z).geo2world();
                }
            }
        } else {
            int delta_A = 2 * dx;
            int d = delta_A - dy;
            int delta_B = delta_A - 2 * dy;
            for (int i = 0; i < dy; i++) {
                prev_x = x;
                prev_y = y;
                prev_z = z;
                x = (int) next_x;
                y = (int) next_y;
                z = (int) next_z;
                if (d > 0) {
                    d += delta_B;
                    next_x += inc_x;
                    next_z += inc_z_for_x;
                    next_y += inc_y;
                    next_z += inc_z_for_y;
                } else {
                    d += delta_A;
                    next_y += inc_y;
                    next_z += inc_z_for_y;
                }

                if (next_z >= waterZ || !NLOS_WATER(x, y, z, (int) next_x, (int) next_y, (int) next_z, geoIndex)) {
                    return new Location(prev_x, prev_y, prev_z).geo2world();
                }
            }
        }
        return new Location((int) next_x, (int) next_y, (int) next_z).geo2world();
    }

    /**
     * проверка проходимости по прямой
     */
    private static boolean canMove(int __x, int __y, int _z, int __tx, int __ty, int _tz, boolean withCollision, int geoIndex) {
        int _x = __x - World.MAP_MIN_X >> 4;
        int _y = __y - World.MAP_MIN_Y >> 4;
        int _tx = __tx - World.MAP_MIN_X >> 4;
        int _ty = __ty - World.MAP_MIN_Y >> 4;

        int diff_x = _tx - _x, diff_y = _ty - _y;
        int incx = sign(diff_x), incy = sign(diff_y);
        final boolean overRegionEdge = ((_x >> 11) != (_tx >> 11) || (_y >> 11) != (_ty >> 11));

        if (diff_x < 0) {
            diff_x = -diff_x;
        }
        if (diff_y < 0) {
            diff_y = -diff_y;
        }

        int pdx, pdy, es, el;

        if (diff_x > diff_y) {
            pdx = incx;
            pdy = 0;
            es = diff_y;
            el = diff_x;
        } else {
            pdx = 0;
            pdy = incy;
            es = diff_x;
            el = diff_y;
        }

        int err = el / 2;

        int curr_x = _x, curr_y = _y, curr_z = _z;
        int next_x = curr_x, next_y = curr_y, next_z = curr_z;

        short[] next_layers = new short[MAX_LAYERS + 1];
        short[] temp_layers = new short[MAX_LAYERS + 1];
        short[] curr_layers = new short[MAX_LAYERS + 1];

        NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
        if (curr_layers[0] == 0) {
            return true;
        }

        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                next_x += incx;
                next_y += incy;
            } else {
                next_x += pdx;
                next_y += pdy;
            }
            boolean regionEdge = overRegionEdge && ((next_x >> 11) != (curr_x >> 11) || (next_y >> 11) != (curr_y >> 11));

            NGetLayers(next_x, next_y, next_layers, geoIndex);
            if ((next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, next_x, next_y, next_layers, temp_layers, withCollision, regionEdge, geoIndex)) == Integer.MIN_VALUE) {
                return false;
            }

            short[] t = curr_layers;
            curr_layers = next_layers;
            next_layers = t;

            curr_x = next_x;
            curr_y = next_y;
            curr_z = next_z;
        }

        int diff_z = curr_z - _tz;
        if (Config.ALLOW_FALL_FROM_WALLS) {
            return diff_z < Config.MAX_Z_DIFF;
        }

        if (diff_z < 0) {
            diff_z = -diff_z;
        }
        return diff_z <= Config.MAX_Z_DIFF;
    }

    private static Location MoveCheck(int __x, int __y, int _z, int __tx, int __ty, boolean withCollision, boolean backwardMove, boolean returnPrev, int geoIndex) {
        int _x = __x - World.MAP_MIN_X >> 4;
        int _y = __y - World.MAP_MIN_Y >> 4;
        int _tx = __tx - World.MAP_MIN_X >> 4;
        int _ty = __ty - World.MAP_MIN_Y >> 4;

        int diff_x = _tx - _x, diff_y = _ty - _y;
        int incx = sign(diff_x), incy = sign(diff_y);
        final boolean overRegionEdge = ((_x >> 11) != (_tx >> 11) || (_y >> 11) != (_ty >> 11));

        if (diff_x < 0) {
            diff_x = -diff_x;
        }
        if (diff_y < 0) {
            diff_y = -diff_y;
        }

        int pdx, pdy, es, el;

        if (diff_x > diff_y) {
            pdx = incx;
            pdy = 0;
            es = diff_y;
            el = diff_x;
        } else {
            pdx = 0;
            pdy = incy;
            es = diff_x;
            el = diff_y;
        }

        int err = el / 2;

        int curr_x = _x, curr_y = _y, curr_z = _z;
        int next_x = curr_x, next_y = curr_y, next_z = curr_z;
        int prev_x = curr_x, prev_y = curr_y, prev_z = curr_z;

        short[] next_layers = new short[MAX_LAYERS + 1];
        short[] temp_layers = new short[MAX_LAYERS + 1];
        short[] curr_layers = new short[MAX_LAYERS + 1];

        NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                next_x += incx;
                next_y += incy;
            } else {
                next_x += pdx;
                next_y += pdy;
            }
            boolean regionEdge = overRegionEdge && ((next_x >> 11) != (curr_x >> 11) || (next_y >> 11) != (curr_y >> 11));

            NGetLayers(next_x, next_y, next_layers, geoIndex);
            if ((next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, next_x, next_y, next_layers, temp_layers, withCollision, regionEdge, geoIndex)) == Integer.MIN_VALUE) {
                break;
            }
            if (backwardMove && NcanMoveNext(next_x, next_y, next_z, next_layers, curr_x, curr_y, curr_layers, temp_layers, withCollision, regionEdge, geoIndex) == Integer.MIN_VALUE) {
                break;
            }

            short[] t = curr_layers;
            curr_layers = next_layers;
            next_layers = t;

            if (returnPrev) {
                prev_x = curr_x;
                prev_y = curr_y;
                prev_z = curr_z;
            }

            curr_x = next_x;
            curr_y = next_y;
            curr_z = next_z;
        }

        if (returnPrev) {
            curr_x = prev_x;
            curr_y = prev_y;
            curr_z = prev_z;
        }

        return new Location(curr_x, curr_y, curr_z).geo2world();
    }

    /**
     * Аналогичен CanMove, но возвращает весь пройденный путь. В гео координатах.
     */
    protected static List<Location> MoveList(int __x, int __y, int _z, int __tx, int __ty, int geoIndex, boolean onlyFullPath) {
        int _x = __x - World.MAP_MIN_X >> 4;
        int _y = __y - World.MAP_MIN_Y >> 4;
        int _tx = __tx - World.MAP_MIN_X >> 4;
        int _ty = __ty - World.MAP_MIN_Y >> 4;

        int diff_x = _tx - _x, diff_y = _ty - _y;
        int incx = sign(diff_x), incy = sign(diff_y);
        final boolean overRegionEdge = ((_x >> 11) != (_tx >> 11) || (_y >> 11) != (_ty >> 11));

        if (diff_x < 0) {
            diff_x = -diff_x;
        }
        if (diff_y < 0) {
            diff_y = -diff_y;
        }

        int pdx, pdy, es, el;

        if (diff_x > diff_y) {
            pdx = incx;
            pdy = 0;
            es = diff_y;
            el = diff_x;
        } else {
            pdx = 0;
            pdy = incy;
            es = diff_x;
            el = diff_y;
        }

        int err = el / 2;

        int curr_x = _x, curr_y = _y, curr_z = _z;
        int next_x = curr_x, next_y = curr_y, next_z = curr_z;

        short[] next_layers = new short[MAX_LAYERS + 1];
        short[] temp_layers = new short[MAX_LAYERS + 1];
        short[] curr_layers = new short[MAX_LAYERS + 1];

        NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
        if (curr_layers[0] == 0) {
            return null;
        }

        List<Location> result = new ArrayList<>(el + 1);

        result.add(new Location(curr_x, curr_y, curr_z)); // Первая точка

        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                next_x += incx;//сдвинуть прямую (сместить вверх или вниз, если цикл проходит по иксам)
                next_y += incy;//или сместить влево-вправо, если цикл проходит по y
            } else {
                next_x += pdx;//продолжить тянуть прямую дальше, т.е. сдвинуть влево или вправо, если
                next_y += pdy;//цикл идёт по иксу; сдвинуть вверх или вниз, если по y
            }
            boolean regionEdge = overRegionEdge && ((next_x >> 11) != (curr_x >> 11) || (next_y >> 11) != (curr_y >> 11));

            NGetLayers(next_x, next_y, next_layers, geoIndex);
            if ((next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, next_x, next_y, next_layers, temp_layers, false, regionEdge, geoIndex)) == Integer.MIN_VALUE) {
                if (onlyFullPath) {
                    return null;
                } else {
                    break;
                }
            }

            short[] t = curr_layers;
            curr_layers = next_layers;
            next_layers = t;

            curr_x = next_x;
            curr_y = next_y;
            curr_z = next_z;

            result.add(new Location(curr_x, curr_y, curr_z));
        }

        return result;
    }

    /**
     * Используется только для антипаровоза в AI
     */
    private static Location MoveCheckForAI(int x, int y, int z, int tx, int ty, int geoIndex) {
        int dx = tx - x;
        int dy = ty - y;
        int inc_x = sign(dx);
        int inc_y = sign(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx + dy < 2 || dx == 2 && dy == 0 || dx == 0 && dy == 2) {
            return new Location(x, y, z).geo2world();
        }
        int prev_x = x;
        int prev_y = y;
        int prev_z = z;
        int next_x = x;
        int next_y = y;
        int next_z = z;
        if (dx >= dy) // dy/dx <= 1
        {
            int delta_A = 2 * dy;
            int d = delta_A - dx;
            int delta_B = delta_A - 2 * dx;
            for (int i = 0; i < dx; i++) {
                prev_x = x;
                prev_y = y;
                prev_z = z;
                x = next_x;
                y = next_y;
                z = next_z;
                if (d > 0) {
                    d += delta_B;
                    next_x += inc_x;
                    next_y += inc_y;
                } else {
                    d += delta_A;
                    next_x += inc_x;
                }
                next_z = NcanMoveNextForAI(x, y, z, next_x, next_y, geoIndex);
                if (next_z == 0) {
                    return new Location(prev_x, prev_y, prev_z).geo2world();
                }
            }
        } else {
            int delta_A = 2 * dx;
            int d = delta_A - dy;
            int delta_B = delta_A - 2 * dy;
            for (int i = 0; i < dy; i++) {
                prev_x = x;
                prev_y = y;
                prev_z = z;
                x = next_x;
                y = next_y;
                z = next_z;
                if (d > 0) {
                    d += delta_B;
                    next_x += inc_x;
                    next_y += inc_y;
                } else {
                    d += delta_A;
                    next_y += inc_y;
                }
                next_z = NcanMoveNextForAI(x, y, z, next_x, next_y, geoIndex);
                if (next_z == 0) {
                    return new Location(prev_x, prev_y, prev_z).geo2world();
                }
            }
        }
        return new Location(next_x, next_y, next_z).geo2world();
    }

    private static boolean NcanMoveNextExCheck(int x, int y, int h, int nextx, int nexty, int hexth, short[] temp_layers, boolean regionEdge, int geoIndex) {
        NGetLayers(x, y, temp_layers, geoIndex);
        if (temp_layers[0] == 0) {
            return true;
        }

        int temp_layer;
        if ((temp_layer = FindNearestLowerLayer(temp_layers, h + Config.MIN_LAYER_HEIGHT, regionEdge)) == Integer.MIN_VALUE) {
            return false;
        }
        short temp_layer_h = (short) ((short) (temp_layer & 0x0fff0) >> 1);
        final int maxDeltaZ = regionEdge ? Config.REGION_EDGE_MAX_Z_DIFF : Config.MAX_Z_DIFF;
        if (Math.abs(temp_layer_h - hexth) >= maxDeltaZ || Math.abs(temp_layer_h - h) >= maxDeltaZ) {
            return false;
        }
        return checkNSWE((byte) (temp_layer & 0x0F), x, y, nextx, nexty);
    }

    /**
     * @return возвращает высоту следующего блока, либо Integer.MIN_VALUE если двигатся нельзя
     */
    private static int NcanMoveNext(int x, int y, int z, short[] layers, int next_x, int next_y, short[] next_layers, short[] temp_layers, boolean withCollision, boolean regionEdge, int geoIndex) {
        if (layers[0] == 0 || next_layers[0] == 0) {
            return z;
        }

        int layer, next_layer;
        if ((layer = FindNearestLowerLayer(layers, z + Config.MIN_LAYER_HEIGHT, regionEdge)) == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        byte layer_nswe = (byte) (layer & 0x0F);
        if (!checkNSWE(layer_nswe, x, y, next_x, next_y))
            return Integer.MIN_VALUE;

        short layer_h = (short) ((short) (layer & 0x0fff0) >> 1);
        if ((next_layer = FindNearestLowerLayer(next_layers, layer_h + Config.MIN_LAYER_HEIGHT, regionEdge)) == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        short next_layer_h = (short) ((short) (next_layer & 0x0fff0) >> 1);

        // если движение не по диагонали
        if (x == next_x || y == next_y) {
            if (withCollision) {
                //short[] heightNSWE = temp_layers;
                if (x == next_x) {
                    NgetHeightAndNSWE(x - 1, y, layer_h, temp_layers, geoIndex);
                    if (Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x - 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x - 1, y, x - 1, next_y)) {
                        return Integer.MIN_VALUE;
                    }

                    NgetHeightAndNSWE(x + 1, y, layer_h, temp_layers, geoIndex);
                    if (Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x + 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x + 1, y, x + 1, next_y)) {
                        return Integer.MIN_VALUE;
                    }

                    return next_layer_h;
                }

                final int maxDeltaZ = regionEdge ? Config.REGION_EDGE_MAX_Z_DIFF : Config.MAX_Z_DIFF;
                NgetHeightAndNSWE(x, y - 1, layer_h, temp_layers, geoIndex);
                if (Math.abs(temp_layers[0] - layer_h) >= maxDeltaZ || !checkNSWE(layer_nswe, x, y - 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y - 1, next_x, y - 1)) {
                    return Integer.MIN_VALUE;
                }

                NgetHeightAndNSWE(x, y + 1, layer_h, temp_layers, geoIndex);
                if (Math.abs(temp_layers[0] - layer_h) >= maxDeltaZ || !checkNSWE(layer_nswe, x, y + 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y + 1, next_x, y + 1)) {
                    return Integer.MIN_VALUE;
                }
            }

            return next_layer_h;
        }

        if (!NcanMoveNextExCheck(x, next_y, layer_h, next_x, next_y, next_layer_h, temp_layers, regionEdge, geoIndex)) {
            return Integer.MIN_VALUE;
        }
        if (!NcanMoveNextExCheck(next_x, y, layer_h, next_x, next_y, next_layer_h, temp_layers, regionEdge, geoIndex)) {
            return Integer.MIN_VALUE;
        }

        //FIXME if(withCollision)

        return next_layer_h;
    }

    /**
     * Используется только для антипаровоза в AI
     */
    private static int NcanMoveNextForAI(int x, int y, int z, int next_x, int next_y, int geoIndex) {
        short[] layers1 = new short[MAX_LAYERS + 1];
        short[] layers2 = new short[MAX_LAYERS + 1];
        NGetLayers(x, y, layers1, geoIndex);
        NGetLayers(next_x, next_y, layers2, geoIndex);

        if (layers1[0] == 0 || layers2[0] == 0) {
            return z == 0 ? 1 : z;
        }

        short h;

        short z1 = Short.MIN_VALUE;
        byte NSWE1 = NSWE_ALL;
        for (int i = 1; i <= layers1[0]; i++) {
            h = (short) ((short) (layers1[i] & 0x0fff0) >> 1);
            if (Math.abs(z - z1) > Math.abs(z - h)) {
                z1 = h;
                NSWE1 = (byte) (layers1[i] & 0x0F);
            }
        }

        if (z1 == Short.MIN_VALUE) {
            return 0;
        }

        short z2 = Short.MIN_VALUE;
        byte NSWE2 = NSWE_ALL;
        for (int i = 1; i <= layers2[0]; i++) {
            h = (short) ((short) (layers2[i] & 0x0fff0) >> 1);
            if (Math.abs(z - z2) > Math.abs(z - h)) {
                z2 = h;
                NSWE2 = (byte) (layers2[i] & 0x0F);
            }
        }

        if (z2 == Short.MIN_VALUE) {
            return 0;
        }

        if (z1 > z2 && z1 - z2 > Config.MAX_Z_DIFF) {
            return 0;
        }

        if (!checkNSWE(NSWE1, x, y, next_x, next_y) || !checkNSWE(NSWE2, next_x, next_y, x, y)) {
            return 0;
        }

        return z2 == 0 ? 1 : z2;
    }

    /**
     * в нулевую ячейку кладется длина
     *
     * @param geoX
     * @param geoY
     * @param result
     */
    public static void NGetLayers(int geoX, int geoY, short[] result, int geoIndex) {
        result[0] = 0;
        byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
        if (block == null) {
            return;
        }

        int cellX, cellY;
        int index = 0;
        // Read current block type: 0 - flat, 1 - complex, 2 - multilevel
        byte type = block[index];
        index++;

        switch (type) {
            case BLOCKTYPE_FLAT:
                short height = makeShort(block[index + 1], block[index]);
                height = (short) (height & 0x0fff0);
                result[0]++;
                result[1] = (short) ((short) (height << 1) | NSWE_ALL);
                return;
            case BLOCKTYPE_COMPLEX:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                index += (cellX << 3) + cellY << 1;
                height = makeShort(block[index + 1], block[index]);
                result[0]++;
                result[1] = height;
                return;
            case BLOCKTYPE_MULTILEVEL:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                int offset = (cellX << 3) + cellY;
                while (offset > 0) {
                    byte lc = block[index];
                    index += (lc << 1) + 1;
                    offset--;
                }
                byte layer_count = block[index];
                index++;
                if (layer_count <= 0 || layer_count > MAX_LAYERS) {
                    return;
                }
                result[0] = layer_count;
                while (layer_count > 0) {
                    result[layer_count] = makeShort(block[index + 1], block[index]);
                    layer_count--;
                    index += 2;
                }
                return;
            default:
                LOGGER.error("GeoEngine: Unknown block type");
                return;
        }
    }

    private static short NgetType(int geoX, int geoY, int geoIndex) {
        byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);

        if (block == null) {
            return 0;
        }

        return block[0];
    }

    public static int NgetHeight(int geoX, int geoY, int z, int geoIndex) {
        byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);

        if (block == null) {
            return z;
        }

        int cellX, cellY, index = 0;

        // Read current block type: 0 - flat, 1 - complex, 2 - multilevel
        byte type = block[index];
        index++;

        short height;
        switch (type) {
            case BLOCKTYPE_FLAT:
                height = makeShort(block[index + 1], block[index]);
                return (short) (height & 0x0fff0);
            case BLOCKTYPE_COMPLEX:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                index += (cellX << 3) + cellY << 1;
                height = makeShort(block[index + 1], block[index]);
                return (short) ((short) (height & 0x0fff0) >> 1); // height / 2
            case BLOCKTYPE_MULTILEVEL:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                int offset = (cellX << 3) + cellY;
                while (offset > 0) {
                    byte lc = block[index];
                    index += (lc << 1) + 1;
                    offset--;
                }
                byte layers = block[index];
                index++;
                if (layers <= 0 || layers > MAX_LAYERS) {
                    return (short) z;
                }

                int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
                int z_nearest_lower = Integer.MIN_VALUE;
                int z_nearest = Integer.MIN_VALUE;

                while (layers > 0) {
                    height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1);
                    if (height < z_nearest_lower_limit) {
                        z_nearest_lower = Math.max(z_nearest_lower, height);
                    } else if (Math.abs(z - height) < Math.abs(z - z_nearest)) {
                        z_nearest = height;
                    }
                    layers--;
                    index += 2;
                }

                return z_nearest_lower != Integer.MIN_VALUE ? z_nearest_lower : z_nearest;
            default:
                LOGGER.error("GeoEngine: Unknown blockType");
                return z;
        }
    }

    /**
     * @param geoX позиция геодаты
     * @param geoY позиция геодаты
     * @param z    координата без изменений
     * @return NSWE: 0-15
     */
    public static byte NgetNSWE(int geoX, int geoY, int z, int geoIndex) {
        byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);

        if (block == null) {
            return NSWE_ALL;
        }

        int cellX, cellY;
        int index = 0;

        // Read current block type: 0 - flat, 1 - complex, 2 - multilevel
        byte type = block[index];
        index++;

        switch (type) {
            case BLOCKTYPE_FLAT:
                return NSWE_ALL;
            case BLOCKTYPE_COMPLEX:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                index += (cellX << 3) + cellY << 1;
                short height = makeShort(block[index + 1], block[index]);
                return (byte) (height & 0x0F);
            case BLOCKTYPE_MULTILEVEL:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                int offset = (cellX << 3) + cellY;
                while (offset > 0) {
                    byte lc = block[index];
                    index += (lc << 1) + 1;
                    offset--;
                }
                byte layers = block[index];
                index++;
                if (layers <= 0 || layers > MAX_LAYERS) {
                    return NSWE_ALL;
                }

                short tempz1 = Short.MIN_VALUE;
                short tempz2 = Short.MIN_VALUE;
                int index_nswe1 = NSWE_NONE;
                int index_nswe2 = NSWE_NONE;
                int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;

                while (layers > 0) {
                    height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1); // height / 2

                    if (height < z_nearest_lower_limit) {
                        if (height > tempz1) {
                            tempz1 = height;
                            index_nswe1 = index;
                        }
                    } else if (Math.abs(z - height) < Math.abs(z - tempz2)) {
                        tempz2 = height;
                        index_nswe2 = index;
                    }

                    layers--;
                    index += 2;
                }

                if (index_nswe1 > 0) {
                    return (byte) (makeShort(block[index_nswe1 + 1], block[index_nswe1]) & 0x0F);
                }
                if (index_nswe2 > 0) {
                    return (byte) (makeShort(block[index_nswe2 + 1], block[index_nswe2]) & 0x0F);
                }

                return NSWE_ALL;
            default:
                LOGGER.error("GeoEngine: Unknown block type.");
                return NSWE_ALL;
        }
    }

    public static void NgetHeightAndNSWE(int geoX, int geoY, short z, short[] result, int geoIndex) {
        byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);

        if (block == null) {
            result[0] = z;
            result[1] = NSWE_ALL;
            return;
        }

        int cellX, cellY, index = 0;
        short height, NSWE = NSWE_ALL;

        // Read current block type: 0 - flat, 1 - complex, 2 - multilevel
        byte type = block[index];
        index++;

        switch (type) {
            case BLOCKTYPE_FLAT:
                height = makeShort(block[index + 1], block[index]);
                result[0] = (short) (height & 0x0fff0);
                result[1] = NSWE_ALL;
                return;
            case BLOCKTYPE_COMPLEX:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                index += (cellX << 3) + cellY << 1;
                height = makeShort(block[index + 1], block[index]);
                result[0] = (short) ((short) (height & 0x0fff0) >> 1); // height / 2
                result[1] = (short) (height & 0x0F);
                return;
            case BLOCKTYPE_MULTILEVEL:
                cellX = getCell(geoX);
                cellY = getCell(geoY);
                int offset = (cellX << 3) + cellY;
                while (offset > 0) {
                    byte lc = block[index];
                    index += (lc << 1) + 1;
                    offset--;
                }
                byte layers = block[index];
                index++;
                if (layers <= 0 || layers > MAX_LAYERS) {
                    result[0] = z;
                    result[1] = NSWE_ALL;
                    return;
                }

                short tempz1 = Short.MIN_VALUE;
                short tempz2 = Short.MIN_VALUE;
                int index_nswe1 = 0;
                int index_nswe2 = 0;
                int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;

                while (layers > 0) {
                    height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1); // height / 2

                    if (height < z_nearest_lower_limit) {
                        if (height > tempz1) {
                            tempz1 = height;
                            index_nswe1 = index;
                        }
                    } else if (Math.abs(z - height) < Math.abs(z - tempz2)) {
                        tempz2 = height;
                        index_nswe2 = index;
                    }

                    layers--;
                    index += 2;
                }

                if (index_nswe1 > 0) {
                    NSWE = makeShort(block[index_nswe1 + 1], block[index_nswe1]);
                    NSWE = (short) (NSWE & 0x0F);
                } else if (index_nswe2 > 0) {
                    NSWE = makeShort(block[index_nswe2 + 1], block[index_nswe2]);
                    NSWE = (short) (NSWE & 0x0F);
                }
                result[0] = tempz1 > Short.MIN_VALUE ? tempz1 : tempz2;
                result[1] = NSWE;
                return;
            default:
                LOGGER.error("GeoEngine: Unknown block type.");
                result[0] = z;
                result[1] = NSWE_ALL;
                return;
        }
    }

    private static short makeShort(byte b1, byte b0) {
        return (short) (b1 << 8 | b0 & 0xff);
    }

    /**
     * @param geoPos позиция геодаты
     * @return Block Index: 0-255
     */
    private static int getBlock(int geoPos) {
        return (geoPos >> 3) % 256;
    }

    /**
     * @param geoPos позиция геодаты
     * @return Cell Index: 0-7
     */
    private static int getCell(int geoPos) {
        return geoPos % 8;
    }

    /**
     * Создает индекс блока геодаты по заданым координатам блока.
     *
     * @param blockX блок по geoX
     * @param blockY блок по geoY
     * @return индекс блока
     */
    private static int getBlockIndex(int blockX, int blockY) {
        return (blockX << 8) + blockY;
    }

    private static byte sign(int x) {
        if (x >= 0) {
            return +1;
        }
        return -1;
    }

    /**
     * Возвращает актуальный блок для текущих геокоординат.<BR>
     * Является заготовкой для возвращения отдельніх блоков с дверьми
     *
     * @param geoX геокоордината
     * @param geoY геокоордината
     * @return текущий блок геодаты, или null если нет геодаты.
     */
    private static byte[] getGeoBlockFromGeoCoords(int geoX, int geoY, int geoIndex) {
        if (!Config.ALLOW_GEODATA) {
            return null;
        }

        int ix = geoX >> 11;
        int iy = geoY >> 11;

        if (ix < 0 || ix >= World.WORLD_SIZE_X || iy < 0 || iy >= World.WORLD_SIZE_Y) {
            return null;
        }

        int blockX = getBlock(geoX);
        int blockY = getBlock(geoY);

        int regIndex = 0;

        //Рефлект с геодатой
        if ((geoIndex & 0x0f000000) == 0x0f000000) {
            int x = (geoIndex & 0x00ff0000) >> 16;
            int y = (geoIndex & 0x0000ff00) >> 8;

            //Проверяем регион
            if (ix == x && iy == y) {
                regIndex = (geoIndex & 0x000000ff);
            }
        }

        byte[][] region = geodata[ix][iy][regIndex];
        if (region == null) {
            return null;
        }

        return region[getBlockIndex(blockX, blockY)];
    }

    /**
     * Загрузка геодаты в память
     */
    public static void load() {
        if (!Config.ALLOW_GEODATA) {
            return;
        }

        LOGGER.info("GeoEngine: Loading Geodata...");

        File geoDir = new File(Config.DATAPACK_ROOT, "geodata");

        if (!geoDir.exists() || !geoDir.isDirectory()) {
            throw new RuntimeException("GeoEngine: Files missing, loading aborted.");
        }

        int count = 0;
        for (int rx = Config.GEO_X_FIRST; rx <= Config.GEO_X_LAST; rx++) {
            for (int ry = Config.GEO_Y_FIRST; ry <= Config.GEO_Y_LAST; ry++) {
                File geoFile = new File(geoDir, String.format("%2d_%2d.l2g", rx, ry));
                if (!geoFile.exists()) {
                    continue;
                }

                LoadGeodataFile(rx, ry, geoFile);
                LoadGeodata(rx, ry, 0);

                count++;
            }
        }

        if (count == 0) {
            throw new RuntimeException("GeoEngine: Files missing, loading aborted.");
        }

        LOGGER.info("GeoEngine: Loaded " + count + " map(s), max layers: " + MAX_LAYERS);

        if (Config.COMPACT_GEO) {
            compact();
        }
    }

    /**
     * Загрузка региона геодаты.
     *
     * @param rx регион x
     * @param ry регион y
     */
    public static boolean LoadGeodataFile(int rx, int ry, File geoFile) {
        int ix = rx - Config.GEO_X_FIRST;
        int iy = ry - Config.GEO_Y_FIRST;

        String geoName = geoFile.getName();
        if ((ix < 0) || (iy < 0) || (ix > (World.MAP_MAX_X >> 15) + Math.abs(World.MAP_MIN_X >> 15)) || (iy > (World.MAP_MAX_Y >> 15) + Math.abs(World.MAP_MIN_Y >> 15))) {
            LOGGER.info("GeoEngine: File " + geoName + " was not loaded!!! ");
            return false;
        }

        LOGGER.debug("GeoEngine: Loading: " + geoName);


        try (FileChannel inChannel = new RandomAccessFile(geoFile, "r").getChannel()) {
            ByteBuffer buf = null;
            int size = 0;
            int checkSum;
            if (geoName.endsWith(".l2j")) {
                size = (int) inChannel.size();
                buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
                inChannel.read(buf);
            } else if (geoName.endsWith(".l2g")) {
                size = Math.max(4, (int) inChannel.size() - 4);
                buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
                buf.limit(4);
                inChannel.read(buf);
                buf.flip();
                checkSum = 0x814141AB ^ buf.getInt();
                buf.clear();
                inChannel.read(buf);

                buf.rewind();

                byte key = (byte) (checkSum >> 24 & 0xFF ^ checkSum >> 16 & 0xFF ^ checkSum >> 8 & 0xFF ^ checkSum & 0xFF);


                while (buf.hasRemaining()) {
                    buf.put(buf.position(), (byte) (buf.get() ^ key));
                    checkSum -= (key = buf.get(buf.position() - 1));
                }
                buf.rewind();

                if (checkSum != 0) {
                    throw new RuntimeException("Test failed!");
                }
            }


            rawgeo[ix][iy] = buf;

            if (size < BLOCKS_IN_MAP * 3) {
                throw new RuntimeException("Invalid geodata : " + geoName + "!");
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        return false;
    }

    public static void DumpGeodata(String dir) {
        new File(dir).mkdirs();
        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                if (geodata[mapX][mapY] == null) {
                    continue;
                }
                int rx = mapX + Config.GEO_X_FIRST;
                int ry = mapY + Config.GEO_Y_FIRST;
                String fName = dir + "/" + rx + "_" + ry + ".l2j";
                LOGGER.info("Dumping geo: " + fName);
                DumpGeodataFile(fName, (byte) rx, (byte) ry);
            }
        }
    }

    public static boolean DumpGeodataFile(int cx, int cy) {
        return DumpGeodataFileMap((byte) (Math.floor((float) cx / (float) 32768) + 20), (byte) (Math.floor((float) cy / (float) 32768) + 18));
    }

    public static boolean DumpGeodataFileMap(byte rx, byte ry) {
        String name = "log/" + rx + "_" + ry + ".l2j";
        return DumpGeodataFile(name, rx, ry);
    }

    public static boolean DumpGeodataFile(String name, byte rx, byte ry) {
        int ix = rx - Config.GEO_X_FIRST;
        int iy = ry - Config.GEO_Y_FIRST;

        byte[][] geoblocks = geodata[ix][iy][0];
        if (geoblocks == null) {
            return false;
        }

        File f = new File(name);
        if (f.exists()) {
            f.delete();
        }
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            for (byte[] geoblock : geoblocks) {
                os.write(geoblock);
            }
        } catch (IOException e) {
            LOGGER.error("", e);
            return false;
        }

        return true;
    }

    public static void LoadGeodata(int rx, int ry, int regIndex) {
        int ix = rx - Config.GEO_X_FIRST;
        int iy = ry - Config.GEO_Y_FIRST;

        ByteBuffer geo = rawgeo[ix][iy];

        int index = 0, orgIndex, block = 0, floor = 0;

        byte[][] blocks;

        synchronized (geodata) {
            if ((blocks = geodata[ix][iy][regIndex]) == null) {
                geodata[ix][iy][regIndex] = (blocks = new byte[BLOCKS_IN_MAP][]); // 256 * 256 блоков в регионе геодаты
            }
        }

        // Indexing geo files, so we will know where each block starts
        for (block = 0; block < BLOCKS_IN_MAP; block++) {
            byte type = geo.get(index);
            index++;

            byte[] geoBlock;
            switch (type) {
                case BLOCKTYPE_FLAT:

                    // Создаем блок геодаты
                    geoBlock = new byte[2 + 1];

                    // Читаем нужные даные с геодаты
                    geoBlock[0] = type;
                    geoBlock[1] = geo.get(index);
                    geoBlock[2] = geo.get(index + 1);

                    // Увеличиваем индекс
                    index += 2;

                    // Добавляем блок геодаты
                    blocks[block] = geoBlock;
                    break;

                case BLOCKTYPE_COMPLEX:

                    // Создаем блок геодаты
                    geoBlock = new byte[128 + 1];

                    // Читаем данные с геодаты
                    geoBlock[0] = type;

                    geo.position(index);
                    geo.get(geoBlock, 1, 128);

                    // Увеличиваем индекс
                    index += 128;

                    // Добавляем блок геодаты
                    blocks[block] = geoBlock;
                    break;

                case BLOCKTYPE_MULTILEVEL:
                    // Оригинальный индекс
                    orgIndex = index;

                    // Считаем длину блока геодаты
                    for (int b = 0; b < 64; b++) {
                        byte layers = geo.get(index);
                        MAX_LAYERS = Math.max(MAX_LAYERS, layers);
                        index += (layers << 1) + 1;
                        if (layers > floor) {
                            floor = layers;
                        }
                    }

                    // Получаем длину
                    int diff = index - orgIndex;

                    // Создаем массив геодаты
                    geoBlock = new byte[diff + 1];

                    // Читаем даные с геодаты
                    geoBlock[0] = type;

                    geo.position(orgIndex);
                    geo.get(geoBlock, 1, diff);

                    // Добавляем блок геодаты
                    blocks[block] = geoBlock;
                    break;
                default:
                    throw new RuntimeException("Invalid geodata: " + rx + "_" + ry + "!");
            }
        }
    }

    public static int NextGeoIndex(int rx, int ry, int refId) {
        if (!Config.ALLOW_GEODATA) {
            return 0;
        }

        int ix = rx - Config.GEO_X_FIRST;
        int iy = ry - Config.GEO_Y_FIRST;

        int regIndex = -1;

        synchronized (geodata) {
            byte[][][] region = geodata[ix][iy];

            //Ищем свободный блок
            for (int i = 0; i < region.length; i++) {
                if (region[i] == null) {
                    regIndex = i;
                    break;
                }
            }

            //Свободного блока нет, создаем новый
            if (regIndex == -1) {
                byte[][][] resizedRegion = new byte[(regIndex = region.length) + 1][][];
                System.arraycopy(region, 0, resizedRegion, 0, region.length);
                geodata[ix][iy] = resizedRegion;
            }

            LoadGeodata(rx, ry, regIndex);
        }

        return 0x0f000000 | (ix << 16) | (iy << 8) | regIndex;
    }

    /**
     * Освободить занятый рефлектом индекс геодаты.
     *
     * @param geoIndex
     */
    public static void FreeGeoIndex(int geoIndex) {
        if (!Config.ALLOW_GEODATA) {
            return;
        }

        //Рефлект без геодаты
        if ((geoIndex & 0x0f000000) != 0x0f000000) {
            return;
        }

        int ix = (geoIndex & 0x00ff0000) >> 16;
        int iy = (geoIndex & 0x0000ff00) >> 8;
        int regIndex = geoIndex & 0x000000ff;

        synchronized (geodata) {
            geodata[ix][iy][regIndex] = null;
        }
    }

    /**
     * Преобразовывает FLAT блоки в COMPLEX<br>
     *
     * @param ix         регион x
     * @param iy         регион y
     * @param blockIndex индекс блока в регионе
     */
    private static void copyBlock(int ix, int iy, int blockIndex)
    {
        byte[][][] region = geodata[ix][iy];

        if(region == null)
        {
            Log.add("door at null region? [" + ix + "][" + iy + "]", "doors");
            return;
        }

        byte[] block = region[blockIndex][0];
        byte blockType = block[0];

        switch(blockType)
        {
            case BLOCKTYPE_FLAT:
                short height = makeShort(block[2], block[1]);
                height &= 0x0fff0;
                height <<= 1;
                height |= NORTH;
                height |= SOUTH;
                height |= WEST;
                height |= EAST;
                byte[] newblock = new byte[129];
                newblock[0] = BLOCKTYPE_COMPLEX;
                for(int i = 1; i < 129; i += 2)
                {
                    newblock[i + 1] = (byte) (height >> 8);
                    newblock[i] = (byte) (height & 0x00ff);
                }
                region[blockIndex][0] = newblock;
                break;
            default:
                if(Config.COMPACT_GEO)
                    region[blockIndex][0] = region[blockIndex][0].clone();
                break;
        }
    }

    public static void removeGeoCollision(GeoCollision collision, int geoIndex) {
        Shape shape = collision.getShape();

        byte[][] around = collision.getGeoAround();
        if (around == null) {
            throw new RuntimeException("Attempt to remove unitialized collision: " + collision);
        }

        //Размер коллизии в клетках геодаты
        int minX = shape.getXmin() - World.MAP_MIN_X - 16 >> 4;
        int minY = shape.getYmin() - World.MAP_MIN_Y - 16 >> 4;
        int minZ = shape.getZmin();
        int maxZ = shape.getZmax();

        short height;
        byte old_nswe;

        for (int gX = 0; gX < around.length; gX++) {
            for (int gY = 0; gY < around[gX].length; gY++) {
                int geoX = minX + gX;
                int geoY = minY + gY;

                byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
                if (block == null) {
                    continue;
                }

                int cellX = getCell(geoX);
                int cellY = getCell(geoY);

                int index = 0;
                byte blockType = block[index];
                index++;

                switch (blockType) {
                    case BLOCKTYPE_COMPLEX:
                        index += (cellX << 3) + cellY << 1;

                        // Получаем высоту клетки
                        height = makeShort(block[index + 1], block[index]);
                        old_nswe = (byte) (height & 0x0F);
                        height &= 0xfff0;
                        height >>= 1;

                        // подходящий слой не найден
                        if (height < minZ || height > maxZ) {
                            break;
                        }

                        // around
                        height <<= 1;
                        height &= 0xfff0;
                        height |= old_nswe;
                        if (collision.isConcrete()) {
                            height |= around[gX][gY];
                        } else {
                            height &= ~around[gX][gY];
                        }

                        // Записываем высоту в массив
                        block[index + 1] = (byte) (height >> 8);
                        block[index] = (byte) (height & 0x00ff);
                        break;
                    case BLOCKTYPE_MULTILEVEL:
                        // Последний валидный индекс для двери
                        int neededIndex = -1;

                        // Далее следует стандартный механизм получения высоты
                        int offset = (cellX << 3) + cellY;
                        while (offset > 0) {
                            byte lc = block[index];
                            index += (lc << 1) + 1;
                            offset--;
                        }
                        byte layers = block[index];
                        index++;
                        if (layers <= 0 || layers > MAX_LAYERS) {
                            break;
                        }
                        short temph = Short.MIN_VALUE;
                        old_nswe = NSWE_ALL;
                        while (layers > 0) {
                            height = makeShort(block[index + 1], block[index]);
                            byte tmp_nswe = (byte) (height & 0x0F);
                            height &= 0xfff0;
                            height >>= 1;
                            int z_diff_last = Math.abs(minZ - temph);
                            int z_diff_curr = Math.abs(maxZ - height);
                            if (z_diff_last > z_diff_curr) {
                                old_nswe = tmp_nswe;
                                temph = height;
                                neededIndex = index;
                            }
                            layers--;
                            index += 2;
                        }

                        // подходящий слой не найден
                        if (temph == Short.MIN_VALUE || (temph < minZ || temph > maxZ)) {
                            break;
                        }

                        // around
                        temph <<= 1;
                        temph &= 0xfff0;
                        temph |= old_nswe;
                        if (collision.isConcrete()) {
                            temph |= around[gX][gY];
                        } else {
                            temph &= ~around[gX][gY];
                        }

                        // записываем высоту
                        block[neededIndex + 1] = (byte) (temph >> 8);
                        block[neededIndex] = (byte) (temph & 0x00ff);
                        break;
                }
            }
        }
    }

    public static void applyGeoCollision(GeoCollision collision, int geoIndex) {
        Shape shape = collision.getShape();
        if (shape.getXmax() == shape.getYmax() && shape.getXmax() == 0) {
            throw new RuntimeException("Attempt to add incorrect collision: " + collision);
        }

        boolean isFirstTime = false;

        //Размер коллизии в клетках геодаты
        int minX = shape.getXmin() - World.MAP_MIN_X - 16 >> 4;
        int maxX = shape.getXmax() - World.MAP_MIN_X + 16 >> 4;
        int minY = shape.getYmin() - World.MAP_MIN_Y - 16 >> 4;
        int maxY = shape.getYmax() - World.MAP_MIN_Y + 16 >> 4;
        int minZ = shape.getZmin();
        int maxZ = shape.getZmax();

        byte[][] around = collision.getGeoAround();
        if (around == null) {
            isFirstTime = true;

            //Сформируем коллизию
            byte[][] cells = new byte[maxX - minX + 1][maxY - minY + 1];
            for (int gX = minX; gX <= maxX; gX++) {
                for (int gY = minY; gY <= maxY; gY++) {
                    int x = (gX << 4) + World.MAP_MIN_X;
                    int y = (gY << 4) + World.MAP_MIN_Y;

                    loop:
                    for (int ax = x; ax < x + 16; ax++) {
                        for (int ay = y; ay < y + 16; ay++) {
                            if (shape.isInside(ax, ay)) {
                                cells[gX - minX][gY - minY] = 1;
                                break loop;
                            }
                        }
                    }
                }
            }

            around = new byte[maxX - minX + 1][maxY - minY + 1];
            for (int gX = 0; gX < cells.length; gX++) {
                for (int gY = 0; gY < cells[gX].length; gY++) {
                    if (cells[gX][gY] == 1) {
                        around[gX][gY] = NSWE_ALL;

                        byte _nswe;
                        if (gY > 0) {
                            if (cells[gX][gY - 1] == 0) {
                                _nswe = around[gX][gY - 1];
                                _nswe |= SOUTH;
                                around[gX][gY - 1] = _nswe;
                            }
                        }
                        if (gY + 1 < cells[gX].length) {
                            if (cells[gX][gY + 1] == 0) {
                                _nswe = around[gX][gY + 1];
                                _nswe |= NORTH;
                                around[gX][gY + 1] = _nswe;
                            }
                        }
                        if (gX > 0) {
                            if (cells[gX - 1][gY] == 0) {
                                _nswe = around[gX - 1][gY];
                                _nswe |= EAST;
                                around[gX - 1][gY] = _nswe;
                            }
                        }
                        if (gX + 1 < cells.length) {
                            if (cells[gX + 1][gY] == 0) {
                                _nswe = around[gX + 1][gY];
                                _nswe |= WEST;
                                around[gX + 1][gY] = _nswe;
                            }
                        }
                    }
                }
            }
            collision.setGeoAround(around);
        }

        short height;
        byte old_nswe, close_nswe;

        for (int gX = 0; gX < around.length; gX++) {
            for (int gY = 0; gY < around[gX].length; gY++) {
                int geoX = minX + gX;
                int geoY = minY + gY;

                // Попытка скопировать блок геодаты, если уже существует, то не скопируется
                //TODO: if(first_time)
                //	copyBlock(ix, iy, blockIndex);

                byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
                if (block == null) {
                    continue;
                }

                int cellX = getCell(geoX);
                int cellY = getCell(geoY);

                int index = 0;
                byte blockType = block[index];
                index++;

                switch (blockType) {
                    case BLOCKTYPE_COMPLEX:
                        index += (cellX << 3) + cellY << 1;

                        // Получаем высоту клетки
                        height = makeShort(block[index + 1], block[index]);
                        old_nswe = (byte) (height & 0x0F);
                        height &= 0xfff0;
                        height >>= 1;

                        // подходящий слой не найден
                        if (height < minZ || height > maxZ) {
                            break;
                        }

                        close_nswe = around[gX][gY];

                        if (isFirstTime) {
                            if (collision.isConcrete()) {
                                close_nswe &= old_nswe;
                            } else {
                                close_nswe &= ~old_nswe;
                            }
                            around[gX][gY] = close_nswe;
                        }

                        // around
                        height <<= 1;
                        height &= 0xfff0;
                        height |= old_nswe;

                        if (collision.isConcrete()) {
                            height &= ~close_nswe;
                        } else {
                            height |= close_nswe;
                        }

                        // Записываем высоту в массив
                        block[index + 1] = (byte) (height >> 8);
                        block[index] = (byte) (height & 0x00ff);
                        break;
                    case BLOCKTYPE_MULTILEVEL:
                        // Последний валидный индекс для двери
                        int neededIndex = -1;

                        // Далее следует стандартный механизм получения высоты
                        int offset = (cellX << 3) + cellY;
                        while (offset > 0) {
                            byte lc = block[index];
                            index += (lc << 1) + 1;
                            offset--;
                        }
                        byte layers = block[index];
                        index++;
                        if (layers <= 0 || layers > MAX_LAYERS) {
                            break;
                        }
                        short temph = Short.MIN_VALUE;
                        old_nswe = NSWE_ALL;
                        while (layers > 0) {
                            height = makeShort(block[index + 1], block[index]);
                            byte tmp_nswe = (byte) (height & 0x0F);
                            height &= 0xfff0;
                            height >>= 1;
                            int z_diff_last = Math.abs(minZ - temph);
                            int z_diff_curr = Math.abs(maxZ - height);
                            if (z_diff_last > z_diff_curr) {
                                old_nswe = tmp_nswe;
                                temph = height;
                                neededIndex = index;
                            }
                            layers--;
                            index += 2;
                        }

                        // подходящий слой не найден
                        if (temph == Short.MIN_VALUE || (temph < minZ || temph > maxZ)) {
                            break;
                        }

                        close_nswe = around[gX][gY];

                        if (isFirstTime) {
                            if (collision.isConcrete()) {
                                close_nswe &= old_nswe;
                            } else {
                                close_nswe &= ~old_nswe;
                            }
                            around[gX][gY] = close_nswe;
                        }

                        // around
                        temph <<= 1;
                        temph &= 0xfff0;
                        temph |= old_nswe;
                        if (collision.isConcrete()) {
                            temph &= ~close_nswe;
                        } else {
                            temph |= close_nswe;
                        }

                        // записываем высоту
                        block[neededIndex + 1] = (byte) (temph >> 8);
                        block[neededIndex] = (byte) (temph & 0x00ff);
                        break;
                }
            }
        }
    }

    /**
     * загружает заранее сгенерированые карты соовпадений в блоках и благодаря им оптимизирует размещение геодаты в памяти
     *
     * @return количество оптимизированых блоков
     */
    public static void compact() {
        long total = 0, optimized = 0;
        BlockLink[] links;
        byte[][][] link_region;

        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                if (geodata[mapX][mapY] == null) {
                    continue;
                }
                total += BLOCKS_IN_MAP;
                links = GeoOptimizer.loadBlockMatches("geodata/matches/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".matches");
                if (links == null) {
                    continue;
                }
                for (BlockLink link : links) {
                    link_region = geodata[link.linkMapX][link.linkMapY];
                    if (link_region == null) {
                        continue;
                    }
                    link_region[link.linkBlockIndex][0] = geodata[mapX][mapY][link.blockIndex][0];
                    optimized++;
                }
            }
        }

        LOGGER.info(String.format("GeoEngine: - Compacted %d of %d blocks...", optimized, total));
    }

    /**
     * сравнение двух байт-массивов
     *
     * @param a1
     * @param a2
     * @return
     */
    public static boolean equalsData(byte[] a1, byte[] a2) {
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * сравнение двух блоков геодаты
     *
     * @param mapX1
     * @param mapY1
     * @param blockIndex1
     * @param mapX2
     * @param mapY2
     * @param blockIndex2
     * @return
     */
    public static boolean compareGeoBlocks(int mapX1, int mapY1, int blockIndex1, int mapX2, int mapY2, int blockIndex2) {
        return equalsData(geodata[mapX1][mapY1][blockIndex1][0], geodata[mapX2][mapY2][blockIndex2][0]);
    }

    private static void initChecksums() {
        LOGGER.info("GeoEngine: - Generating Checksums...");
        new File(Config.DATAPACK_ROOT, "geodata/checksum").mkdirs();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        GeoOptimizer.checkSums = new int[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][];
        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                if (geodata[mapX][mapY] != null) {
                    executor.execute(new GeoOptimizer.CheckSumLoader(mapX, mapY, geodata[mapX][mapY]));
                }
            }
        }
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }

    private static void initBlockMatches(int maxScanRegions) {
        LOGGER.info("GeoEngine: Generating Block Matches...");
        new File(Config.DATAPACK_ROOT, "geodata/matches").mkdirs();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                if (geodata[mapX][mapY] != null && GeoOptimizer.checkSums != null && GeoOptimizer.checkSums[mapX][mapY] != null) {
                    executor.execute(new GeoOptimizer.GeoBlocksMatchFinder(mapX, mapY, maxScanRegions));
                }
            }
        }
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }

    public static void deleteChecksumFiles() {
        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                if (geodata[mapX][mapY] == null) {
                    continue;
                }
                new File(Config.DATAPACK_ROOT, "geodata/checksum/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".crc").delete();
            }
        }
    }

    public static void genBlockMatches(int maxScanRegions) {
        initChecksums();
        initBlockMatches(maxScanRegions);
    }

    public static void unload() {
        for (int mapX = 0; mapX < World.WORLD_SIZE_X; mapX++) {
            for (int mapY = 0; mapY < World.WORLD_SIZE_Y; mapY++) {
                geodata[mapX][mapY] = null;
            }
        }
    }
}