package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.tables.GmListTable;

public class AdminGmChat implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanAnnounce) {
            return false;
        }
        switch (command) {
            case admin_gmchat: {
                try {
                    final String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
                    final Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
                    GmListTable.broadcastToGMs(cs);
                } catch (StringIndexOutOfBoundsException ignored) {
                }
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
        admin_gmchat,
        admin_snoop
    }
}
