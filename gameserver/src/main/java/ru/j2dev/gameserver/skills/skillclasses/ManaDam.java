package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class ManaDam extends Skill {
    public ManaDam(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        int sps = 0;
        if (isSSPossible()) {
            sps = activeChar.getChargedSpiritShot();
        }
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isDead()) {
                    continue;
                }
                final int magicLevel = (getMagicLevel() == 0) ? activeChar.getLevel() : getMagicLevel();
                int landRate = Rnd.get(30, 100);
                landRate *= target.getLevel();
                landRate /= magicLevel;
                if (Rnd.chance(landRate)) {
                    double mAtk = activeChar.getMAtk(target, this);
                    if (sps == 2) {
                        mAtk *= 4.0;
                    } else if (sps == 1) {
                        mAtk *= 2.0;
                    }
                    double mDef = target.getMDef(activeChar, this);
                    if (mDef < 1.0) {
                        mDef = 1.0;
                    }
                    double damage = Math.sqrt(mAtk) * getPower() * (target.getMaxMp() / 97) / mDef;
                    if (Config.MDAM_CRIT_POSSIBLE) {
                        final boolean crit = Formulas.calcMCrit(activeChar, target, activeChar.getMagicCriticalRate(target, this));
                        if (crit) {
                            activeChar.sendPacket(Msg.MAGIC_CRITICAL_HIT);
                            damage *= activeChar.calcStat(Stats.MCRITICAL_DAMAGE, 4.0, target, this);
                        }
                    }
                    target.reduceCurrentMp(damage, activeChar, true);
                } else {
                    final SystemMessage msg = new SystemMessage(159).addName(target);
                    activeChar.sendPacket(msg);
                    target.sendPacket(msg);
                    target.reduceCurrentHp(1.0, activeChar, this, true, true, false, true, false, false, true);
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
