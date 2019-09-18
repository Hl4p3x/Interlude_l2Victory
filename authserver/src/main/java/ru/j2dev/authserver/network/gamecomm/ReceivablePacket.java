package ru.j2dev.authserver.network.gamecomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket extends ru.j2dev.commons.net.nio.ReceivablePacket<GameServer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceivablePacket.class);

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
    public final boolean read() {
        try {
            readImpl();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return true;
    }

    @Override
    public final void run() {
        try {
            runImpl();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    protected abstract void readImpl();

    protected abstract void runImpl();

    public void sendPacket(final SendablePacket packet) {
        getGameServer().sendPacket(packet);
    }
}
