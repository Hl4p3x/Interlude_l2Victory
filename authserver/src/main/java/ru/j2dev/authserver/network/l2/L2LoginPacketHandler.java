package ru.j2dev.authserver.network.l2;

import ru.j2dev.authserver.network.l2.c2s.AuthGameGuard;
import ru.j2dev.authserver.network.l2.c2s.RequestAuthLogin;
import ru.j2dev.authserver.network.l2.c2s.RequestServerList;
import ru.j2dev.authserver.network.l2.c2s.RequestServerLogin;
import ru.j2dev.commons.net.nio.impl.IPacketHandler;
import ru.j2dev.commons.net.nio.impl.ReceivablePacket;

import java.nio.ByteBuffer;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient> {
    @Override
    public ReceivablePacket<L2LoginClient> handlePacket(final ByteBuffer buf, final L2LoginClient client) {
        final int opcode = buf.get() & 0xFF;
        ReceivablePacket<L2LoginClient> packet = null;
        final L2LoginClient.LoginClientState state = client.getState();
        switch (state) {
            case CONNECTED: {
                if (opcode == 0x07) {
                    packet = new AuthGameGuard();
                    break;
                }
                break;
            }
            case AUTHED_GG: {
                if (opcode == 0x00) {
                    packet = new RequestAuthLogin();
                    break;
                }
                break;
            }
            case AUTHED: {
                if (opcode == 0x05) {
                    packet = new RequestServerList();
                    break;
                }
                if (opcode == 0x02) {
                    packet = new RequestServerLogin();
                    break;
                }
                break;
            }
        }
        return packet;
    }
}
