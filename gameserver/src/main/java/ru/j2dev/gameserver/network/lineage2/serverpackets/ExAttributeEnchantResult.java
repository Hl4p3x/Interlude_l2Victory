package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExAttributeEnchantResult extends L2GameServerPacket {
    private final int _result;

    public ExAttributeEnchantResult(final int unknown) {
        _result = unknown;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x61);
        writeD(_result);
    }
}
