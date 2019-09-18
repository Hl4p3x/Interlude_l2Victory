package ru.j2dev.commons.net.nio;

import java.nio.ByteBuffer;

public abstract class AbstractPacket<T> {
    protected abstract ByteBuffer getByteBuffer();

    public abstract T getClient();
}
