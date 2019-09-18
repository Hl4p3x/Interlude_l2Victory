package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket {
    public static final L2GameServerPacket FAIL = new ExPutEnchantSupportItemResult(0);
    public static final L2GameServerPacket SUCCESS = new ExPutEnchantSupportItemResult(1);

    private int _result;

    private ExPutEnchantSupportItemResult(final int result) {
        _result = result;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x82);
        writeD(_result);
    }
}
