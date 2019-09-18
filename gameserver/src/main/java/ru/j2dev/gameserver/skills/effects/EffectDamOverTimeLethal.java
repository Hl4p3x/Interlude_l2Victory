package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class EffectDamOverTimeLethal extends Effect {
    public EffectDamOverTimeLethal(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead()) {
            return false;
        }
        double damage = calc();
        if (getSkill().isOffensive()) {
            damage *= 2.0;
        }
        damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());
        _effected.reduceCurrentHp(damage, _effector, getSkill(), !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);
        return true;
    }
}
