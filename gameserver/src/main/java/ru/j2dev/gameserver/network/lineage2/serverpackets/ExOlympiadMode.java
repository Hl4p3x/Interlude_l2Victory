package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket {
    private final int _mode;

    public ExOlympiadMode(final int mode) {
        _mode = mode;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x2b);
        writeC(_mode);
    }
}
