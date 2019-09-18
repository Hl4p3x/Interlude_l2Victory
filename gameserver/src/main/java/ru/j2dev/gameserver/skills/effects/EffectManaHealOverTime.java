package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectManaHealOverTime extends Effect {
    private final boolean _ignoreMpEff;

    public EffectManaHealOverTime(final Env env, final EffectTemplate template) {
        super(env, template);
        _ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isHealBlocked()) {
            return true;
        }
        final double mp = calc();
        final double newMp = mp * (_ignoreMpEff ? 100.0 : _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, _effector, getSkill())) / 100.0;
        final double addToMp = Math.max(0.0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100.0 - _effected.getCurrentMp()));
        if (addToMp > 0.0) {
            _effected.setCurrentMp(_effected.getCurrentMp() + addToMp);
        }
        return true;
    }
}
