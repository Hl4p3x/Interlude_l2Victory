package ru.j2dev.commons.net.nio.impl;

public interface IClientFactory<T extends MMOClient> {
    T create(final MMOConnection<T> p0);
}
