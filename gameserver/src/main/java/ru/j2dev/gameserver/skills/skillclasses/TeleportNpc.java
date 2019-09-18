package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class TeleportNpc extends Skill {
    public TeleportNpc(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null && !target.isDead()) {
                getEffects(activeChar, target, getActivateRate() > 0, false);
                target.abortAttack(true, true);
                target.abortCast(true, true);
                target.stopMove();
                int x = activeChar.getX();
                int y = activeChar.getY();
                final int z = activeChar.getZ();
                final int h = activeChar.getHeading();
                final int range = (int) (activeChar.getColRadius() + target.getColRadius());
                final int hyp = (int) Math.sqrt(range * range / 2);
                if (h < 16384) {
                    x += hyp;
                    y += hyp;
                } else if (h > 16384 && h <= 32768) {
                    x -= hyp;
                    y += hyp;
                } else if (h < 32768) {
                    x -= hyp;
                    y -= hyp;
                } else if (h > 49152) {
                    x += hyp;
                    y -= hyp;
                }
                target.setXYZ(x, y, z);
                target.validateLocation(1);
            }
        }
    }
}
