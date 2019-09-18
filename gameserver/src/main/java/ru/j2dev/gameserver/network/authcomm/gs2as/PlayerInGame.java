package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;

public class PlayerInGame extends SendablePacket {
    private final String account;

    public PlayerInGame(final String account) {
        this.account = account;
    }

    @Override
    protected void writeImpl() {
        writeC(0x3);
        writeS(account);
    }
}
