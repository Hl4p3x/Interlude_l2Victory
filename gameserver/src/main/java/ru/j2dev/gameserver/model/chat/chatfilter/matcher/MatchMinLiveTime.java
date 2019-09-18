package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public class MatchMinLiveTime implements ChatFilterMatcher {
    private final long _createTime;

    public MatchMinLiveTime(final int createTime) {
        _createTime = createTime * 1000L;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        return System.currentTimeMillis() - player.getCreateTime() < _createTime;
    }
}
