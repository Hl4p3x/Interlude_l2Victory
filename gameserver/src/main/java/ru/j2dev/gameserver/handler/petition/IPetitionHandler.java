package ru.j2dev.gameserver.handler.petition;

import ru.j2dev.gameserver.model.Player;

public interface IPetitionHandler {
    void handle(final Player p0, final int p1, final String p2);
}
