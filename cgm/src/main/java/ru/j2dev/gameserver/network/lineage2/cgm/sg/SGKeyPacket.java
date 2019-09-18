package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.SmartCore;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

public class SGKeyPacket extends L2GameServerPacket {
    private final byte[] _key;

    public SGKeyPacket(final byte[] key) {
        SmartCore.cryptInternalKey(_key = key);
    }

    @Override
    public void writeImpl() {
        writeC(0);
        if (_key == null || _key.length == 0) {
            writeC(0);
            return;
        }
        writeC(1);
        for (int idx = 0; idx < 8; ++idx) {
            writeC((int) _key[idx]);
        }
        writeD(1);
        writeC(0);
    }
}