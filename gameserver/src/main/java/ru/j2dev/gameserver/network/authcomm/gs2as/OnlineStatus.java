package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;

public class OnlineStatus extends SendablePacket {
    private final boolean _online;

    public OnlineStatus(final boolean online) {
        _online = online;
    }

    @Override
    protected void writeImpl() {
        writeC(0x1);
        writeC(_online ? 1 : 0);
    }
}
