package ru.j2dev.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class SendablePacket<T extends MMOClient> extends ru.j2dev.commons.net.nio.SendablePacket<T> {
    @Override
    protected ByteBuffer getByteBuffer() {
        return ((SelectorThread) Thread.currentThread()).getWriteBuffer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getClient() {
        return (T) ((SelectorThread) Thread.currentThread()).getWriteClient();
    }

    @Override
    protected abstract boolean write();
}
