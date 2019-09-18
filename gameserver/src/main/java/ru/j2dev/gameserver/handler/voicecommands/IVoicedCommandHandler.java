package ru.j2dev.gameserver.handler.voicecommands;

import ru.j2dev.gameserver.model.Player;

public interface IVoicedCommandHandler {
    boolean useVoicedCommand(final String p0, final Player p1, final String p2);

    String[] getVoicedCommandList();
}
