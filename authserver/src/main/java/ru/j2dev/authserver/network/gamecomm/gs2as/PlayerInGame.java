package ru.j2dev.authserver.network.gamecomm.gs2as;

import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ReceivablePacket;

public class PlayerInGame extends ReceivablePacket {
    private String account;

    @Override
    protected void readImpl() {
        account = readS();
    }

    @Override
    protected void runImpl() {
        final GameServer gs = getGameServer();
        if (gs.isAuthed()) {
            gs.addAccount(account);
        }
    }
}
