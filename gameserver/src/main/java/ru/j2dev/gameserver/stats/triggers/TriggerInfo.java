package ru.j2dev.gameserver.stats.triggers;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.AddedSkill;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.conditions.Condition;

public class TriggerInfo extends AddedSkill {
    private final TriggerType _type;
    private final double _chance;
    private Condition[] _conditions;

    public TriggerInfo(final int id, final int level, final TriggerType type, final double chance) {
        super(id, level);
        _conditions = Condition.EMPTY_ARRAY;
        _type = type;
        _chance = chance;
    }

    public final void addCondition(final Condition c) {
        _conditions = ArrayUtils.add(_conditions, c);
    }

    public boolean checkCondition(final Creature actor, final Creature target, final Creature aimTarget, final Skill owner, final double damage) {
        if (getSkill().checkTarget(actor, aimTarget, aimTarget, false, false) != null) {
            return false;
        }
        final Env env = new Env();
        env.character = actor;
        env.skill = owner;
        env.target = target;
        env.value = damage;
        for (final Condition c : _conditions) {
            if (!c.test(env)) {
                return false;
            }
        }
        return true;
    }

    public TriggerType getType() {
        return _type;
    }

    public double getChance() {
        return _chance;
    }
}
