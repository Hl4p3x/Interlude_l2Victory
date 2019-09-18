package ru.j2dev.authserver.network.gamecomm.gs2as;

import ru.j2dev.authserver.Config;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.crypt.PasswordHash;
import ru.j2dev.authserver.network.gamecomm.ReceivablePacket;
import ru.j2dev.authserver.network.gamecomm.as2gs.NotifyPwdCngResult;

public class IGPwdCng extends ReceivablePacket {
    private int _requestor_oid;
    private String _account;
    private String _old_pass;
    private String _new_pass;

    @Override
    protected void readImpl() {
        _requestor_oid = readD();
        _account = readS();
        _old_pass = readS();
        _new_pass = readS();
    }

    @Override
    protected void runImpl() {
        try {
            final Account acc = new Account(_account);
            acc.restore();
            if (acc.getPasswordHash() == null) {
                sendPacket(new NotifyPwdCngResult(_requestor_oid, 4));
                return;
            }
            if (!_new_pass.matches(Config.APASSWD_TEMPLATE)) {
                sendPacket(new NotifyPwdCngResult(_requestor_oid, 3));
                return;
            }
            boolean passwordCorrect = Config.DEFAULT_CRYPT.compare(_old_pass, acc.getPasswordHash());
            if (!passwordCorrect) {
                for (final PasswordHash c : Config.LEGACY_CRYPT) {
                    if (c.compare(_old_pass, acc.getPasswordHash())) {
                        passwordCorrect = true;
                        break;
                    }
                }
            }
            if (!passwordCorrect) {
                sendPacket(new NotifyPwdCngResult(_requestor_oid, 2));
                return;
            }
            acc.setPasswordHash(Config.DEFAULT_CRYPT.encrypt(_new_pass));
            acc.update();
            sendPacket(new NotifyPwdCngResult(_requestor_oid, 1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
