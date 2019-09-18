package ru.j2dev.authserver.network.l2.s2c;

import ru.j2dev.authserver.network.l2.L2LoginClient;

public final class Init extends L2LoginServerPacket {
    private final int _sessionId;
    private final byte[] _publicKey;
    private final byte[] _blowfishKey;

    public Init(final L2LoginClient client) {
        this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
    }

    public Init(final byte[] publickey, final byte[] blowfishkey, final int sessionId) {
        _sessionId = sessionId;
        _publicKey = publickey;
        _blowfishKey = blowfishkey;
    }

    @Override
    protected void writeImpl() {
        writeC(0x0);
        writeD(_sessionId);
        writeD(0xc621);
        writeB(_publicKey);
        writeD(0x29dd954e);
        writeD(0x77c39cfc);
        writeD(0x97adb620);
        writeD(0x7bde0f7);
        writeB(_blowfishKey);
        writeC(0x0);
    }
}
