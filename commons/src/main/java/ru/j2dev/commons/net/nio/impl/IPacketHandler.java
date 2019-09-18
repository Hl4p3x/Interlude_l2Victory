package ru.j2dev.commons.net.nio.impl;

import java.nio.ByteBuffer;

public interface IPacketHandler<T extends MMOClient> {
    ReceivablePacket<T> handlePacket(final ByteBuffer p0, final T p1);
}
