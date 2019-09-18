package ru.j2dev.gameserver.network.authcomm.as2gs;

import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.ReceivablePacket;
import ru.j2dev.gameserver.network.authcomm.gs2as.PingResponse;

public class PingRequest extends ReceivablePacket {
    @Override
    public void readImpl() {
    }

    @Override
    protected void runImpl() {
        AuthServerCommunication.getInstance().sendPacket(new PingResponse());
    }
}
