package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Summon;

public class SetSummonRemainTime extends L2GameServerPacket {
    private final int _maxFed;
    private final int _curFed;

    public SetSummonRemainTime(final Summon summon) {
        _curFed = summon.getCurrentFed();
        _maxFed = summon.getMaxFed();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd1);
        writeD(_maxFed);
        writeD(_curFed);
    }
}
