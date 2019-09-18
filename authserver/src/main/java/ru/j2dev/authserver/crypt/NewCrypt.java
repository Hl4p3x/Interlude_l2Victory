package ru.j2dev.authserver.crypt;

import java.io.IOException;

public class NewCrypt {
    private final BlowfishEngine _crypt;
    private final BlowfishEngine _decrypt;

    public NewCrypt(final byte[] blowfishKey) {
        (_crypt = new BlowfishEngine()).init(true, blowfishKey);
        (_decrypt = new BlowfishEngine()).init(false, blowfishKey);
    }

    public NewCrypt(final String key) {
        this(key.getBytes());
    }

    public static boolean verifyChecksum(final byte[] raw) {
        return verifyChecksum(raw, 0, raw.length);
    }

    public static boolean verifyChecksum(final byte[] raw, final int offset, final int size) {
        if ((size & 0x3) != 0x0 || size <= 4) {
            return false;
        }
        long chksum = 0L;
        final int count = size - 4;
        long check = -1L;
        int i;
        for (i = offset; i < count; i += 4) {
            check = (raw[i] & 0xFF);
            check |= (raw[i + 1] << 8 & 0xFF00);
            check |= (raw[i + 2] << 16 & 0xFF0000);
            check |= (raw[i + 3] << 24 & 0xFF000000);
            chksum ^= check;
        }
        check = (raw[i] & 0xFF);
        check |= (raw[i + 1] << 8 & 0xFF00);
        check |= (raw[i + 2] << 16 & 0xFF0000);
        check |= (raw[i + 3] << 24 & 0xFF000000);
        return check == chksum;
    }

    public static void appendChecksum(final byte[] raw) {
        appendChecksum(raw, 0, raw.length);
    }

    public static void appendChecksum(final byte[] raw, final int offset, final int size) {
        long chksum = 0L;
        int count;
        int i;
        for (count = size - 4, i = offset; i < count; i += 4) {
            long ecx = raw[i] & 0xFF;
            ecx |= (raw[i + 1] << 8 & 0xFF00);
            ecx |= (raw[i + 2] << 16 & 0xFF0000);
            ecx |= (raw[i + 3] << 24 & 0xFF000000);
            chksum ^= ecx;
        }
        long ecx = raw[i] & 0xFF;
        ecx |= (raw[i + 1] << 8 & 0xFF00);
        ecx |= (raw[i + 2] << 16 & 0xFF0000);
        ecx |= (raw[i + 3] << 24 & 0xFF000000);
        raw[i] = (byte) (chksum & 0xFFL);
        raw[i + 1] = (byte) (chksum >> 8 & 0xFFL);
        raw[i + 2] = (byte) (chksum >> 16 & 0xFFL);
        raw[i + 3] = (byte) (chksum >> 24 & 0xFFL);
    }

    public static void encXORPass(final byte[] raw, final int key) {
        encXORPass(raw, 0, raw.length, key);
    }

    public static void encXORPass(final byte[] raw, final int offset, final int size, final int key) {
        final int stop = size - 8;
        int pos = 4 + offset;
        int ecx = key;
        while (pos < stop) {
            int edx = raw[pos] & 0xFF;
            edx |= (raw[pos + 1] & 0xFF) << 8;
            edx |= (raw[pos + 2] & 0xFF) << 16;
            edx |= (raw[pos + 3] & 0xFF) << 24;
            ecx += edx;
            edx ^= ecx;
            raw[pos++] = (byte) (edx & 0xFF);
            raw[pos++] = (byte) (edx >> 8 & 0xFF);
            raw[pos++] = (byte) (edx >> 16 & 0xFF);
            raw[pos++] = (byte) (edx >> 24 & 0xFF);
        }
        raw[pos++] = (byte) (ecx & 0xFF);
        raw[pos++] = (byte) (ecx >> 8 & 0xFF);
        raw[pos++] = (byte) (ecx >> 16 & 0xFF);
        raw[pos] = (byte) (ecx >> 24 & 0xFF);
    }

    public byte[] decrypt(final byte[] raw) throws IOException {
        final byte[] result = new byte[raw.length];
        for (int count = raw.length / 8, i = 0; i < count; ++i) {
            _decrypt.processBlock(raw, i * 8, result, i * 8);
        }
        return result;
    }

    public void decrypt(final byte[] raw, final int offset, final int size) throws IOException {
        final byte[] result = new byte[size];
        for (int count = size / 8, i = 0; i < count; ++i) {
            _decrypt.processBlock(raw, offset + i * 8, result, i * 8);
        }
        System.arraycopy(result, 0, raw, offset, size);
    }

    public byte[] crypt(final byte[] raw) throws IOException {
        final int count = raw.length / 8;
        final byte[] result = new byte[raw.length];
        for (int i = 0; i < count; ++i) {
            _crypt.processBlock(raw, i * 8, result, i * 8);
        }
        return result;
    }

    public void crypt(final byte[] raw, final int offset, final int size) throws IOException {
        final int count = size / 8;
        final byte[] result = new byte[size];
        for (int i = 0; i < count; ++i) {
            _crypt.processBlock(raw, offset + i * 8, result, i * 8);
        }
        System.arraycopy(result, 0, raw, offset, size);
    }
}
