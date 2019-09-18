package ru.j2dev.authserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GGAuth extends L2LoginServerPacket {
    public static int SKIP_GG_AUTH_REQUEST = 11;
    static Logger LOGGER = LoggerFactory.getLogger(GGAuth.class);

    private final int _response;

    public GGAuth(final int response) {
        _response = response;
    }

    @Override
    protected void writeImpl() {
        writeC(0xb);
        writeD(_response);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
    }
}
