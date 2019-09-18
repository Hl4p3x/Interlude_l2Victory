package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

public class AdminEvents implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().IsEventGm) {
            return false;
        }
        switch (command) {
            case admin_events: {
                if (wordList.length == 1) {
                    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/events.htm"));
                    break;
                }
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/" + wordList[1].trim()));
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
        admin_events
    }
}
