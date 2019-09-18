package ru.j2dev.gameserver.listener.game;

import ru.j2dev.gameserver.listener.GameListener;

public interface OnShutdownListener extends GameListener {
    void onShutdown();
}
