package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class Ex2ndPasswordVerify extends L2GameServerPacket {
    private final Ex2ndPasswordVerifyResult _result;
    private final int _arg;

    public Ex2ndPasswordVerify(final Ex2ndPasswordVerifyResult result) {
        _result = result;
        _arg = 0;
    }

    public Ex2ndPasswordVerify(final Ex2ndPasswordVerifyResult result, final int count) {
        _result = result;
        _arg = count;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xe6);
        writeD(_result.getVal());
        writeD(_arg);
    }

    public enum Ex2ndPasswordVerifyResult {
        SUCCESS(0),
        FAILED(1),
        BLOCK_HOMEPAGE(2),
        ERROR(3);

        private final int _val;

        Ex2ndPasswordVerifyResult(final int arg) {
            _val = arg;
        }

        public int getVal() {
            return _val;
        }
    }
}
