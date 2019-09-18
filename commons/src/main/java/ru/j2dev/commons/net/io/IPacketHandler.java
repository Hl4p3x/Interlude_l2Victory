package ru.j2dev.commons.net.io;

import java.nio.ByteBuffer;

public interface IPacketHandler<IOCli extends IOClient<? extends IOContext<IOCli>>> {
    ReceivablePacket<IOCli> handle(final ByteBuffer p0, final IOCli p1);
}