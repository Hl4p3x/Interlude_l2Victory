package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.network.lineage2.serverpackets.FinishRotating;
import ru.j2dev.gameserver.network.lineage2.serverpackets.StartRotating;
import ru.j2dev.gameserver.stats.Env;

public final class EffectBluff extends Effect {
    public EffectBluff(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return (!getEffected().isNpc() || getEffected().isMonster()) && super.checkCondition();
    }

    @Override
    public void onStart() {
        getEffected().broadcastPacket(new StartRotating(getEffected(), getEffected().getHeading(), 1, 65535));
        getEffected().broadcastPacket(new FinishRotating(getEffected(), getEffector().getHeading(), 65535));
        getEffected().setHeading(getEffector().getHeading());
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
