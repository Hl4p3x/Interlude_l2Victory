package ru.j2dev.authserver.crypt;

import ru.j2dev.commons.util.Rnd;

import java.io.IOException;

public class LoginCrypt {
    private static final byte[] STATIC_BLOWFISH_KEY = {107, 96, -53, 91, -126, -50, -112, -79, -52, 43, 108, 85, 108, 108, 108, 108};

    private NewCrypt _staticCrypt;
    private NewCrypt _crypt;
    private boolean _static;

    public LoginCrypt() {
        _static = true;
    }

    public void setKey(final byte[] key) {
        _staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
        _crypt = new NewCrypt(key);
    }

    public boolean decrypt(final byte[] raw, final int offset, final int size) throws IOException {
        _crypt.decrypt(raw, offset, size);
        return NewCrypt.verifyChecksum(raw, offset, size);
    }

    public int encrypt(final byte[] raw, final int offset, int size) throws IOException {
        size += 4;
        if (_static) {
            size += 4;
            size += 8 - size % 8;
            NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
            _staticCrypt.crypt(raw, offset, size);
            _static = false;
        } else {
            size += 8 - size % 8;
            NewCrypt.appendChecksum(raw, offset, size);
            _crypt.crypt(raw, offset, size);
        }
        return size;
    }
}
