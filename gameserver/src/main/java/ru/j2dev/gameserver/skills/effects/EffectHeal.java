package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectHeal extends Effect {
    private final boolean _ignoreHpEff;

    public EffectHeal(final Env env, final EffectTemplate template) {
        super(env, template);
        _ignoreHpEff = template.getParam().getBool("ignoreHpEff", false);
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
        final double hp = calc();
        final double newHp = hp * (_ignoreHpEff ? 100.0 : _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, _effector, getSkill())) / 100.0;
        final double addToHp = Math.max(0.0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0 - _effected.getCurrentHp()));
        if (addToHp > 0.0) {
            _effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
            _effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
