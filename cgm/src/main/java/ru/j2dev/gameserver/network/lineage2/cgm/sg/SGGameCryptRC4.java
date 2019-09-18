package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.GuardConfig;
import ru.akumu.smartguard.core.manager.LicenseManager;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.SmartGuardHelperImpl;
import ru.j2dev.gameserver.network.lineage2.cgm.rc4.RC4Decrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.rc4.RC4Encrypt;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.IntStream;

public class SGGameCryptRC4 extends GameCrypt {
    private final byte[] _inKey = new byte[16];
    private final byte[] _outKey = new byte[16];
    private boolean _isEnabled;
    private boolean _isProtected;
    private RC4Decrypt _decrypt;
    private RC4Encrypt _encrypt;

    public SGGameCryptRC4() {
    }

    private static byte[] makeRC4NetworkKey(final LicenseManager.KeyType type, final byte[] xorKey) {
        if (!type.network) {
            throw new InvalidParameterException("KeyType must be network compatable.");
        }
        if (xorKey == null) {
            throw new InvalidParameterException("Xor key can not be null.");
        }
        final byte[] baseKey = SmartGuardHelperImpl.getSGKey(type);
        if (baseKey == null) {
            return null;
        }
        final byte[] key = Arrays.copyOf(baseKey, baseKey.length);
        IntStream.range(0, key.length).forEach(ko -> key[ko] ^= xorKey[ko % xorKey.length]);
        return key;
    }

    @Override
    public void setKey(final byte[] key) {
        if (GuardConfig.ProtectionEnabled) {
            final byte[] inKey = makeRC4NetworkKey(LicenseManager.KeyType.IN, key);
            final byte[] outKo = makeRC4NetworkKey(LicenseManager.KeyType.OUT, key);
            _decrypt = new RC4Decrypt(inKey);
            _encrypt = new RC4Encrypt(outKo);
            System.arraycopy(inKey, 0, _inKey, 0, 16);
            System.arraycopy(outKo, 0, _outKey, 0, 16);
            _isProtected = true;
        } else {
            System.arraycopy(key, 0, _inKey, 0, 16);
            System.arraycopy(key, 0, _outKey, 0, 16);
            _isProtected = false;
        }
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
        if (_isProtected) {
            _decrypt.decrypt(raw, offset, size);
            return true;
        }
        int temp = 0;
        for (int i = 0; i < size; ++i) {
            final int temp2 = raw[offset + i] & 0xFF;
            raw[offset + i] = (byte) (temp2 ^ _inKey[i & 0xF] ^ temp);
            temp = temp2;
        }
        int old = _inKey[8] & 0xFF;
        old |= (_inKey[9] << 8 & 0xFF00);
        old |= (_inKey[10] << 16 & 0xFF0000);
        old |= (_inKey[11] << 24 & 0xFF000000);
        old += size;
        _inKey[8] = (byte) (old & 0xFF);
        _inKey[9] = (byte) (old >> 8 & 0xFF);
        _inKey[10] = (byte) (old >> 16 & 0xFF);
        _inKey[11] = (byte) (old >> 24 & 0xFF);
        return true;
    }

    @Override
    public void encrypt(final byte[] raw, final int offset, final int size) {
        if (!_isEnabled) {
            _isEnabled = true;
            return;
        }
        if (_isProtected) {
            _encrypt.encrypt(raw, offset, size);
            return;
        }
        int temp = 0;
        for (int i = 0; i < size; ++i) {
            final int temp2 = raw[offset + i] & 0xFF;
            temp ^= (temp2 ^ _outKey[i & 0xF]);
            raw[offset + i] = (byte) temp;
        }
        int old = _outKey[8] & 0xFF;
        old |= (_outKey[9] << 8 & 0xFF00);
        old |= (_outKey[10] << 16 & 0xFF0000);
        old |= (_outKey[11] << 24 & 0xFF000000);
        old += size;
        _outKey[8] = (byte) (old & 0xFF);
        _outKey[9] = (byte) (old >> 8 & 0xFF);
        _outKey[10] = (byte) (old >> 16 & 0xFF);
        _outKey[11] = (byte) (old >> 24 & 0xFF);
    }
}