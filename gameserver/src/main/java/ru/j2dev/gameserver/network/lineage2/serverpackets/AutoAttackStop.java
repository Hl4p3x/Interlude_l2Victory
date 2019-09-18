package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AutoAttackStop extends L2GameServerPacket {
    private final int _targetId;

    public AutoAttackStop(final int targetId) {
        _targetId = targetId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x2c);
        writeD(_targetId);
    }
}
