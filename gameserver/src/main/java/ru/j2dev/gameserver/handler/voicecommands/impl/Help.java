package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.scripts.Functions;

public class Help extends Functions implements IVoicedCommandHandler {
    private final String[] _commandList;

    public Help() {
        _commandList = new String[]{"exp", "whereis"};
    }

    @Override
    public boolean useVoicedCommand(String command, final Player activeChar, final String args) {
        command = command.intern();
        if ("whereis".equalsIgnoreCase(command)) {
            return whereis(activeChar, args);
        }
        return "exp".equalsIgnoreCase(command) && exp(activeChar, args);
    }

    private boolean exp(final Player activeChar, final String args) {
        if (activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel())) {
            activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel", activeChar));
        } else {
            final long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
            activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft", activeChar).addNumber(exp));
        }
        return true;
    }

    private boolean whereis(final Player activeChar, final String args) {
        final Player friend = World.getPlayer(args);
        if (friend == null) {
            return false;
        }
        if (friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan()) {
            final RadarControl rc = new RadarControl(0, 1, friend.getLoc());
            activeChar.sendPacket(rc);
            return true;
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }
}
