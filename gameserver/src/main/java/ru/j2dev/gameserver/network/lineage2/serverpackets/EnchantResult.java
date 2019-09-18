package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class EnchantResult extends L2GameServerPacket {
    public static final EnchantResult SUCESS = new EnchantResult(0, 0, 0L);
    public static final EnchantResult CANCEL = new EnchantResult(2, 0, 0L);
    public static final EnchantResult BLESSED_FAILED = new EnchantResult(3, 0, 0L);
    public static final EnchantResult FAILED_NO_CRYSTALS = new EnchantResult(4, 0, 0L);
    public static final EnchantResult ANCIENT_FAILED = new EnchantResult(5, 0, 0L);

    private final int _resultId;
    private final int _crystalId;
    private final long _count;

    public EnchantResult(final int resultId, final int crystalId, final long count) {
        _resultId = resultId;
        _crystalId = crystalId;
        _count = count;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x81);
        writeD(_resultId);
        writeD(_crystalId);
        writeQ(_count);
    }
}
