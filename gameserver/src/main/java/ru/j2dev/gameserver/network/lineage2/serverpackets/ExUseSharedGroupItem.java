package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.skills.TimeStamp;

public class ExUseSharedGroupItem extends L2GameServerPacket {
    private final int _itemId;
    private final int _grpId;
    private final int _remainedTime;
    private final int _totalTime;

    public ExUseSharedGroupItem(final int grpId, final TimeStamp timeStamp) {
        _grpId = grpId;
        _itemId = timeStamp.getId();
        _remainedTime = (int) (timeStamp.getReuseCurrent() / 1000L);
        _totalTime = (int) (timeStamp.getReuseBasic() / 1000L);
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x49);
        writeD(_itemId);
        writeD(_grpId);
        writeD(_remainedTime);
        writeD(_totalTime);
    }
}
