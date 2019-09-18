package ru.j2dev.gameserver.model.items.attachment;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment {
    void onLogout(final Player p0);

    void onDeath(final Player p0, final Creature p1);

    void onEnterPeace(final Player p0);

    boolean canAttack(final Player p0);

    boolean canCast(final Player p0, final Skill p1);
}
