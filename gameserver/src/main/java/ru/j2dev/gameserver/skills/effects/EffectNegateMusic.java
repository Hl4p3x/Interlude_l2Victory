package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectNegateMusic extends Effect {
    public EffectNegateMusic(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    @Override
    public boolean onActionTime() {
        for (final Effect e : _effected.getEffectList().getAllEffects()) {
            if (e.getSkill().isMusic()) {
                e.exit();
            }
        }
        return false;
    }
}
