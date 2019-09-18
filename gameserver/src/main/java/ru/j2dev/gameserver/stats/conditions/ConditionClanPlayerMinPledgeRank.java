package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Player.EPledgeRank;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.stats.Env;

public class ConditionClanPlayerMinPledgeRank extends Condition {
    private final EPledgeRank _minPledgeRank;

    public ConditionClanPlayerMinPledgeRank(final String minPledgeRankName) {
        this(parsePledgeRank(minPledgeRankName));
    }

    public ConditionClanPlayerMinPledgeRank(final EPledgeRank minPledgeRank) {
        _minPledgeRank = minPledgeRank;
    }

    private static EPledgeRank parsePledgeRank(final String pledgeRankText) {
        final EPledgeRank pledgeRank = EPledgeRank.valueOf(pledgeRankText.toUpperCase());
        if (pledgeRank == null) {
            throw new IllegalArgumentException("Unknown pledge rank \"" + pledgeRankText + "\"");
        }
        return pledgeRank;
    }

    @Override
    protected boolean testImpl(final Env env) {
        if (env.character == null) {
            return false;
        }
        final Player player = env.character.getPlayer();
        if (player == null) {
            return false;
        }
        final Clan clan = player.getClan();
        return clan != null && player.getPledgeRank().getRankId() >= _minPledgeRank.getRankId();
    }
}
