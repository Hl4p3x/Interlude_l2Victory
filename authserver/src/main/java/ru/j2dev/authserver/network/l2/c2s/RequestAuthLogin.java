package ru.j2dev.authserver.network.l2.c2s;

import ru.j2dev.authserver.Config;
import ru.j2dev.authserver.IpBanManager;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.accounts.SessionManager;
import ru.j2dev.authserver.crypt.PasswordHash;
import ru.j2dev.authserver.network.l2.L2LoginClient;
import ru.j2dev.authserver.network.l2.s2c.LoginFail;
import ru.j2dev.authserver.network.l2.s2c.LoginOk;
import ru.j2dev.authserver.utils.Log;

import javax.crypto.Cipher;

public class RequestAuthLogin extends L2LoginClientPacket {
    private final byte[] _raw;

    public RequestAuthLogin() {
        _raw = new byte[128];
    }

    @Override
    protected void readImpl() {
        readB(_raw);
        readD();
        readD();
        readD();
        readD();
        readD();
        readD();
        readH();
        readC();
    }

    @Override
    protected void runImpl() throws Exception {
        final L2LoginClient client = getClient();
        byte[] decrypted;
        try {
            final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(2, client.getRSAPrivateKey());
            decrypted = rsaCipher.doFinal(_raw, 0, 128);
        } catch (Exception e) {
            client.closeNow(true);
            return;
        }
        String user = new String(decrypted, 94, 14).trim();
        user = user.toLowerCase();
        final String password = new String(decrypted, 108, 16).trim();
        int ncotp = decrypted[124] & 0xFF;
        ncotp |= (decrypted[125] & 0xFF) << 8;
        ncotp |= (decrypted[126] & 0xFF) << 16;
        ncotp |= (decrypted[127] & 0xFF) << 24;
        final int currentTime = (int) (System.currentTimeMillis() / 1000L);
        final Account account = new Account(user);
        account.restore();
        final String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);
        if (account.getPasswordHash() == null) {
            if (!Config.AUTO_CREATE_ACCOUNTS || !user.matches(Config.ANAME_TEMPLATE) || !password.matches(Config.APASSWD_TEMPLATE)) {
                client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
                return;
            }
            account.setPasswordHash(passwordHash);
            account.save();
        }
        boolean passwordCorrect = account.getPasswordHash().equalsIgnoreCase(passwordHash);
        if (!passwordCorrect) {
            for (final PasswordHash c : Config.LEGACY_CRYPT) {
                if (c.compare(password, account.getPasswordHash())) {
                    passwordCorrect = true;
                    account.setPasswordHash(passwordHash);
                    break;
                }
            }
        }
        if (!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect)) {
            client.closeNow(false);
            return;
        }
        if (!passwordCorrect) {
            client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
            return;
        }
        if (account.getAccessLevel() < 0) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            return;
        }
        if (account.getBanExpire() > currentTime) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            return;
        }
        if (!account.isAllowedIP(client.getIpAddress())) {
            client.close(LoginFail.LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
            return;
        }
        account.setLastAccess(currentTime);
        account.setLastIP(client.getIpAddress());
        Log.LogAccount(account);
        final SessionManager.Session session = SessionManager.getInstance().openSession(account);
        client.setAuthed(true);
        client.setLogin(user);
        client.setAccount(account);
        client.setSessionKey(session.getSessionKey());
        client.setState(L2LoginClient.LoginClientState.AUTHED);
        client.sendPacket(new LoginOk(client.getSessionKey()));
    }
}
