package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectDamOverTime extends Effect {
    private static final int[] bleed = {12, 17, 25, 34, 44, 54, 62, 67, 72, 77, 82, 87};
    private static final int[] poison = {11, 16, 24, 32, 41, 50, 58, 63, 68, 72, 77, 82};

    private final boolean _percent;

    public EffectDamOverTime(final Env env, final EffectTemplate template) {
        super(env, template);
        _percent = getTemplate().getParam().getBool("percent", false);
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead()) {
            return false;
        }
        double damage = calc();
        if (_percent) {
            damage = _effected.getMaxHp() * _template._value * 0.01;
        }
        if (damage < 2.0 && getStackOrder() != -1) {
            switch (getEffectType()) {
                case Poison: {
                    damage = poison[getStackOrder() - 1] * getPeriod() / 1000L;
                    break;
                }
                case Bleed: {
                    damage = bleed[getStackOrder() - 1] * getPeriod() / 1000L;
                    break;
                }
            }
        }
        damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());
        if (damage > _effected.getCurrentHp() - 1.0 && !_effected.isNpc()) {
            if (!getSkill().isOffensive()) {
                _effected.sendPacket(Msg.NOT_ENOUGH_HP);
            }
            return false;
        }
        if (getSkill().getAbsorbPart() > 0.0) {
            _effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(_effected.getCurrentHp(), damage) + _effector.getCurrentHp(), false);
        }
        _effected.reduceCurrentHp(damage, _effector, getSkill(), !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);
        return true;
    }
}
