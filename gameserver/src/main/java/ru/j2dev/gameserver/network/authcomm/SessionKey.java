package ru.j2dev.gameserver.network.authcomm;

public class SessionKey {
    public final int playOkID1;
    public final int playOkID2;
    public final int loginOkID1;
    public final int loginOkID2;
    private final int hashCode;

    public SessionKey(final int loginOK1, final int loginOK2, final int playOK1, final int playOK2) {
        playOkID1 = playOK1;
        playOkID2 = playOK2;
        loginOkID1 = loginOK1;
        loginOkID2 = loginOK2;
        int hashCode = playOK1 * 17;
        hashCode += playOK2;
        hashCode *= 37;
        hashCode += loginOK1;
        hashCode *= 51;
        hashCode += loginOK2;
        this.hashCode = hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == getClass()) {
            final SessionKey skey = (SessionKey) o;
            return playOkID1 == skey.playOkID1 && playOkID2 == skey.playOkID2 && loginOkID1 == skey.loginOkID1 && loginOkID2 == skey.loginOkID2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "[playOkID1: " + playOkID1 + " playOkID2: " + playOkID2 + " loginOkID1: " + loginOkID1 + " loginOkID2: " + loginOkID2 + "]";
    }
}
