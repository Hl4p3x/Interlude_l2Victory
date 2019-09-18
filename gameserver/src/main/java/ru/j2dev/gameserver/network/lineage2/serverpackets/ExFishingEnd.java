package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExFishingEnd extends L2GameServerPacket {
    private final int _charId;
    private final boolean _win;

    public ExFishingEnd(final Player character, final boolean win) {
        _charId = character.getObjectId();
        _win = win;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x14);
        writeD(_charId);
        writeC(_win ? 1 : 0);
    }
}
