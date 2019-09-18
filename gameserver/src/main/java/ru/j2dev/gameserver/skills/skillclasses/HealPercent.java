package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class HealPercent extends Skill {
    public HealPercent(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isHealBlocked()) {
                    continue;
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
                final double hp = _power * target.getMaxHp() / 100.0;
                final double addToHp = Math.max(0.0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0 - target.getCurrentHp()));
                if (addToHp > 0.0) {
                    target.setCurrentHp(addToHp + target.getCurrentHp(), false);
                }
                if (!target.isPlayer()) {
                    continue;
                }
                if (activeChar != target) {
                    target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
                } else {
                    activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
