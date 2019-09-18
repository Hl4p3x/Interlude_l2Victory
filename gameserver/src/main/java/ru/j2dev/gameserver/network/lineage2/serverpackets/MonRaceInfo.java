package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.instances.NpcInstance;

public class MonRaceInfo extends L2GameServerPacket {
    private final int _unknown1;
    private final int _unknown2;
    private final NpcInstance[] _monsters;
    private final int[][] _speeds;

    public MonRaceInfo(final int unknown1, final int unknown2, final NpcInstance[] monsters, final int[][] speeds) {
        _unknown1 = unknown1;
        _unknown2 = unknown2;
        _monsters = monsters;
        _speeds = speeds;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xdd);
        writeD(_unknown1);
        writeD(_unknown2);
        writeD(8);
        for (int i = 0; i < 8; ++i) {
            writeD(_monsters[i].getObjectId());
            writeD(_monsters[i].getTemplate().npcId + 1000000);
            writeD(0x371b);
            writeD(0x2c673 + 58 * (7 - i));
            writeD(0xfffff212);
            writeD(0x2f30);
            writeD(0x2c673 + 58 * (7 - i));
            writeD(0xfffff212);
            writeF(_monsters[i].getColHeight());
            writeF(_monsters[i].getColRadius());
            writeD(120);
            for (int j = 0; j < 20; ++j) {
                writeC((_unknown1 == 0) ? _speeds[i][j] : 0);
            }
            writeD(0);
        }
    }
}
