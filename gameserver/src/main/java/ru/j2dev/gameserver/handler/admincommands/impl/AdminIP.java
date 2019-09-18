package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;

public class AdminIP implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanBan) {
            return false;
        }
        switch (command) {
            case admin_charip: {
                if (wordList.length != 2) {
                    activeChar.sendMessage("Command syntax: //charip <char_name>");
                    activeChar.sendMessage(" Gets character's IP.");
                    break;
                }
                final Player pl = World.getPlayer(wordList[1]);
                if (pl == null) {
                    activeChar.sendMessage("Character " + wordList[1] + " not found.");
                    break;
                }
                final String ip_adr = pl.getIP();
                if ("<not connected>".equalsIgnoreCase(ip_adr)) {
                    activeChar.sendMessage("Character " + wordList[1] + " not found.");
                    break;
                }
                activeChar.sendMessage("Character's IP: " + ip_adr);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_charip
    }
}
