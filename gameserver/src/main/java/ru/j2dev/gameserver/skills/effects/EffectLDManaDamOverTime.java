package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;

public class EffectLDManaDamOverTime extends Effect {
    public EffectLDManaDamOverTime(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead()) {
            return false;
        }
        double manaDam = calc();
        manaDam *= _effected.getLevel() / 2.4;
        if (manaDam > _effected.getCurrentMp() && getSkill().isToggle()) {
            _effected.sendPacket(Msg.NOT_ENOUGH_MP);
            _effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
            return false;
        }
        _effected.reduceCurrentMp(manaDam, null);
        return true;
    }
}
