package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.FinishRotating;
import ru.j2dev.gameserver.network.lineage2.serverpackets.StartRotating;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Formulas.AttackInfo;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class PDam extends Skill {
    private final boolean _onCrit;
    private final boolean _directHp;
    private final boolean _turner;
    private final boolean _blow;

    public PDam(final StatsSet set) {
        super(set);
        _onCrit = set.getBool("onCrit", false);
        _directHp = set.getBool("directHp", false);
        _turner = set.getBool("turner", false);
        _blow = set.getBool("blow", false);
    }

    @Override
    public boolean isBlowSkill() {
        return _blow;
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
        for (final Creature target : targets) {
            if (target != null && !target.isDead()) {
                if (_turner && !target.isInvul()) {
                    target.broadcastPacket(new StartRotating(target, target.getHeading(), 1, 65535));
                    target.broadcastPacket(new FinishRotating(target, activeChar.getHeading(), 65535));
                    target.setHeading(activeChar.getHeading());
                    target.sendPacket(new SystemMessage(110).addSkillName(_displayId, _displayLevel));
                }
                final boolean reflected = target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                final AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, _blow, ss, _onCrit);
                if (info.lethal_dmg > 0.0) {
                    realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
                }
                if (!info.miss || info.damage >= 1.0) {
                    realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, !info.lethal && _directHp, true, false, false, getPower() != 0.0);
                }
                if (!reflected) {
                    realTarget.doCounterAttack(this, activeChar, _blow);
                }
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
        if (isSuicideAttack()) {
            activeChar.doDie(null);
        } else if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
