package ru.j2dev.authserver.network.l2.c2s;

import ru.j2dev.authserver.GameServerManager;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ProxyServer;
import ru.j2dev.authserver.network.l2.L2LoginClient;
import ru.j2dev.authserver.network.l2.SessionKey;
import ru.j2dev.authserver.network.l2.s2c.LoginFail;
import ru.j2dev.authserver.network.l2.s2c.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket {
    private int _loginOkID1;
    private int _loginOkID2;
    private int _serverId;

    @Override
    protected void readImpl() {
        _loginOkID1 = readD();
        _loginOkID2 = readD();
        _serverId = readC();
    }

    @Override
    protected void runImpl() {
        final L2LoginClient client = getClient();
        final SessionKey skey = client.getSessionKey();
        if (skey == null || !skey.checkLoginPair(_loginOkID1, _loginOkID2)) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            return;
        }
        final Account account = client.getAccount();
        GameServer gs = GameServerManager.getInstance().getGameServerById(_serverId);
        if (gs == null) {
            final ProxyServer ps = GameServerManager.getInstance().getProxyServerById(_serverId);
            if (ps != null) {
                gs = GameServerManager.getInstance().getGameServerById(ps.getOrigServerId());
            }
        }
        if (gs == null || !gs.isAuthed() || (gs.getOnline() >= gs.getMaxPlayers() && account.getAccessLevel() < 50)) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            return;
        }
        if (gs.isGmOnly() && account.getAccessLevel() < 100) {
            client.close(LoginFail.LoginFailReason.REASON_SERVER_MAINTENANCE);
            return;
        }
        account.setLastServer(_serverId);
        account.update();
        client.close(new PlayOk(skey));
    }
}
