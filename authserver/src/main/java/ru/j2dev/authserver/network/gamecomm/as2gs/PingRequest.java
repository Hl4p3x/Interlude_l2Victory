package ru.j2dev.authserver.network.gamecomm.as2gs;

import ru.j2dev.authserver.network.gamecomm.SendablePacket;

public class PingRequest extends SendablePacket {
    @Override
    protected void writeImpl() {
        writeC(0xff);
    }
}
