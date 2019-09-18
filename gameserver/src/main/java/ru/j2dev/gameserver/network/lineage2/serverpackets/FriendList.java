package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FriendList extends L2GameServerPacket {
    private List<FriendInfo> _friends;

    public FriendList(final Player player) {
        _friends = Collections.emptyList();
        final Map<Integer, Friend> friends = player.getFriendList().getList();
        _friends = new ArrayList<>(friends.size());
        friends.forEach((key, friend) -> {
            final FriendInfo f = new FriendInfo();
            f.name = friend.getName();
            f.classId = friend.getClassId();
            f.objectId = key;
            f.level = friend.getLevel();
            f.online = friend.isOnline();
            _friends.add(f);
        });
    }

    @Override
    protected void writeImpl() {
        writeC(0xfa);
        writeD(_friends.size());
        _friends.forEach(f -> {
            writeD(f.objectId);
            writeS(f.name);
            writeD(f.online);
            writeD(f.online ? f.objectId : 0);
        });
    }

    private class FriendInfo {
        private String name;
        private int objectId;
        private boolean online;
        private int level;
        private int classId;
    }
}
