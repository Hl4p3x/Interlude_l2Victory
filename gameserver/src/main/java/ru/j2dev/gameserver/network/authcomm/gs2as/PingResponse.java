package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;

public class PingResponse extends SendablePacket {
    @Override
    protected void writeImpl() {
        writeC(0xff);
        writeQ(System.currentTimeMillis());
    }
}
