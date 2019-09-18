package ru.j2dev.authserver.network.gamecomm.as2gs;

import ru.j2dev.authserver.network.gamecomm.SendablePacket;

public class KickPlayer extends SendablePacket {
    private final String account;

    public KickPlayer(final String login) {
        account = login;
    }

    @Override
    protected void writeImpl() {
        writeC(0x3);
        writeS(account);
    }
}
