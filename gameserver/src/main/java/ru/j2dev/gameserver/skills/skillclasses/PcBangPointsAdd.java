package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class PcBangPointsAdd extends Skill {
    public PcBangPointsAdd(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final int points = (int) _power;
        for (final Creature target : targets) {
            if (target.isPlayer()) {
                final Player player = target.getPlayer();
                player.addPcBangPoints(points, false);
            }
            getEffects(activeChar, target, getActivateRate() > 0, false);
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
