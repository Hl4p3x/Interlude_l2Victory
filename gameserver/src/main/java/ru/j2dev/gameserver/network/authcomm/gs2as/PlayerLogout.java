package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;

public class PlayerLogout extends SendablePacket {
    private final String account;

    public PlayerLogout(final String account) {
        this.account = account;
    }

    @Override
    protected void writeImpl() {
        writeC(0x4);
        writeS(account);
    }
}
