package ru.j2dev.authserver.network.gamecomm.gs2as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.GameServerManager;
import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ReceivablePacket;
import ru.j2dev.authserver.network.gamecomm.as2gs.AuthResponse;
import ru.j2dev.authserver.network.gamecomm.as2gs.LoginServerFail;

import java.util.stream.IntStream;

public class AuthRequest extends ReceivablePacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthRequest.class);

    private int _protocolVersion;
    private int requestId;
    private boolean acceptAlternateID;
    private String externalIp;
    private String internalIp;
    private int maxOnline;
    private int _serverType;
    private int _ageLimit;
    private boolean _gmOnly;
    private boolean _brackets;
    private boolean _pvp;
    private int[] ports;

    @Override
    protected void readImpl() {
        _protocolVersion = readD();
        requestId = readC();
        acceptAlternateID = (readC() == 1);
        _serverType = readD();
        _ageLimit = readD();
        _gmOnly = (readC() == 1);
        _brackets = (readC() == 1);
        _pvp = (readC() == 1);
        externalIp = readS();
        internalIp = readS();
        ports = new int[readH()];
        IntStream.range(0, ports.length).forEach(i -> ports[i] = readH());
        maxOnline = readD();
    }

    @Override
    protected void runImpl() {
        LOGGER.info("Trying to register gameserver: " + requestId + " [" + getGameServer().getConnection().getIpAddress() + "]");
        int failReason = 0;
        GameServer gs = getGameServer();
        if (GameServerManager.getInstance().registerGameServer(requestId, gs)) {
            gs.setPorts(ports);
            gs.setExternalHost(externalIp);
            gs.setInternalHost(internalIp);
            gs.setMaxPlayers(maxOnline);
            gs.setPvp(_pvp);
            gs.setServerType(_serverType);
            gs.setShowingBrackets(_brackets);
            gs.setGmOnly(_gmOnly);
            gs.setAgeLimit(_ageLimit);
            gs.setProtocol(_protocolVersion);
            gs.setAuthed(true);
            gs.getConnection().startPingTask();
        } else if (acceptAlternateID) {
            if (GameServerManager.getInstance().registerGameServer(gs = getGameServer())) {
                gs.setPorts(ports);
                gs.setExternalHost(externalIp);
                gs.setInternalHost(internalIp);
                gs.setMaxPlayers(maxOnline);
                gs.setPvp(_pvp);
                gs.setServerType(_serverType);
                gs.setShowingBrackets(_brackets);
                gs.setGmOnly(_gmOnly);
                gs.setAgeLimit(_ageLimit);
                gs.setProtocol(_protocolVersion);
                gs.setAuthed(true);
                gs.getConnection().startPingTask();
            } else {
                failReason = 5;
            }
        } else {
            failReason = 4;
        }
        if (failReason != 0) {
            LOGGER.info("Gameserver registration failed.");
            sendPacket(new LoginServerFail(failReason));
            return;
        }
        LOGGER.info("Gameserver registration successful.");
        sendPacket(new AuthResponse(gs));
    }
}
