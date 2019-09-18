package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectHealCPPercent extends Effect {
    private final boolean _ignoreCpEff;

    public EffectHealCPPercent(final Env env, final EffectTemplate template) {
        super(env, template);
        _ignoreCpEff = template.getParam().getBool("ignoreCpEff", true);
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
        final double cp = calc() * _effected.getMaxCp() / 100.0;
        final double newCp = cp * (_ignoreCpEff ? 100.0 : _effected.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0, _effector, getSkill())) / 100.0;
        final double addToCp = Math.max(0.0, Math.min(newCp, _effected.calcStat(Stats.CP_LIMIT, null, null) * _effected.getMaxCp() / 100.0 - _effected.getCurrentCp()));
        if (_effected == _effector) {
            _effected.sendPacket(new SystemMessage(1405).addNumber((long) addToCp));
        } else {
            _effected.sendPacket(new SystemMessage(1406).addName(_effector).addNumber((long) addToCp));
        }
        if (addToCp > 0.0) {
            _effected.setCurrentCp(addToCp + _effected.getCurrentCp());
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
