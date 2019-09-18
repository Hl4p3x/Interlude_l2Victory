package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Formulas.AttackInfo;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Drain extends Skill {
    private final double _absorbAbs;

    public Drain(final StatsSet set) {
        super(set);
        _absorbAbs = set.getDouble("absorbAbs", 0.0);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
        final boolean ss = isSSPossible() && activeChar.getChargedSoulShot();
        final boolean corpseSkill = _targetType == SkillTargetType.TARGET_CORPSE;
        for (final Creature target : targets) {
            if (target != null) {
                final boolean reflected = !corpseSkill && target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                if (getPower() > 0.0 || _absorbAbs > 0.0) {
                    if (realTarget.isDead() && !corpseSkill) {
                        continue;
                    }
                    double hp = 0.0;
                    final double targetHp = realTarget.getCurrentHp();
                    if (!corpseSkill) {
                        double damage;
                        if (isMagic()) {
                            damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
                        } else {
                            final AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);
                            damage = info.damage;
                            if (info.lethal_dmg > 0.0) {
                                realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
                            }
                        }
                        final double targetCP = realTarget.getCurrentCp();
                        if (damage > targetCP || !realTarget.isPlayer()) {
                            hp = (damage - targetCP) * _absorbPart;
                        }
                        realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
                        if (!reflected) {
                            realTarget.doCounterAttack(this, activeChar, false);
                        }
                    }
                    if (_absorbAbs == 0.0 && _absorbPart == 0.0) {
                        continue;
                    }
                    hp += _absorbAbs;
                    if (hp > targetHp && !corpseSkill) {
                        hp = targetHp;
                    }
                    final double addToHp = Math.max(0.0, Math.min(hp, activeChar.calcStat(Stats.HP_LIMIT, null, null) * activeChar.getMaxHp() / 100.0 - activeChar.getCurrentHp()));
                    if (addToHp > 0.0 && !activeChar.isHealBlocked()) {
                        activeChar.setCurrentHp(activeChar.getCurrentHp() + addToHp, false);
                    }
                    if (realTarget.isDead() && corpseSkill && realTarget.isNpc()) {
                        activeChar.getAI().setAttackTarget(null);
                        ((NpcInstance) realTarget).endDecayTask();
                    }
                }

                if (corpseSkill) {
                    target.deleteMe();
                }
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
        if (isMagic()) {
            if (sps == 0) {
                return;
            }
        } else if (!ss) {
            return;
        }
        activeChar.unChargeShots(isMagic());
    }
}
