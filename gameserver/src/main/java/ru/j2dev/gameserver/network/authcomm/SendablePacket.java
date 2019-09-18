package ru.j2dev.gameserver.network.authcomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class SendablePacket extends ru.j2dev.commons.net.nio.SendablePacket<AuthServerCommunication> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendablePacket.class);

    @Override
    public AuthServerCommunication getClient() {
        return AuthServerCommunication.getInstance();
    }

    @Override
    protected ByteBuffer getByteBuffer() {
        return getClient().getWriteBuffer();
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
