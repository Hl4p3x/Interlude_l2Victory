package ru.j2dev.gameserver.network.lineage2.cgm.sxg;

import org.strixplatform.configs.MainConfig;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.rc4.RC4Decrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.rc4.RC4Encrypt;

public class SXGGameCryptWrapper extends GameCrypt {
    private byte[] _inKey;
    private byte[] _outKey;
    private boolean _isEnabled;
    private RC4Decrypt _decrypt;
    private RC4Encrypt _encrypt;

    public SXGGameCryptWrapper() {
        _isEnabled = false;
    }

    private static byte[] ksn2(final byte[] key) {
        final byte[] keyInit = key.clone();
        if (MainConfig.STX_PF_XOR_KEY != null && MainConfig.STX_PF_XOR_KEY.length() > 0) {
            final byte[] xorKey = MainConfig.STX_PF_XOR_KEY.getBytes();
            for (int i = 0; i < 8; ++i) {
                keyInit[i] ^= xorKey[i % 4];
            }
        }
        return keyInit;
    }

    @Override
    public void setKey(final byte[] key) {
        _inKey = key.clone();
        _outKey = key.clone();
        _decrypt = new RC4Decrypt(ksn2(_inKey));
        _encrypt = new RC4Encrypt(ksn2(_outKey));
    }

    @Override
    public void setKey(final byte[] key, final boolean value) {
        setKey(key);
    }

    @Override
    public boolean decrypt(final byte[] raw, final int offset, final int size) {
        if (!_isEnabled) {
            return true;
        }
        _decrypt.decrypt(raw, offset, size);
        return true;
    }

    @Override
    public void encrypt(final byte[] raw, final int offset, final int size) {
        if (!_isEnabled) {
            _isEnabled = true;
            return;
        }
        _encrypt.encrypt(raw, offset, size);
    }
}
