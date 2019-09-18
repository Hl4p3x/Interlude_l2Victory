package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.GuardConfig;
import ru.akumu.smartguard.core.SmartCore;
import ru.akumu.smartguard.core.manager.modules.ModulesManager;
import ru.akumu.smartguard.core.manager.session.L2SessionData;
import ru.akumu.smartguard.core.network.packets.MsgPacket;
import ru.j2dev.gameserver.Shutdown;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.SessionKey;
import ru.j2dev.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.LoginFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ServerClose;

public class SGAuthLoginPacket extends L2GameClientPacket {

    private String _loginName;
    private int _playKey1;
    private int _playKey2;
    private int _loginKey1;
    private int _loginKey2;
    private int _languageType;
    private byte[] _sgPayload;

    @Override
    protected void readImpl() {
        _loginName = readS(32).toLowerCase();
        _playKey2 = readD();
        _playKey1 = readD();
        _loginKey1 = readD();
        _loginKey2 = readD();
        _languageType = readD();
        if (GuardConfig.ProtectionEnabled && getByteBuffer().remaining() > 2) {
            final int lgPayloadLen = readH();
            if (getByteBuffer().remaining() >= lgPayloadLen) {
                readB(_sgPayload = new byte[lgPayloadLen]);
            }
        }
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (GuardConfig.ProtectionEnabled) {
            if (_sgPayload == null) {
                client.close(new LoginFail(LoginFail.NO_TEXT));
                return;
            }
            final ClientWrapper sgClient = new ClientWrapper(client);
            final MsgPacket msgPkt = SmartCore.checkClientLogin(sgClient, _loginName, new L2SessionData(_playKey2, _playKey1, _loginKey1, _loginKey2), _sgPayload);
            if (msgPkt != null) {
                sgClient.closeWithPacket(msgPkt);
                return;
            }
            final SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
            client.setSessionId(key);
            client.setLoginName(_loginName);
            ModulesManager.getInstance().onPlayerLogin(sgClient);
        }
        final SessionKey key2 = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
        client.setSessionId(key2);
        client.setLoginName(_loginName);
        if (Shutdown.getInstance().getMode() != -1 && Shutdown.getInstance().getSeconds() <= 15) {
            client.closeNow(false);
        } else {
            if (AuthServerCommunication.getInstance().isShutdown()) {
                client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
                return;
            }
            final GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
            if (oldClient != null) {
                oldClient.close(ServerClose.STATIC);
            }
            AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
        }
    }
}