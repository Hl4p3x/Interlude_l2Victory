package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExRedSky extends L2GameServerPacket {
    private final int _duration;

    public ExRedSky(final int duration) {
        _duration = duration;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x40);
        writeD(_duration);
    }
}
