package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;

public class AdminGm implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        switch (command) {
            case admin_gm: {
                handleGm(activeChar);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleGm(final Player activeChar) {
        if (activeChar.isGM()) {
            activeChar.getPlayerAccess().IsGM = false;
            activeChar.sendMessage("You no longer have GM status.");
        } else {
            activeChar.getPlayerAccess().IsGM = true;
            activeChar.sendMessage("You have GM status now.");
        }
    }

    private enum Commands {
        admin_gm
    }
}
