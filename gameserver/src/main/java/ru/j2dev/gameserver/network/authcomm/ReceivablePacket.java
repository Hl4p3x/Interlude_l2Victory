package ru.j2dev.gameserver.network.authcomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket extends ru.j2dev.commons.net.nio.ReceivablePacket<AuthServerCommunication> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceivablePacket.class);

    @Override
    public AuthServerCommunication getClient() {
        return AuthServerCommunication.getInstance();
    }

    @Override
    protected ByteBuffer getByteBuffer() {
        return getClient().getReadBuffer();
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

    protected void sendPacket(final SendablePacket sp) {
        getClient().sendPacket(sp);
    }
}
