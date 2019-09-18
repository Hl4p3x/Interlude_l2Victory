package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.utils.MapUtils;

public class MatchMaps implements ChatFilterMatcher {
    private final int[] _maps;

    public MatchMaps(final int[] maps) {
        _maps = maps;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        final int rx = MapUtils.regionX(player);
        final int ry = MapUtils.regionY(player);
        for (int i = 0; i < _maps.length; i += 2) {
            final int mx = _maps[i];
            final int my = _maps[i + 1];
            if (mx == rx && my == ry) {
                return true;
            }
        }
        return false;
    }
}
