package ru.j2dev.authserver.network.l2.s2c;

import ru.j2dev.authserver.network.l2.SessionKey;

public final class PlayOk extends L2LoginServerPacket {
    private final int _playOk1;
    private final int _playOk2;

    public PlayOk(final SessionKey sessionKey) {
        _playOk1 = sessionKey.playOkID1;
        _playOk2 = sessionKey.playOkID2;
    }

    @Override
    protected void writeImpl() {
        writeC(0x7);
        writeD(_playOk1);
        writeD(_playOk2);
    }
}
