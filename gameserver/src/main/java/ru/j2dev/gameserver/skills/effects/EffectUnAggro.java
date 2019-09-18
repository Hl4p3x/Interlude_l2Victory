package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.stats.Env;

public class EffectUnAggro extends Effect {
    public EffectUnAggro(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isNpc()) {
            ((NpcInstance) _effected).setUnAggred(true);
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_effected.isNpc()) {
            ((NpcInstance) _effected).setUnAggred(false);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
