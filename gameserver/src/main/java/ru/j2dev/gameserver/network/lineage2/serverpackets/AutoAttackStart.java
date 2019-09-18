package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AutoAttackStart extends L2GameServerPacket {
    private final int _targetId;

    public AutoAttackStart(final int targetId) {
        _targetId = targetId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x2b);
        writeD(_targetId);
    }
}
