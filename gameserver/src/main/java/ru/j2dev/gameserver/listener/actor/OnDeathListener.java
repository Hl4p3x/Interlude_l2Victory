package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnDeathListener extends CharListener {
    void onDeath(Creature actor, Creature killer);
}
