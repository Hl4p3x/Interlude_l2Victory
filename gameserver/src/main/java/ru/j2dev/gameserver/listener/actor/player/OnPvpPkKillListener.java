package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

public interface OnPvpPkKillListener extends PlayerListener {
    void onPvpPkKill(final Player killer, final Player victim, final boolean isPk);
}
