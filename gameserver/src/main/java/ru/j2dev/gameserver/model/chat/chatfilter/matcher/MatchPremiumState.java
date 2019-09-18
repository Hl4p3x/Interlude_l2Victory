package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public class MatchPremiumState implements ChatFilterMatcher {
    private final boolean _excludePremium;

    public MatchPremiumState(final boolean premiumState) {
        _excludePremium = premiumState;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        return _excludePremium || !player.hasBonus();
    }
}
