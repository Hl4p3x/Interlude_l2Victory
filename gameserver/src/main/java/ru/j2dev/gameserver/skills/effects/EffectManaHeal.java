package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectManaHeal extends Effect {
    private final boolean _ignoreMpEff;

    public EffectManaHeal(final Env env, final EffectTemplate template) {
        super(env, template);
        _ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
    }

    @Override
    public boolean checkCondition() {
        return !_effected.isHealBlocked() && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isHealBlocked()) {
            return;
        }
        final double mp = calc();
        final double newMp = Math.min(mp * 1.7, mp * (_ignoreMpEff ? 100.0 : _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, _effector, getSkill())) / 100.0);
        final double addToMp = Math.max(0.0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100.0 - _effected.getCurrentMp()));
        _effected.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
        if (addToMp > 0.0) {
            _effected.setCurrentMp(addToMp + _effected.getCurrentMp());
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
