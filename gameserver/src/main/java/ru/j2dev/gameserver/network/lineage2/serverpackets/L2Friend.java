package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class L2Friend extends L2GameServerPacket {
    private final boolean _add;
    private final boolean _online;
    private final String _name;
    private final int _object_id;

    public L2Friend(final Player player, final boolean add) {
        _add = add;
        _name = player.getName();
        _object_id = player.getObjectId();
        _online = true;
    }

    public L2Friend(final String name, final boolean add, final boolean online, final int object_id) {
        _name = name;
        _add = add;
        _object_id = object_id;
        _online = online;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xfb);
        writeD(_add ? 1 : 3);
        writeD(0);
        writeS(_name);
        writeD(_online ? 1 : 0);
        writeD(_object_id);
    }
}
