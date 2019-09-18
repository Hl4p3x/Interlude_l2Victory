package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;

public class AdminMove implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanReload) {
            return false;
        }
        switch (command) {
            case admin_move_debug: {
                if (wordList.length > 1) {
                    final int dbgMode = Integer.parseInt(wordList[1]);
                    if (dbgMode > 0) {
                        activeChar.setVar("debugMove", Integer.parseInt(wordList[1]), -1L);
                        activeChar.sendMessage("Move debug mode " + dbgMode);
                    } else {
                        activeChar.unsetVar("debugMove");
                        activeChar.sendMessage("Move debug disabled");
                    }
                    break;
                }
                activeChar.setVar("debugMove", (activeChar.getVarInt("debugMove", 0) <= 0) ? 1 : 0, -1L);
                activeChar.sendMessage("Move debug mode " + activeChar.getVarInt("debugMove", 0));
                break;
            }
        }
        return false;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_move_debug
    }
}
