package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerRiding extends Condition {
    private final CheckPlayerRiding _riding;

    public ConditionPlayerRiding(final CheckPlayerRiding riding) {
        _riding = riding;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && ((_riding == CheckPlayerRiding.STRIDER && ((Player) env.character).isRiding()) || (_riding == CheckPlayerRiding.WYVERN && env.character.isFlying()) || (_riding == CheckPlayerRiding.NONE && !((Player) env.character).isRiding() && !env.character.isFlying()));
    }

    public enum CheckPlayerRiding {
        NONE,
        STRIDER,
        WYVERN
    }
}
