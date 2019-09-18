package ru.j2dev.authserver.network.l2.s2c;

import ru.j2dev.authserver.network.l2.SessionKey;

public final class LoginOk extends L2LoginServerPacket {
    private final int _loginOk1;
    private final int _loginOk2;

    public LoginOk(final SessionKey sessionKey) {
        _loginOk1 = sessionKey.loginOkID1;
        _loginOk2 = sessionKey.loginOkID2;
    }

    @Override
    protected void writeImpl() {
        writeC(0x3);
        writeD(_loginOk1);
        writeD(_loginOk2);
        writeD(0x0);
        writeD(0x0);
        writeD(0x3ea);
        writeD(0x0);
        writeD(0x0);
        writeD(0x0);
        writeB(new byte[16]);
    }
}
