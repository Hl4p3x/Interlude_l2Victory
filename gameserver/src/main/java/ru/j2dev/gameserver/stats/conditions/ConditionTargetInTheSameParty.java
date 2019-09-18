package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetInTheSameParty extends Condition {
    private final boolean _val;

    public ConditionTargetInTheSameParty(final boolean val) {
        _val = val;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature creature = env.character;
        final Creature targetCreature = env.target;
        if (!creature.isPlayable() || targetCreature == null || !targetCreature.isPlayable()) {
            return !_val;
        }
        final Player player = creature.getPlayer();
        final Player target = targetCreature.getPlayer();
        if (player == target) {
            return _val;
        }
        if (player.isInParty() && player.getParty() == target.getParty()) {
            return _val;
        }
        return !_val;
    }
}
