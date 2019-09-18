package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.stats.Env;

public final class EffectInvulnerable extends Effect {
    public EffectInvulnerable(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        if (_effected.isInvul()) {
            return false;
        }
        final Skill skill = _effected.getCastingSkill();
        return (skill == null || skill.getSkillType() != SkillType.TAKECASTLE) && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startHealBlocked();
        _effected.setIsInvul(true);
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopHealBlocked();
        _effected.setIsInvul(false);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
