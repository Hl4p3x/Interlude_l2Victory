package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.Env;

public class EffectMutePhisycal extends Effect {
    public EffectMutePhisycal(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!_effected.startPMuted()) {
            final Skill castingSkill = _effected.getCastingSkill();
            if (castingSkill != null && !castingSkill.isMagic()) {
                _effected.abortCast(true, true);
            }
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopPMuted();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
