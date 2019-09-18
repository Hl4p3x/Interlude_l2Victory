package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;

public interface OnCurrentHpDamageListener extends CharListener {
    void onCurrentHpDamage(final Creature p0, final double p1, final Creature p2, final Skill p3);
}
