package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class CameraMode extends L2GameServerPacket {
    final int _mode;

    public CameraMode(final int mode) {
        _mode = mode;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf1);
        writeD(_mode);
    }
}
