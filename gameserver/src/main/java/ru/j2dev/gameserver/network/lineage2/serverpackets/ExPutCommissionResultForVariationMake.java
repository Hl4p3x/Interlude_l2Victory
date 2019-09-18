package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPutCommissionResultForVariationMake extends L2GameServerPacket {
    public static final ExPutCommissionResultForVariationMake FAIL_PACKET = new ExPutCommissionResultForVariationMake();

    private int _gemstoneObjId;
    private int _serverId;
    private int _result;
    private long _gemstoneCount;
    private long _requiredGemstoneCount;

    private ExPutCommissionResultForVariationMake() {
        _gemstoneObjId = 0;
        _serverId = 1;
        _gemstoneCount = 0L;
        _requiredGemstoneCount = 0L;
        _result = 0;
    }

    public ExPutCommissionResultForVariationMake(final int gemstoneObjId, final long count) {
        _gemstoneObjId = gemstoneObjId;
        _serverId = 1;
        _gemstoneCount = count;
        _requiredGemstoneCount = count;
        _result = 1;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x54);
        writeD(_gemstoneObjId);
        writeD(_serverId);
        writeD((int) _gemstoneCount);
        writeD((int) _requiredGemstoneCount);
        writeD(_result);
    }
}
