package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

public interface OnGainExpSpListener extends PlayerListener {
    void onGainExpSp(final Player p0, final long p1, final long p2);
}
