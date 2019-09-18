package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class MDam extends Skill {
    public MDam(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (_targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE && (target == null || !target.isDead() || (!target.isNpc() && !target.isSummon()))) {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final int sps = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : (activeChar.getChargedSoulShot() ? 2 : 0)) : 0;
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isDead()) {
                    continue;
                }
                final boolean reflected = target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                final double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
                if (damage >= 1.0) {
                    realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
                }
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
        if (isSuicideAttack()) {
            activeChar.doDie(null);
        } else if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
        if (_targetType == SkillTargetType.TARGET_AREA_AIM_CORPSE && targets.size() > 0) {
            final Creature corpse = targets.get(0);
            if (corpse != null && corpse.isDead()) {
                if (corpse.isNpc()) {
                    ((NpcInstance) corpse).endDecayTask();
                } else if (corpse.isSummon()) {
                    ((SummonInstance) corpse).endDecayTask();
                }
                activeChar.getAI().setAttackTarget(null);
            }
        }
    }
}
