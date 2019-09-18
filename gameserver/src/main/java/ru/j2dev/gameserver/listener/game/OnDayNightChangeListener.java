package ru.j2dev.gameserver.listener.game;

import ru.j2dev.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener {
    void onDay();

    void onNight();
}
