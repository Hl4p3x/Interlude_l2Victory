package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class ConditionPlayerCubic extends Condition {
    private final int _id;

    public ConditionPlayerCubic(final int id) {
        _id = id;
    }

    @Override
    protected boolean testImpl(final Env env) {
        if (env.target == null || !env.target.isPlayer()) {
            return false;
        }
        final Player targetPlayer = (Player) env.target;
        if (targetPlayer.getCubic(_id) != null) {
            return true;
        }
        final int size = (int) targetPlayer.calcStat(Stats.CUBICS_LIMIT, 1.0);
        if (targetPlayer.getCubics().size() >= size) {
            if (env.character == targetPlayer) {
                targetPlayer.sendPacket(Msg.CUBIC_SUMMONING_FAILED);
            }
            return false;
        }
        return true;
    }
}
