package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.Map;

public class ExReceiveShowPostFriend extends L2GameServerPacket {
    private final Map<Integer, String> _list;

    public ExReceiveShowPostFriend(final Player player) {
        _list = player.getPostFriends();
    }

    @Override
    public void writeImpl() {
        writeEx(0xd3);
        writeD(_list.size());
        _list.values().forEach(this::writeS);
    }
}
