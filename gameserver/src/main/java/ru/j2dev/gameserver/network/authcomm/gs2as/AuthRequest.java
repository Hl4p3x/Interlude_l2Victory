package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.network.authcomm.SendablePacket;

import java.util.Arrays;

public class AuthRequest extends SendablePacket {
    @Override
    protected void writeImpl() {
        writeC(0x0);
        writeD(GameServer.AUTH_SERVER_PROTOCOL);
        writeC(Config.REQUEST_ID);
        writeC(Config.ACCEPT_ALTERNATE_ID ? 0x1 : 0x0);
        writeD(Config.AUTH_SERVER_SERVER_TYPE);
        writeD(Config.AUTH_SERVER_AGE_LIMIT);
        writeC(Config.AUTH_SERVER_GM_ONLY ? 0x1 : 0x0);
        writeC(Config.AUTH_SERVER_BRACKETS ? 0x1 : 0x0);
        writeC(Config.AUTH_SERVER_IS_PVP ? 0x1 : 0x0);
        writeS(Config.EXTERNAL_HOSTNAME);
        writeS(Config.INTERNAL_HOSTNAME);
        writeH(Config.PORTS_GAME.length);
        Arrays.stream(Config.PORTS_GAME).forEach(this::writeH);
        writeD(Config.MAXIMUM_ONLINE_USERS);
    }
}
