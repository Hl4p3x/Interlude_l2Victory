package ru.j2dev.gameserver.model.chat.chatfilter;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public interface ChatFilterMatcher {
    boolean isMatch(final Player p0, final ChatType p1, final String p2, final Player p3);
}
