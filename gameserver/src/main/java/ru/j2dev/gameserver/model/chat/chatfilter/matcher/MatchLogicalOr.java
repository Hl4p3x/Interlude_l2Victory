package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

import java.util.Arrays;

public class MatchLogicalOr implements ChatFilterMatcher {
    private final ChatFilterMatcher[] _matches;

    public MatchLogicalOr(final ChatFilterMatcher[] matches) {
        _matches = matches;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        return Arrays.stream(_matches).anyMatch(m -> m.isMatch(player, type, msg, recipient));
    }
}
