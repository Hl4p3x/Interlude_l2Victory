package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class Ex2ndPasswordCheck extends L2GameServerPacket {
    private final int _arg0;
    private final int _arg1;

    public Ex2ndPasswordCheck(final Ex2ndPasswordCheckResult result) {
        _arg0 = result.getArg0();
        _arg1 = result.getArg1();
    }

    public Ex2ndPasswordCheck(final Ex2ndPasswordCheckResult result, final int time) {
        _arg0 = result.getArg0();
        _arg1 = time;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xe5);
        writeD(_arg0);
        writeD(_arg1);
    }

    public enum Ex2ndPasswordCheckResult {
        CREATE(0, 0),
        CHECK(1, 0),
        BLOCK_TIME(1),
        SUCCESS(2, 0),
        ERROR(3, 0);

        private final int _arg0;
        private final int _arg1;

        Ex2ndPasswordCheckResult(final int arg0, final int arg1) {
            _arg0 = arg0;
            _arg1 = arg1;
        }

        Ex2ndPasswordCheckResult(final int arg0) {
            _arg0 = arg0;
            _arg1 = -1;
        }

        public int getArg0() {
            return _arg0;
        }

        public int getArg1() {
            return _arg1;
        }
    }
}
