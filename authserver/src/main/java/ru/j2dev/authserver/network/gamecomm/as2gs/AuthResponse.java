package ru.j2dev.authserver.network.gamecomm.as2gs;

import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.SendablePacket;

public class AuthResponse extends SendablePacket {
    private final int serverId;
    private final String name;

    public AuthResponse(final GameServer gs) {
        serverId = gs.getId();
        name = gs.getName();
    }

    @Override
    protected void writeImpl() {
        writeC(0x0);
        writeC(serverId);
        writeS(name);
    }
}
