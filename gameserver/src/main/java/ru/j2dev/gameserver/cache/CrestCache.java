package ru.j2dev.gameserver.cache;

import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.IntMap;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CrestCache {
    public static final int ALLY_CREST_SIZE = 192;
    public static final int CREST_SIZE = 256;
    public static final int LARGE_CREST_SIZE = 2176;
    private static final Logger LOGGER = LoggerFactory.getLogger(CrestCache.class);

    private final TIntIntHashMap _pledgeCrestId = new TIntIntHashMap();
    private final TIntIntHashMap _pledgeCrestLargeId = new TIntIntHashMap();
    private final TIntIntHashMap _allyCrestId = new TIntIntHashMap();
    private final IntMap<byte[]> _pledgeCrest = new IntMap<>();
    private final IntMap<byte[]> _pledgeCrestLarge = new IntMap<>();
    private final IntMap<byte[]> _allyCrest = new IntMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private CrestCache() {
        load();
    }

    public static CrestCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        protected static final CrestCache INSTANCE = new CrestCache();
    }


    /**
     * Генерирует уникальный положительный ID на основе данных: ID клана/альянса и значка
     *
     * @param pledgeId ID клана или альянса
     * @param data     данные значка
     * @return ID значка в "кэше"
     */
    public static int getCrestId(final int pledgeId, final byte[] data) {
        int ret = 0;
        for (final byte element : data) {
            ret = 7 * ret + element;
        }
        return Math.abs(ret + pledgeId);
    }

    /**
     * Проверяет byte[] содержащий dds картинку на валидность
     */
    public static boolean isValidCrestData(final byte[] data, final int size) {
        final DDSHeader ddsHeader = new DDSHeader();
        ddsHeader.read(data);
        if (ddsHeader.magic != 0x20534444) {
            return false;
        }
        if (ddsHeader.size != 0x7C) {
            return false;
        }
        if (ddsHeader.flags != 0x81007) {
            return false;
        }
        if (ddsHeader.height % 4 != 0 || ddsHeader.height > 64 || ddsHeader.height < 4) {
            return false;
        }
        if (ddsHeader.width % 4 != 0 || ddsHeader.width > 64 || ddsHeader.width < 4) {
            return false;
        }
        if (ddsHeader.pfFlags != 0x04 || ddsHeader.pfFourCC != 0x31545844 || ddsHeader.pfSize != 0x20) {
            return false;
        }
        if (ddsHeader.mipMapCount != 0x00) {
            return false;
        }
        if (ddsHeader.pitch != (size - 0x80)) {
            return false;
        }
        if (ddsHeader.depth != 0x00) {
            return false;
        }
        if (ddsHeader.ddsCaps1 != 0x00) {
            return false;
        }
        if (ddsHeader.ddsCaps2 != 0x00) {
            return false;
        }
        if (ddsHeader.ddsCaps3 != 0x00) {
            return false;
        }
        return ddsHeader.ddsCaps4 == 0x00;
    }

    public void load() {
        int count = 0;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT clan_id, crest FROM clan_data WHERE crest IS NOT NULL");
            rset = statement.executeQuery();
            while (rset.next()) {
                count++;
                final int pledgeId = rset.getInt("clan_id");
                final byte[] crest = rset.getBytes("crest");
                final int crestId = getCrestId(pledgeId, crest);
                _pledgeCrestId.put(pledgeId, crestId);
                _pledgeCrest.put(crestId, crest);
            }
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT clan_id, largecrest FROM clan_data WHERE largecrest IS NOT NULL");
            rset = statement.executeQuery();
            while (rset.next()) {
                count++;
                final int pledgeId = rset.getInt("clan_id");
                final byte[] crest = rset.getBytes("largecrest");
                final int crestId = getCrestId(pledgeId, crest);
                _pledgeCrestLargeId.put(pledgeId, crestId);
                _pledgeCrestLarge.put(crestId, crest);
            }
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT ally_id, crest FROM ally_data WHERE crest IS NOT NULL");
            rset = statement.executeQuery();
            while (rset.next()) {
                count++;
                final int pledgeId = rset.getInt("ally_id");
                final byte[] crest = rset.getBytes("crest");
                final int crestId = getCrestId(pledgeId, crest);
                _allyCrestId.put(pledgeId, crestId);
                _allyCrest.put(crestId, crest);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        LOGGER.info("CrestCache: Loaded " + count + " crests");
    }

    public byte[] getPledgeCrest(final int crestId) {
        byte[] crest;
        readLock.lock();
        try {
            crest = _pledgeCrest.get(crestId);
        } finally {
            readLock.unlock();
        }
        return crest;
    }

    public byte[] getPledgeCrestLarge(final int crestId) {
        byte[] crest;
        readLock.lock();
        try {
            crest = _pledgeCrestLarge.get(crestId);
        } finally {
            readLock.unlock();
        }
        return crest;
    }

    public byte[] getAllyCrest(final int crestId) {
        byte[] crest;
        readLock.lock();
        try {
            crest = _allyCrest.get(crestId);
        } finally {
            readLock.unlock();
        }
        return crest;
    }

    public int getPledgeCrestId(final int pledgeId) {
        int crestId;
        readLock.lock();
        try {
            crestId = _pledgeCrestId.get(pledgeId);
        } finally {
            readLock.unlock();
        }
        return crestId;
    }

    public int getPledgeCrestLargeId(final int pledgeId) {
        int crestId;
        readLock.lock();
        try {
            crestId = _pledgeCrestLargeId.get(pledgeId);
        } finally {
            readLock.unlock();
        }
        return crestId;
    }

    public int getAllyCrestId(final int pledgeId) {
        int crestId;
        readLock.lock();
        try {
            crestId = _allyCrestId.get(pledgeId);
        } finally {
            readLock.unlock();
        }
        return crestId;
    }

    public void removePledgeCrest(final int pledgeId) {
        writeLock.lock();
        try {
            _pledgeCrest.remove(_pledgeCrestId.remove(pledgeId));
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
            statement.setNull(1, -3);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void removePledgeCrestLarge(final int pledgeId) {
        writeLock.lock();
        try {
            _pledgeCrestLarge.remove(_pledgeCrestLargeId.remove(pledgeId));
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
            statement.setNull(1, -3);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void removeAllyCrest(final int pledgeId) {
        writeLock.lock();
        try {
            _allyCrest.remove(_allyCrestId.remove(pledgeId));
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
            statement.setNull(1, -3);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public int savePledgeCrest(final int pledgeId, final byte[] crest) {
        final int crestId = getCrestId(pledgeId, crest);
        writeLock.lock();
        try {
            _pledgeCrestId.put(pledgeId, crestId);
            _pledgeCrest.put(crestId, crest);
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
            statement.setBytes(1, crest);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return crestId;
    }

    public int savePledgeCrestLarge(final int pledgeId, final byte[] crest) {
        final int crestId = getCrestId(pledgeId, crest);
        writeLock.lock();
        try {
            _pledgeCrestLargeId.put(pledgeId, crestId);
            _pledgeCrestLarge.put(crestId, crest);
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
            statement.setBytes(1, crest);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return crestId;
    }

    public int saveAllyCrest(final int pledgeId, final byte[] crest) {
        final int crestId = getCrestId(pledgeId, crest);
        writeLock.lock();
        try {
            _allyCrestId.put(pledgeId, crestId);
            _allyCrest.put(crestId, crest);
        } finally {
            writeLock.unlock();
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
            statement.setBytes(1, crest);
            statement.setInt(2, pledgeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return crestId;
    }

    public static class DDSHeader {
        private int magic;
        private int size;
        private int flags;
        private int height;
        private int width;
        private int pitch;
        private int depth;
        private int mipMapCount;
        private int alphaBitDepth;
        private int reserved1;
        private int surface;
        private int colorSpaceLowValue;
        private int colorSpaceHighValue;
        private int destBltColorSpaceLowValue;
        private int destBltColorSpaceHighValue;
        private int srcOverlayColorSpaceLowValue;
        private int srcOverlayColorSpaceHighValue;
        private int srcBltColorSpaceLowValue;
        private int srcBltColorSpaceHighValue;
        private int pfSize;
        private int pfFlags;
        private int pfFourCC;
        private int pfRGBBitCount;
        private int pfRBitMask;
        private int pfGBitMask;
        private int pfBBitMask;
        private int pfABitMask;
        private int ddsCaps1;
        private int ddsCaps2;
        private int ddsCaps3;
        private int ddsCaps4;
        private int textureStage;

        public void read(final byte[] array) {
            ByteBuffer buf = ByteBuffer.wrap(array);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            magic = buf.getInt();
            size = buf.getInt();
            flags = buf.getInt();
            height = buf.getInt();
            width = buf.getInt();
            pitch = buf.getInt();
            depth = buf.getInt();
            mipMapCount = buf.getInt();
            alphaBitDepth = buf.getInt();
            reserved1 = buf.getInt();
            surface = buf.getInt();
            colorSpaceLowValue = buf.getInt();
            colorSpaceHighValue = buf.getInt();
            destBltColorSpaceLowValue = buf.getInt();
            destBltColorSpaceHighValue = buf.getInt();
            srcOverlayColorSpaceLowValue = buf.getInt();
            srcOverlayColorSpaceHighValue = buf.getInt();
            srcBltColorSpaceLowValue = buf.getInt();
            srcBltColorSpaceHighValue = buf.getInt();
            pfSize = buf.getInt();
            pfFlags = buf.getInt();
            pfFourCC = buf.getInt();
            pfRGBBitCount = buf.getInt();
            pfRBitMask = buf.getInt();
            pfGBitMask = buf.getInt();
            pfBBitMask = buf.getInt();
            pfABitMask = buf.getInt();
            ddsCaps1 = buf.getInt();
            ddsCaps2 = buf.getInt();
            ddsCaps3 = buf.getInt();
            ddsCaps4 = buf.getInt();
            textureStage = buf.getInt();
        }
    }
}
