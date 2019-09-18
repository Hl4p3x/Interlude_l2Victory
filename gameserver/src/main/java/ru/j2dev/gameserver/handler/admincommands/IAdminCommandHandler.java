package ru.j2dev.gameserver.handler.admincommands;

import ru.j2dev.gameserver.model.Player;

public interface IAdminCommandHandler {

    boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar);

    Enum[] getAdminCommandEnum();
}
