package ru.j2dev.authserver.network.gamecomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class SendablePacket extends ru.j2dev.commons.net.nio.SendablePacket<GameServer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendablePacket.class);

    protected GameServer _gs;
    protected ByteBuffer _buf;

    @Override
    protected ByteBuffer getByteBuffer() {
        return _buf;
    }

    protected void setByteBuffer(final ByteBuffer buf) {
        _buf = buf;
    }

    @Override
    public GameServer getClient() {
        return _gs;
    }

    protected void setClient(final GameServer gs) {
        _gs = gs;
    }

    public GameServer getGameServer() {
        return getClient();
    }

    @Override
    public boolean write() {
        try {
            writeImpl();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return true;
    }

    protected abstract void writeImpl();
}
