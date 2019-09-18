package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnCurrentMpReduceListener extends CharListener {
    void onCurrentMpReduce(final Creature p0, final double p1, final Creature p2);
}
