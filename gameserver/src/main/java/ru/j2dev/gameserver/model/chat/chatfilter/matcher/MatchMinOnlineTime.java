package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public class MatchMinOnlineTime implements ChatFilterMatcher {
    private final long _onlineTime;

    public MatchMinOnlineTime(final int onlineTime) {
        _onlineTime = onlineTime * 1000L;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        return player.getOnlineTime() < _onlineTime;
    }
}
