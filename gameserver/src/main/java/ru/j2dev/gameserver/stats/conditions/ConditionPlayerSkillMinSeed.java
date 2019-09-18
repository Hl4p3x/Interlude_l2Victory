package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.skills.effects.EffectSkillSeed;
import ru.j2dev.gameserver.stats.Env;

import java.util.List;

public class ConditionPlayerSkillMinSeed extends Condition {
    private final int _skillId;
    private final int _skillMinSeed;

    public ConditionPlayerSkillMinSeed(final int skillId, final int skillMinSeed) {
        _skillId = skillId;
        _skillMinSeed = skillMinSeed;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature activeChar = env.character;
        if (activeChar == null) {
            return false;
        }
        final List<Effect> effects = activeChar.getEffectList().getEffectsBySkillId(_skillId);
        if (effects == null || effects.isEmpty()) {
            return false;
        }
        for (final Effect effect : effects) {
            if (effect instanceof EffectSkillSeed) {
                final EffectSkillSeed effectSeed = (EffectSkillSeed) effect;
                if (effectSeed.getSeeds() >= _skillMinSeed) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
