package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.stats.Env;

public class EffectEnervation extends Effect {
    public EffectEnervation(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isNpc()) {
            ((NpcInstance) _effected).setParameter("DebuffIntention", 0.5);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }

    @Override
    public void onExit() {
        super.onExit();
        if (_effected.isNpc()) {
            ((NpcInstance) _effected).setParameter("DebuffIntention", 1.0);
        }
    }
}
