package ru.j2dev.gameserver.network.lineage2.cgm.rc4;

import java.nio.ByteBuffer;

abstract class RC4 {
    private final int[] _s;
    private int _i;
    private int _j;

    public RC4(final byte[] key) {
        _s = new int[256];
        _i = 0;
        _j = 0;
        ksn(key);
    }

    private void ksn(final byte[] iv) {
        _i = 0;
        _j = 0;
        for (int i = 0; i < 256; ++i) {
            _s[i] = (byte) i;
        }
        int j = 0;
        for (int i = 0; i < 256; ++i) {
            j = (0xFF & j + _s[i] + iv[i % iv.length]);
            final int t = _s[i] & 0xFF;
            _s[i] = _s[j];
            _s[j] = t;
        }
    }

    protected byte prng() {
        _i = (_i + 1 & 0xFF);
        _j = (_j + _s[_i] & 0xFF);
        final int t = _s[_i];
        _s[_i] = _s[_j];
        _s[_j] = t;
        return (byte) _s[_s[_i] + _s[_j] & 0xFF];
    }

    protected void rc4(final byte[] buff, final int offset, final int length) {
        final int end = offset + length;
        final int[] s = _s;
        int i = _i;
        int j = _j;
        for (int idx = offset; idx < end; ++idx) {
            i = (i + 1 & 0xFF);
            j = (j + s[i] & 0xFF);
            int t = s[i];
            s[i] = s[j];
            s[j] = t;
            t = s[s[i] + s[j] & 0xFF];
            buff[idx] ^= (byte) t;
        }
        _i = i;
        _j = j;
    }

    protected void rc4(final ByteBuffer buff, final int pos, final int len) {
        final int end = pos + len;
        final int[] s = _s;
        int i = _i;
        int j = _j;
        for (int idx = pos; idx < end; ++idx) {
            i = (i + 1 & 0xFF);
            j = (j + s[i] & 0xFF);
            int t = s[i];
            s[i] = s[j];
            s[j] = t;
            t = s[s[i] + s[j] & 0xFF];
            buff.put(idx, (byte) (buff.get(idx) ^ t));
        }
        _i = i;
        _j = j;
    }
}