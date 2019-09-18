package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.TrapInstance;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class DefuseTrap extends Skill {
    public DefuseTrap(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (target == null || !target.isTrap()) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null && target.isTrap()) {
                final TrapInstance trap = (TrapInstance) target;
                if (trap.getLevel() > getPower()) {
                    continue;
                }
                trap.deleteMe();
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
