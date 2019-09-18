package ru.j2dev.commons.net.io;

import java.nio.ByteBuffer;

public interface IIOCipher {
    boolean encrypt(final ByteBuffer p0, final int p1, final int p2);

    boolean decrypt(final ByteBuffer p0, final int p1, final int p2);
}