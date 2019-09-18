package ru.j2dev.gameserver.network.authcomm.as2gs;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.ReceivablePacket;
import ru.j2dev.gameserver.network.authcomm.SessionKey;
import ru.j2dev.gameserver.network.authcomm.gs2as.PlayerInGame;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterSelectionInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.LoginFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ServerClose;

public class PlayerAuthResponse extends ReceivablePacket {
    private String account;
    private boolean authed;
    private int playOkId1;
    private int playOkId2;
    private int loginOkId1;
    private int loginOkId2;
    private int lastServerId;

    public PlayerAuthResponse() {
        lastServerId = Config.REQUEST_ID;
    }

    @Override
    public void readImpl() {
        account = readS();
        authed = (readC() == 1);
        if (authed) {
            playOkId1 = readD();
            playOkId2 = readD();
            loginOkId1 = readD();
            loginOkId2 = readD();
            if (getAvaliableBytes() > 0) {
                lastServerId = readD();
            }
        }
    }

    @Override
    protected void runImpl() {
        final SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
        final GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
        if (client == null) {
            return;
        }
        if (authed && client.getSessionKey().equals(skey)) {
            client.setAuthed(true);
            client.setState(GameClient.GameClientState.AUTHED);
            client.setServerId(lastServerId);
            final GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
            if (!Config.ALLOW_MULILOGIN && oldClient != null) {
                oldClient.setAuthed(false);
                final Player activeChar = oldClient.getActiveChar();
                if (activeChar != null) {
                    activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
                    activeChar.logout();
                } else {
                    oldClient.close(ServerClose.STATIC);
                }
            }
            sendPacket(new PlayerInGame(client.getLogin()));
            final CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
            client.sendPacket(csi);
            client.setCharSelection(csi.getCharInfo());
        } else {
            client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
        }
    }
}
