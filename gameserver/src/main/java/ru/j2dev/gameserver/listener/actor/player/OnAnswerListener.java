package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;

public interface OnAnswerListener extends PlayerListener {
    void sayYes();

    default void sayNo() {
    }

    /**
     * От тупого хака
     */
    default long expireTime() {
        return 0;
    }
}
