package ru.j2dev.gameserver.network.authcomm.as2gs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.ReceivablePacket;
import ru.j2dev.gameserver.network.authcomm.gs2as.OnlineStatus;
import ru.j2dev.gameserver.network.authcomm.gs2as.PlayerInGame;

import java.util.Arrays;

public class AuthResponse extends ReceivablePacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResponse.class);

    private int _serverId;
    private String _serverName;

    @Override
    protected void readImpl() {
        _serverId = readC();
        _serverName = readS();
    }

    @Override
    protected void runImpl() {
        LOGGER.info("|=====================AUTHRESPONE=========================|");
        LOGGER.info("Registered on authserver as " + _serverId + " [" + _serverName + "]");
        sendPacket(new OnlineStatus(true));
        final String[] accounts = AuthServerCommunication.getInstance().getAccounts();
        Arrays.stream(accounts).map(PlayerInGame::new).forEach(this::sendPacket);
    }
}
