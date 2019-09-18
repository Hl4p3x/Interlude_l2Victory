package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPutEnchantTargetItemResult extends L2GameServerPacket {
    public static final L2GameServerPacket FAIL = new ExPutEnchantTargetItemResult(0);
    public static final L2GameServerPacket SUCCESS = new ExPutEnchantTargetItemResult(1);

    private int _result;

    private ExPutEnchantTargetItemResult(final int result) {
        _result = result;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x81);
        writeD(_result);
    }
}
