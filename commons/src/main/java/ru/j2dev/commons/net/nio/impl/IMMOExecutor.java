package ru.j2dev.commons.net.nio.impl;

public interface IMMOExecutor<T extends MMOClient> {
    void execute(final Runnable p0);
}
