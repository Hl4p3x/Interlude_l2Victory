package ru.j2dev.gameserver.listener.actor;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;

public interface OnMagicHitListener extends CharListener {
    void onMagicHit(final Creature p0, final Skill p1, final Creature p2);
}
