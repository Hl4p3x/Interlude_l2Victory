package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGame;

public interface OnOlyCompetitionListener extends PlayerListener {
    void onOlyCompetitionCompleted(final Player p0, final OlympiadGame p1, final boolean p2);
}
