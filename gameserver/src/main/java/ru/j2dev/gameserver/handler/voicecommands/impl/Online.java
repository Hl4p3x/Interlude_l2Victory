package ru.j2dev.gameserver.handler.voicecommands.impl;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;

@HideAccess
@StringEncryption
public class Online extends Functions implements IVoicedCommandHandler {
    private final String[] _commandList = {"online"};

    public Online() {
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args) {
        if (Config.SERVICES_ONLINE_COMMAND_ENABLE || player.isGM()) {
            player.sendMessage(new CustomMessage("scripts.commands.user.online.service", player).addNumber(Math.round(GameObjectsStorage.getPlayers().size() * Config.SERVICE_COMMAND_MULTIPLIER)));
            return false;
        }
        return true;
    }
}
