package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

public interface OnPlayerEnterListener extends PlayerListener {
    void onPlayerEnter(final Player p0);
}
