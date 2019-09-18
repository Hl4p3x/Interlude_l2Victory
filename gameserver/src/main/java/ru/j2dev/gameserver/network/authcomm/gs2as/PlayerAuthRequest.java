package ru.j2dev.gameserver.network.authcomm.gs2as;

import ru.j2dev.gameserver.network.authcomm.SendablePacket;
import ru.j2dev.gameserver.network.lineage2.GameClient;

public class PlayerAuthRequest extends SendablePacket {
    private final String account;
    private final int playOkID1;
    private final int playOkID2;
    private final int loginOkID1;
    private final int loginOkID2;

    public PlayerAuthRequest(final GameClient client) {
        account = client.getLogin();
        playOkID1 = client.getSessionKey().playOkID1;
        playOkID2 = client.getSessionKey().playOkID2;
        loginOkID1 = client.getSessionKey().loginOkID1;
        loginOkID2 = client.getSessionKey().loginOkID2;
    }

    @Override
    protected void writeImpl() {
        writeC(0x2);
        writeS(account);
        writeD(playOkID1);
        writeD(playOkID2);
        writeD(loginOkID1);
        writeD(loginOkID2);
    }
}
