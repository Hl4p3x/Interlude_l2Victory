package ru.j2dev.gameserver.network.authcomm.as2gs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.ReceivablePacket;

public class LoginServerFail extends ReceivablePacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServerFail.class);
    private static final String[] reasons = {"none", "IP banned", "IP reserved", "wrong hexid", "ID reserved", "no free ID", "not authed", "already logged in"};

    private int _reason;

    public String getReason() {
        return reasons[_reason];
    }

    @Override
    protected void readImpl() {
        _reason = readC();
    }

    @Override
    protected void runImpl() {
        LOGGER.warn("Authserver registration failed! Reason: " + getReason());
        AuthServerCommunication.getInstance().restart();
    }
}
