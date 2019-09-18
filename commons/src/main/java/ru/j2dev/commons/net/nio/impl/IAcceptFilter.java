package ru.j2dev.commons.net.nio.impl;

import java.nio.channels.SocketChannel;

public interface IAcceptFilter {
    boolean accept(final SocketChannel p0);
}
