package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Effect.EEffectSlot;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StealBuff extends Skill {
    private final int _stealCount;
    private final int _chanceMod;

    public StealBuff(final StatsSet set) {
        super(set);
        _stealCount = set.getInteger("StealCount", 1);
        _chanceMod = set.getInteger("ChanceMod", 0);
    }

    public static boolean calcSkillCancel(final Skill cancel, final Effect effect, final int chance_mod, final double res_mul, final boolean chance_restrict) {
        final int dml = Math.max(0, cancel.getMagicLevel() - effect.getSkill().getMagicLevel());
        final int chance = (int) ((2 * dml + chance_mod + effect.getPeriod() * effect.getCount() / 120000L) * res_mul);
        return Rnd.chance(Math.max(Config.SKILLS_DISPEL_MOD_MIN, Math.min(Config.SKILLS_DISPEL_MOD_MAX, chance)));
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                final double res_mul = 1.0 - target.calcStat(Stats.CANCEL_RESIST, 0.0, null, null) * 0.01;
                final Effect[] effects = target.getEffectList().getAllFirstEffects();
                final LinkedList<Effect> eset = new LinkedList<>();
                for (final EEffectSlot ees : EEffectSlot.VALUES) {
                    for (final Effect eff : effects) {
                        if (eff != null && !eff.getTemplate()._applyOnCaster) {
                            if (eff.getEffectSlot() == ees) {
                                final Skill skill = eff.getSkill();
                                if (skill.isCancelable() && skill.isActive() && !skill.isOffensive() && !skill.isToggle()) {
                                    if (!skill.isTrigger()) {
                                        eset.add(eff);
                                    }
                                }
                            }
                        }
                    }
                }
                boolean update = false;
                final Iterator<Effect> it = eset.descendingIterator();
                int cnt = 0;
                while (it.hasNext() && cnt++ < _stealCount) {
                    final Effect effect = it.next();
                    if (!calcSkillCancel(this, effect, _chanceMod, res_mul, true)) {
                        continue;
                    }
                    final Skill skill2 = effect.getSkill();
                    for (final Effect ceff : target.getEffectList().getEffectsBySkill(skill2)) {
                        if (ceff != null) {
                            final Effect leff = ceff.getTemplate().getEffect(new Env(activeChar, activeChar, skill2));
                            leff.setCount(ceff.getCount());
                            if (ceff.getCount() == 1) {
                                leff.setPeriod(ceff.getPeriod() - ceff.getTime());
                            } else {
                                leff.setPeriod(ceff.getPeriod());
                            }
                            update = true;
                            ceff.exit();
                            activeChar.getEffectList().addEffect(leff);
                        }
                    }
                    target.sendPacket(new SystemMessage(92).addSkillName(skill2.getId(), skill2.getLevel()));
                }
                if (update) {
                    target.sendChanges();
                    target.updateEffectIcons();
                    activeChar.sendChanges();
                    activeChar.updateEffectIcons();
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
