package ru.j2dev.gameserver.geodata.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.World;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class GeoOptimizer {
    private static final Logger log = LoggerFactory.getLogger(GeoOptimizer.class);
    private static final byte version = 1;
    public static int[][][] checkSums;

    public static BlockLink[] loadBlockMatches(final String fileName) {
        final File f = new File(Config.DATAPACK_ROOT, fileName);
        if (!f.exists()) {
            return null;
        }
        try {
            final FileChannel roChannel = new RandomAccessFile(f, "r").getChannel();

            final int count = (int) ((roChannel.size() - 1) / 6);
            final ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0, roChannel.size());
            roChannel.close();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (buffer.get() != version) {
                return null;
            }

            final BlockLink[] links = new BlockLink[count];
            for (int i = 0; i < links.length; i++) {
                links[i] = new BlockLink(buffer.getShort(), buffer.get(), buffer.get(), buffer.getShort());
            }
            return links;

        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static class GeoBlocksMatchFinder extends RunnableImpl {
        private final int geoX, geoY, rx, ry, maxScanRegions;
        private final String fileName;

        public GeoBlocksMatchFinder(final int _geoX, final int _geoY, final int _maxScanRegions) {
            super();
            geoX = _geoX;
            geoY = _geoY;
            rx = geoX + Config.GEO_X_FIRST;
            ry = geoY + Config.GEO_Y_FIRST;
            maxScanRegions = _maxScanRegions;
            fileName = "geodata/matches/" + rx + "_" + ry + ".matches";
        }

        private boolean exists() {
            return new File(Config.DATAPACK_ROOT, fileName).exists();
        }

        private void saveToFile(final BlockLink[] links) {
            log.info("Saving matches to: " + fileName);
            try {
                final File f = new File(Config.DATAPACK_ROOT, fileName);
                if (f.exists()) {
                    f.delete();
                }
                final FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
                final ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0, links.length * 6 + 1);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(version);
                for (BlockLink link : links) {
                    buffer.putShort((short) link.blockIndex);
                    buffer.put(link.linkMapX);
                    buffer.put(link.linkMapY);
                    buffer.putShort((short) link.linkBlockIndex);
                }
                wChannel.close();
            } catch (Exception e) {
                log.error("", e);
            }
        }

        private void calcMatches(final int[] curr_checkSums, final int mapX, final int mapY, final List<BlockLink> putlinks, final boolean[] notready) {
            final int[] next_checkSums = checkSums[mapX][mapY];
            if (next_checkSums == null) {
                return;
            }

            int startIdx2;
            for (int blockIdx = 0; blockIdx < GeoEngine.BLOCKS_IN_MAP; blockIdx++) {
                if (notready[blockIdx]) {
                    startIdx2 = next_checkSums == curr_checkSums ? blockIdx + 1 : 0;
                    for (int blockIdx2 = startIdx2; blockIdx2 < GeoEngine.BLOCKS_IN_MAP; blockIdx2++) {
                        if (curr_checkSums[blockIdx] == next_checkSums[blockIdx2]) {
                            if (GeoEngine.compareGeoBlocks(geoX, geoY, blockIdx, mapX, mapY, blockIdx2)) {
                                putlinks.add(new BlockLink(blockIdx, (byte) mapX, (byte) mapY, blockIdx2));
                                notready[blockIdx] = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        private BlockLink[] gen() {
            log.info("Searching matches for " + rx + "_" + ry);
            long started = System.currentTimeMillis();

            final boolean[] notready = new boolean[GeoEngine.BLOCKS_IN_MAP];
            for (int i = 0; i < GeoEngine.BLOCKS_IN_MAP; i++) {
                notready[i] = true;
            }

            final List<BlockLink> links = new ArrayList<>();
            final int[] _checkSums = checkSums[geoX][geoY];

            int n = 0;
            for (int mapX = geoX; mapX < World.WORLD_SIZE_X; mapX++) {
                final int startgeoY = mapX == geoX ? geoY : 0;
                for (int mapY = startgeoY; mapY < World.WORLD_SIZE_Y; mapY++) {
                    //log.info("Searching matches for " + rx + "_" + ry + " in " + (mapX + GeoEngine.MAP_X_FIRST) + "_" + (mapY + GeoEngine.MAP_Y_FIRST) + ", already found matches: " + links.size());
                    calcMatches(_checkSums, mapX, mapY, links, notready);
                    n++;
                    if (maxScanRegions > 0 && maxScanRegions == n) {
                        return links.toArray(new BlockLink[0]);
                    }
                }
            }

            started = System.currentTimeMillis() - started;
            log.info("Founded " + links.size() + " matches for " + rx + "_" + ry + " in " + started / 1000f + "s");
            return links.toArray(new BlockLink[0]);
        }

        @Override
        public void runImpl() {
            if (!exists()) {
                BlockLink[] links = gen();
                saveToFile(links);
            }
        }
    }

    public static class CheckSumLoader extends RunnableImpl {
        private final int geoX, geoY, rx, ry;
        private final byte[][][] region;
        private final String fileName;

        public CheckSumLoader(final int _geoX, final int _geoY, final byte[][][] _region) {
            super();
            geoX = _geoX;
            geoY = _geoY;
            rx = geoX + Config.GEO_X_FIRST;
            ry = _geoY + Config.GEO_Y_FIRST;
            region = _region;
            fileName = "geodata/checksum/" + rx + "_" + ry + ".crc";
        }

        private boolean loadFromFile() {
            final File GeoCrc = new File(Config.DATAPACK_ROOT, fileName);
            if (!GeoCrc.exists()) {
                return false;
            }
            try {
                final FileChannel roChannel = new RandomAccessFile(GeoCrc, "r").getChannel();
                if (roChannel.size() != GeoEngine.BLOCKS_IN_MAP * 4) {
                    roChannel.close();
                    return false;
                }

                final ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0, roChannel.size());
                roChannel.close();
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                final int[] _checkSums = new int[GeoEngine.BLOCKS_IN_MAP];
                for (int i = 0; i < GeoEngine.BLOCKS_IN_MAP; i++) {
                    _checkSums[i] = buffer.getInt();
                }
                checkSums[geoX][geoY] = _checkSums;
                return true;

            } catch (Exception e) {
                log.error("", e);
                return false;
            }
        }

        private void saveToFile() {
            log.info("Saving checksums to: " + fileName);
            final FileChannel wChannel;
            try {
                final File f = new File(Config.DATAPACK_ROOT, fileName);
                if (f.exists()) {
                    f.delete();
                }
                wChannel = new RandomAccessFile(f, "rw").getChannel();
                final ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0, GeoEngine.BLOCKS_IN_MAP * 4);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                int[] _checkSums = checkSums[geoX][geoY];
                for (int i = 0; i < GeoEngine.BLOCKS_IN_MAP; i++) {
                    buffer.putInt(_checkSums[i]);
                }
                wChannel.close();
            } catch (Exception e) {
                log.error("", e);
            }
        }

        private void gen() {
            log.info("Generating checksums for " + rx + "_" + ry);
            final int[] _checkSums = new int[GeoEngine.BLOCKS_IN_MAP];
            final CRC32 crc32 = new CRC32();
            for (int i = 0; i < GeoEngine.BLOCKS_IN_MAP; i++) {
                crc32.update(region[i][0]);
                _checkSums[i] = (int) (~crc32.getValue());
                crc32.reset();
            }
            checkSums[geoX][geoY] = _checkSums;
        }

        @Override
        public void runImpl() {
            if (!loadFromFile()) {
                gen();
                saveToFile();
            }
        }
    }

    public static class BlockLink {
        public final int blockIndex, linkBlockIndex;
        public final byte linkMapX, linkMapY;

        public BlockLink(final short _blockIndex, final byte _linkMapX, final byte _linkMapY, final short _linkBlockIndex) {
            blockIndex = _blockIndex & 0xFFFF;
            linkMapX = _linkMapX;
            linkMapY = _linkMapY;
            linkBlockIndex = _linkBlockIndex & 0xFFFF;
        }

        public BlockLink(final int _blockIndex, final byte _linkMapX, final byte _linkMapY, final int _linkBlockIndex) {
            blockIndex = _blockIndex & 0xFFFF;
            linkMapX = _linkMapX;
            linkMapY = _linkMapY;
            linkBlockIndex = _linkBlockIndex & 0xFFFF;
        }
    }
}