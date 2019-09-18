package ru.j2dev.gameserver.listener.game;

import ru.j2dev.gameserver.listener.GameListener;

public interface OnCharacterDeleteListener extends GameListener {
    void onCharacterDelate(final int p0);
}
