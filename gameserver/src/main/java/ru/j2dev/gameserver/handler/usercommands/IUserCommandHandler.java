package ru.j2dev.gameserver.handler.usercommands;

import ru.j2dev.gameserver.model.Player;

public interface IUserCommandHandler {
    boolean useUserCommand(final int p0, final Player p1);

    int[] getUserCommandList();
}
