package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetPlayerRace extends Condition {
    private final Race _race;

    public ConditionTargetPlayerRace(final String race) {
        _race = Race.valueOf(race.toLowerCase());
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature target = env.target;
        return target != null && target.isPlayer() && _race == ((Player) target).getRace();
    }
}
