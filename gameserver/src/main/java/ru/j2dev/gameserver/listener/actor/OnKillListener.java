package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnKillListener extends CharListener {
    void onKill(final Creature p0, final Creature p1);

    boolean ignorePetOrSummon();
}
