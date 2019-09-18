package ru.j2dev.gameserver.listener.game;

import ru.j2dev.gameserver.listener.GameListener;

public interface OnSSPeriodListener extends GameListener {
    void onPeriodChange(final int p0);
}
