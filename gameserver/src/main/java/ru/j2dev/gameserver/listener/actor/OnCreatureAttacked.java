package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;

/**
 * Created by JunkyFunky
 * on 11.07.2018 20:44
 * group j2dev
 */
public interface OnCreatureAttacked extends CharListener {

    void onCreatureAttacked(final Creature attacker, final Creature target);
}
