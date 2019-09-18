package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class EventTrigger extends L2GameServerPacket {
    private final int _trapId;
    private final boolean _active;

    public EventTrigger(final int trapId, final boolean active) {
        _trapId = trapId;
        _active = active;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xcf);
        writeD(_trapId);
        writeC(_active ? 1 : 0);
    }
}
