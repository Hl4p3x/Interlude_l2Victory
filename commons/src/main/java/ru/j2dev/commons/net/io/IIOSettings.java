package ru.j2dev.commons.net.io;

import java.net.InetSocketAddress;

public interface IIOSettings<IOCli extends IOClient<? extends IOContext<IOCli>>> {
    IOExecutor getIOExecutor();

    IAcceptFilter getAcceptFilter();

    InetSocketAddress getBindAddress();

    int getBindBacklog();

    IClientFactory<IOCli> getClientFactory();

    IPacketHandler<IOCli> getPacketHandler();

    boolean useNoDelay();

    boolean closeOnReceiveEOS();

    boolean useDirectBuffer();
}