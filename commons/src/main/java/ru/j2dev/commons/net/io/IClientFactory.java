package ru.j2dev.commons.net.io;

public interface IClientFactory<IOCli extends IOClient<? extends IOContext<IOCli>>> {
    IOCli create(final IOContext<IOCli> p0);
}