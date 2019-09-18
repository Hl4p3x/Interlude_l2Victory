package ru.j2dev.authserver.network.gamecomm.gs2as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.network.gamecomm.ReceivablePacket;

public class ChangeAccessLevel extends ReceivablePacket {
    public static final Logger LOGGER = LoggerFactory.getLogger(ChangeAccessLevel.class);

    private String account;
    private int level;
    private int banExpire;

    @Override
    protected void readImpl() {
        account = readS();
        level = readD();
        banExpire = readD();
    }

    @Override
    protected void runImpl() {
        final Account acc = new Account(account);
        acc.restore();
        acc.setAccessLevel(level);
        acc.setBanExpire(banExpire);
        acc.update();
    }
}
