package ru.j2dev.gameserver.network.lineage2.serverpackets;

import java.util.List;

public class ExGetBossRecord extends L2GameServerPacket {
    private final List<BossRecordInfo> _bossRecordInfo;
    private final int _ranking;
    private final int _totalPoints;

    public ExGetBossRecord(final int ranking, final int totalScore, final List<BossRecordInfo> bossRecordInfo) {
        _ranking = ranking;
        _totalPoints = totalScore;
        _bossRecordInfo = bossRecordInfo;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x33);
        writeD(_ranking);
        writeD(_totalPoints);
        writeD(_bossRecordInfo.size());
        _bossRecordInfo.forEach(w -> {
            writeD(w._bossId);
            writeD(w._points);
            writeD(w._unk1);
        });
    }

    public static class BossRecordInfo {
        public final int _bossId;
        public final int _points;
        public final int _unk1;

        public BossRecordInfo(final int bossId, final int points, final int unk1) {
            _bossId = bossId;
            _points = points;
            _unk1 = unk1;
        }
    }
}
