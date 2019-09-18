package ru.j2dev.gameserver.network.authcomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.network.authcomm.as2gs.*;

import java.nio.ByteBuffer;

public class PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandler.class);

    public static ReceivablePacket handlePacket(final ByteBuffer buf) {
        ReceivablePacket packet = null;
        final int id = buf.get() & 0xFF;
        switch (id) {
            case 0x0: {
                packet = new AuthResponse();
                break;
            }
            case 0x1: {
                packet = new LoginServerFail();
                break;
            }
            case 0x2: {
                packet = new PlayerAuthResponse();
                break;
            }
            case 0x3: {
                packet = new KickPlayer();
                break;
            }
            case 0xa1: {
                packet = new NotifyPwdCngResult();
                break;
            }
            case 0xff: {
                packet = new PingRequest();
                break;
            }
            default: {
                LOGGER.error("Received unknown packet: " + Integer.toHexString(id));
                break;
            }
        }
        return packet;
    }
}
