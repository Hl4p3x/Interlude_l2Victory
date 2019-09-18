package ru.j2dev.gameserver.handler.chat;

import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public interface IChatHandler {
    void say();

    ChatType getType();
}
