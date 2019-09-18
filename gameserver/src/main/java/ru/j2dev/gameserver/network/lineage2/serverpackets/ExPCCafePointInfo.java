package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExPCCafePointInfo extends L2GameServerPacket {
    private final int _mAddPoint;
    private final int _mPeriodType;
    private final int _pointType;
    private final int _pcBangPoints;
    private final int _remainTime;

    public ExPCCafePointInfo(final Player player, final int mAddPoint, final int mPeriodType, final int pointType, final int remainTime) {
        _pcBangPoints = player.getPcBangPoints();
        _mAddPoint = mAddPoint;
        _mPeriodType = mPeriodType;
        _pointType = pointType;
        _remainTime = remainTime;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x31);
        writeD(_pcBangPoints);
        writeD(_mAddPoint);
        writeC(_mPeriodType);
        writeD(_remainTime);
        writeC(_pointType);
    }
}
