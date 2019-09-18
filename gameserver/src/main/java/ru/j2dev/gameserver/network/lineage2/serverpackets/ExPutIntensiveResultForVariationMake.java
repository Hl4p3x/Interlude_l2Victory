package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPutIntensiveResultForVariationMake extends L2GameServerPacket {
    public static final ExPutIntensiveResultForVariationMake FAIL_PACKET = new ExPutIntensiveResultForVariationMake(0, 0, 0, 0L, false);

    private int _refinerItemObjId;
    private int _lifestoneItemId;
    private int _gemstoneItemId;
    private int _result;
    private long _gemstoneCount;

    public ExPutIntensiveResultForVariationMake(final int refinerItemObjId, final int lifeStoneId, final int gemstoneItemId, final long gemstoneCount, final boolean isSuccess) {
        _refinerItemObjId = refinerItemObjId;
        _lifestoneItemId = lifeStoneId;
        _gemstoneItemId = gemstoneItemId;
        _gemstoneCount = gemstoneCount;
        _result = (isSuccess ? 1 : 0);
    }

    @Override
    protected void writeImpl() {
        writeEx(0x53);
        writeD(_refinerItemObjId);
        writeD(_lifestoneItemId);
        writeD(_gemstoneItemId);
        writeD((int) _gemstoneCount);
        writeD(_result);
    }
}
