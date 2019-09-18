package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;

public interface OnTeleportListener extends PlayerListener {
    void onTeleport(final Player p0, final int p1, final int p2, final int p3, final Reflection p4);
}
