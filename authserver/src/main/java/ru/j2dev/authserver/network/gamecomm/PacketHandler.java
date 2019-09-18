package ru.j2dev.authserver.network.gamecomm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.network.gamecomm.gs2as.*;

import java.nio.ByteBuffer;

public class PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandler.class);

    public static ReceivablePacket handlePacket(final GameServer gs, final ByteBuffer buf) {
        ReceivablePacket packet = null;
        final int id = buf.get() & 0xFF;
        if (!gs.isAuthed()) {
            switch (id) {
                case 0x0: {
                    packet = new AuthRequest();
                    break;
                }
                default: {
                    LOGGER.error("Received unknown packet: " + Integer.toHexString(id));
                    break;
                }
            }
        } else {
            switch (id) {
                case 0x1: {
                    packet = new OnlineStatus();
                    break;
                }
                case 0x2: {
                    packet = new PlayerAuthRequest();
                    break;
                }
                case 0x3: {
                    packet = new PlayerInGame();
                    break;
                }
                case 0x4: {
                    packet = new PlayerLogout();
                    break;
                }
                case 0x11: {
                    packet = new ChangeAccessLevel();
                    break;
                }
                case 0xa0: {
                    packet = new IGPwdCng();
                    break;
                }
                case 0xff: {
                    packet = new PingResponse();
                    break;
                }
                default: {
                    LOGGER.error("Received unknown packet: " + Integer.toHexString(id));
                    break;
                }
            }
        }
        return packet;
    }
}
