package ru.j2dev.gameserver.network.lineage2.cgm.lg;

import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

public class LGKeyPacket extends L2GameServerPacket {
    private final byte[] _key;

    public LGKeyPacket(final byte[] key) {
        _key = key;
    }

    @Override
    protected void writeImpl() {
        writeC(0);
        if (_key == null || _key.length == 0) {
            writeC(0);
            return;
        }
        writeC(1);
        writeB(_key);
        writeD(1);
        writeD(1);
    }
}