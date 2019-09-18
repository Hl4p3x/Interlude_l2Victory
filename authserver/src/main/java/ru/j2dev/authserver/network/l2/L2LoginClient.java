package ru.j2dev.authserver.network.l2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.Config;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.crypt.LoginCrypt;
import ru.j2dev.authserver.crypt.ScrambledKeyPair;
import ru.j2dev.authserver.network.l2.s2c.AccountKicked;
import ru.j2dev.authserver.network.l2.s2c.L2LoginServerPacket;
import ru.j2dev.authserver.network.l2.s2c.LoginFail;
import ru.j2dev.commons.net.nio.impl.MMOClient;
import ru.j2dev.commons.net.nio.impl.MMOConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(L2LoginClient.class);
    private final String _ipAddr = getConnection().getSocket().getInetAddress().getHostAddress();
    private LoginClientState _state = LoginClientState.CONNECTED;
    private LoginCrypt _loginCrypt;
    private ScrambledKeyPair _scrambledPair = Config.getScrambledRSAKeyPair();
    private byte[] _blowfishKey = Config.getBlowfishKey();
    private String _login;
    private SessionKey _skey;
    private Account _account;
    private int _sessionId;

    public L2LoginClient(final MMOConnection<L2LoginClient> con) {
        super(con);
        (_loginCrypt = new LoginCrypt()).setKey(_blowfishKey);
        _sessionId = con.hashCode();
    }

    @Override
    public boolean decrypt(final ByteBuffer buf, final int size) {
        boolean ret;
        try {
            ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
        } catch (IOException e) {
            LOGGER.error("", e);
            closeNow(true);
            return false;
        }
        if (!ret) {
            closeNow(true);
        }
        return ret;
    }

    @Override
    public boolean encrypt(final ByteBuffer buf, int size) {
        final int offset = buf.position();
        try {
            size = _loginCrypt.encrypt(buf.array(), offset, size);
        } catch (IOException e) {
            LOGGER.error("", e);
            return false;
        }
        buf.position(offset + size);
        return true;
    }

    public LoginClientState getState() {
        return _state;
    }

    public void setState(final LoginClientState state) {
        _state = state;
    }

    public byte[] getBlowfishKey() {
        return _blowfishKey;
    }

    public byte[] getScrambledModulus() {
        return _scrambledPair.getScrambledModulus();
    }

    public RSAPrivateKey getRSAPrivateKey() {
        return (RSAPrivateKey) _scrambledPair.getKeyPair().getPrivate();
    }

    public String getLogin() {
        return _login;
    }

    public void setLogin(final String login) {
        _login = login;
    }

    public Account getAccount() {
        return _account;
    }

    public void setAccount(final Account account) {
        _account = account;
    }

    public SessionKey getSessionKey() {
        return _skey;
    }

    public void setSessionKey(final SessionKey skey) {
        _skey = skey;
    }

    public int getSessionId() {
        return _sessionId;
    }

    public void setSessionId(final int val) {
        _sessionId = val;
    }

    public void sendPacket(final L2LoginServerPacket lsp) {
        if (isConnected()) {
            getConnection().sendPacket(lsp);
        }
    }

    public void close(final LoginFail.LoginFailReason reason) {
        if (isConnected()) {
            getConnection().close(new LoginFail(reason));
        }
    }

    public void close(final AccountKicked.AccountKickedReason reason) {
        if (isConnected()) {
            getConnection().close(new AccountKicked(reason));
        }
    }

    public void close(final L2LoginServerPacket lsp) {
        if (isConnected()) {
            getConnection().close(lsp);
        }
    }

    @Override
    public void onDisconnection() {
        _state = LoginClientState.DISCONNECTED;
        _skey = null;
        _loginCrypt = null;
        _scrambledPair = null;
        _blowfishKey = null;
    }

    @Override
    public String toString() {
        switch (_state) {
            case AUTHED: {
                return "[ Account : " + getLogin() + " IP: " + getIpAddress() + "]";
            }
            default: {
                return "[ State : " + getState() + " IP: " + getIpAddress() + "]";
            }
        }
    }

    public String getIpAddress() {
        return _ipAddr;
    }

    @Override
    protected void onForcedDisconnection() {
    }

    public enum LoginClientState {
        CONNECTED,
        AUTHED_GG,
        AUTHED,
        DISCONNECTED
    }
}
