package ru.j2dev.gameserver.network.lineage2.cgm.rc4;

import java.nio.ByteBuffer;

public final class RC4Decrypt extends RC4 {
    public RC4Decrypt(final byte[] key) {
        super(key);
    }

    public void decrypt(final byte[] buff, final int offset, final int length) {
        rc4(buff, offset, length);
    }

    public void decrypt(final byte[] buff) {
        rc4(buff, 0, buff.length);
    }

    public void decrypt(final ByteBuffer buff, final int pos, final int len) {
        rc4(buff, pos, len);
    }

    public void decrypt(final ByteBuffer buff) {
        rc4(buff, buff.position(), buff.remaining());
    }
}