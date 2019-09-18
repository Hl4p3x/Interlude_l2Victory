package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectHealPercent extends Effect {
    public EffectHealPercent(final Env env, final EffectTemplate template) {
        super(env, template);
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
        final double hp = calc() * _effected.getMaxHp() / 100.0;
        final double addToHp = Math.max(0.0, Math.min(hp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0 - _effected.getCurrentHp()));
        _effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
        if (addToHp > 0.0) {
            _effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
