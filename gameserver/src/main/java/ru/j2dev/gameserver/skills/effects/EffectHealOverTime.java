package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExRegenMax;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectHealOverTime extends Effect {
    private final boolean _ignoreHpEff;

    public EffectHealOverTime(final Env env, final EffectTemplate template) {
        super(env, template);
        _ignoreHpEff = template.getParam().getBool("ignoreHpEff", false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getEffected().isPlayer() && getCount() > 0 && getPeriod() > 0L) {
            getEffected().sendPacket(new ExRegenMax(calc(), (int) (getCount() * getPeriod() / 1000L), Math.round(getPeriod() / 1000L)));
        }
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isHealBlocked()) {
            return true;
        }
        final double hp = calc();
        final double newHp = hp * (_ignoreHpEff ? 100.0 : _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, _effector, getSkill())) / 100.0;
        final double addToHp = Math.max(0.0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0 - _effected.getCurrentHp()));
        if (addToHp > 0.0) {
            getEffected().setCurrentHp(_effected.getCurrentHp() + addToHp, false);
        }
        return true;
    }
}
