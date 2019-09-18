package ru.j2dev.authserver.network.gamecomm.as2gs;

import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.accounts.SessionManager;
import ru.j2dev.authserver.network.gamecomm.SendablePacket;
import ru.j2dev.authserver.network.l2.SessionKey;

public class PlayerAuthResponse extends SendablePacket {
    private String login;
    private boolean authed;
    private int playOkID1;
    private int playOkID2;
    private int loginOkID1;
    private int loginOkID2;
    private double bonus;
    private int bonusExpire;
    private int lastServerId;

    public PlayerAuthResponse(final SessionManager.Session session, final boolean authed, final int lastServer) {
        final Account account = session.getAccount();
        login = account.getLogin();
        this.authed = authed;
        if (authed) {
            final SessionKey skey = session.getSessionKey();
            playOkID1 = skey.playOkID1;
            playOkID2 = skey.playOkID2;
            loginOkID1 = skey.loginOkID1;
            loginOkID2 = skey.loginOkID2;
            lastServerId = lastServer;
        }
    }

    public PlayerAuthResponse(final String account) {
        login = account;
        authed = false;
    }

    @Override
    protected void writeImpl() {
        writeC(2);
        writeS(login);
        writeC(authed ? 1 : 0);
        if (authed) {
            writeD(playOkID1);
            writeD(playOkID2);
            writeD(loginOkID1);
            writeD(loginOkID2);
            writeD(lastServerId);
        }
    }
}
