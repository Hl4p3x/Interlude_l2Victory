package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class KeyPacket extends L2GameServerPacket {
    private final byte[] _key;

    public KeyPacket(final byte[] key) {
        _key = key;
    }

    @Override
    public void writeImpl() {
        writeC(0x0);
        if (_key == null || _key.length == 0) {
            writeC(0);
            return;
        }
        writeC(1);
        writeB(_key);
        writeD(1);
        writeD(0);
        writeC(0);
        writeD(0);
    }
}
