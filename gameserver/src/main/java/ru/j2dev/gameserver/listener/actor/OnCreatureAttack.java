package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnCreatureAttack extends CharListener {
    void onCreatureAttack(final Creature target, final Creature attacker);
}
