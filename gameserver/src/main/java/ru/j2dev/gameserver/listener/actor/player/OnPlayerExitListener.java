package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

public interface OnPlayerExitListener extends PlayerListener {
    void onPlayerExit(final Player p0);
}
