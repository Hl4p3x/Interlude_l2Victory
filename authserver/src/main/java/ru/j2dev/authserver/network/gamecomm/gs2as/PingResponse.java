package ru.j2dev.authserver.network.gamecomm.gs2as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ReceivablePacket;

public class PingResponse extends ReceivablePacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingResponse.class);

    private long _serverTime;

    @Override
    protected void readImpl() {
        _serverTime = readQ();
    }

    @Override
    protected void runImpl() {
        final GameServer gameServer = getGameServer();
        if (!gameServer.isAuthed()) {
            return;
        }
        gameServer.getConnection().onPingResponse();
        final long diff = System.currentTimeMillis() - _serverTime;
        if (Math.abs(diff) > 999L) {
            LOGGER.warn("Gameserver " + gameServer.getId() + " [" + gameServer.getName() + "] : time offset " + diff + " ms.");
        }
    }
}
