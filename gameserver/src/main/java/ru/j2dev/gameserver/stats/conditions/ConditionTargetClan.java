package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetClan extends Condition {
    private final boolean _test;

    public ConditionTargetClan(final String param) {
        _test = Boolean.valueOf(param);
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature Char = env.character;
        final Creature target = env.target;
        return Char.getPlayer() != null && target.getPlayer() != null && ((Char.getPlayer().getClanId() != 0 && Char.getPlayer().getClanId() == target.getPlayer().getClanId() == _test) || (Char.getPlayer().getParty() != null && Char.getPlayer().getParty() == target.getPlayer().getParty()));
    }
}
