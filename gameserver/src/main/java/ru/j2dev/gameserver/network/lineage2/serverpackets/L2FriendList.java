package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class L2FriendList extends L2GameServerPacket {
    private List<FriendInfo> _list;

    public L2FriendList(final Player player) {
        _list = Collections.emptyList();
        final Map<Integer, Friend> list = player.getFriendList().getList();
        _list = new ArrayList<>(list.size());
        list.forEach((key, value) -> {
            final FriendInfo f = new FriendInfo();
            f._objectId = key;
            f._name = value.getName();
            f._online = value.isOnline();
            _list.add(f);
        });
    }

    @Override
    protected final void writeImpl() {
        writeC(0xfa);
        writeD(_list.size());
        _list.forEach(friendInfo -> {
            writeD(0);
            writeS(friendInfo._name);
            writeD(friendInfo._online ? 1 : 0);
            writeD(friendInfo._objectId);
        });
    }

    private static class FriendInfo {
        private int _objectId;
        private String _name;
        private boolean _online;
    }
}
