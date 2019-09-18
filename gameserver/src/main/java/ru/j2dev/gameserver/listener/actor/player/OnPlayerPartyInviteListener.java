package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener {
    void onPartyInvite(final Player p0);
}
