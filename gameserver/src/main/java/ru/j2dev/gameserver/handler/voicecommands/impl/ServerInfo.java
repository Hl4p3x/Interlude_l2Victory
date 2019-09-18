package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerInfo extends Functions implements IVoicedCommandHandler {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private final String[] _commandList;

    public ServerInfo() {
        _commandList = new String[]{"rev", "ver", "date", "time"};
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        if (command.equals("rev") || command.equals("ver")) {
            activeChar.sendMessage("Developer page: " + GameServer.getInstance().getVersion().getTeamSite());
            activeChar.sendMessage("Builder: " + GameServer.getInstance().getVersion().getCoreDev());
            activeChar.sendMessage("Revision: " + GameServer.getInstance().getVersion().getRevisionNumber());
            activeChar.sendMessage("Build date: " + GameServer.getInstance().getVersion().getBuildDate());
        } else if (command.equals("date") || command.equals("time")) {
            activeChar.sendMessage(ServerInfo.DATE_FORMAT.format(new Date(System.currentTimeMillis())));
            return true;
        }
        return false;
    }
}