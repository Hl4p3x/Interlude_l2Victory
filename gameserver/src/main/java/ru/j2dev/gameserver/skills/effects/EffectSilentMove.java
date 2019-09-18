package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;

public final class EffectSilentMove extends Effect {
    public EffectSilentMove(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isPlayable()) {
            ((Playable) _effected).startSilentMoving();
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_effected.isPlayable()) {
            ((Playable) _effected).stopSilentMoving();
        }
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead()) {
            return false;
        }
        if (!getSkill().isToggle()) {
            return false;
        }
        final double manaDam = calc();
        if (manaDam > _effected.getCurrentMp()) {
            _effected.sendPacket(Msg.NOT_ENOUGH_MP);
            _effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
            return false;
        }
        _effected.reduceCurrentMp(manaDam, null);
        return true;
    }
}
