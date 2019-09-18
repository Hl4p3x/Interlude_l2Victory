package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket {
    public static final ExVariationCancelResult FAIL_PACKET = new ExVariationCancelResult(0);

    private int _closeWindow;
    private int _unk1;

    public ExVariationCancelResult(final int result) {
        _closeWindow = 1;
        _unk1 = result;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x57);
        writeD(_closeWindow);
        writeD(_unk1);
    }
}
