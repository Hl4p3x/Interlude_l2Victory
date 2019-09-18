package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPutItemResultForVariationMake extends L2GameServerPacket {
    public static final ExPutItemResultForVariationMake FAIL_PACKET = new ExPutItemResultForVariationMake(0, false);

    private int _itemObjId;
    private int _serverId;
    private int _result;

    public ExPutItemResultForVariationMake(final int itemObjId, final boolean isSuccess) {
        _itemObjId = itemObjId;
        _serverId = 0;
        _result = (isSuccess ? 1 : 0);
    }

    @Override
    protected void writeImpl() {
        writeEx(0x52);
        writeD(_itemObjId);
        writeD(_serverId);
        writeD(_result);
    }
}
