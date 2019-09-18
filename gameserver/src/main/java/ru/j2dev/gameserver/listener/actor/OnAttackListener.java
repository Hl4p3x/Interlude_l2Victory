package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnAttackListener extends CharListener {
    void onAttack(final Creature attacker, final Creature target);
}
