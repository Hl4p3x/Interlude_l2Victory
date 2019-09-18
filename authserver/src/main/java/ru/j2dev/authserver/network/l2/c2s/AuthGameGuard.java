package ru.j2dev.authserver.network.l2.c2s;

import ru.j2dev.authserver.network.l2.L2LoginClient;
import ru.j2dev.authserver.network.l2.s2c.GGAuth;
import ru.j2dev.authserver.network.l2.s2c.LoginFail;

public class AuthGameGuard extends L2LoginClientPacket {
    private int _sessionId;

    @Override
    protected void readImpl() {
        _sessionId = readD();
    }

    @Override
    protected void runImpl() {
        final L2LoginClient client = getClient();
        if (_sessionId == 0 || _sessionId == client.getSessionId()) {
            client.setState(L2LoginClient.LoginClientState.AUTHED_GG);
            client.sendPacket(new GGAuth(client.getSessionId()));
        } else {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
        }
    }
}
