package ru.j2dev.gameserver.network.lineage2.cgm.lg;

import ru.j2dev.gameserver.network.lineage2.GameCrypt;

public class LGGameCryptWrapper extends GameCrypt {
    private final com.lameguard.crypt.GameCrypt _gameCrypt;

    public LGGameCryptWrapper() {
        _gameCrypt = new com.lameguard.crypt.GameCrypt();
    }

    @Override
    public void setKey(final byte[] key) {
        _gameCrypt.setKey(key);
    }

    @Override
    public void setKey(final byte[] key, final boolean value) {
        setKey(key);
    }

    @Override
    public boolean decrypt(final byte[] raw, final int offset, final int size) {
        _gameCrypt.decrypt(raw, offset, size);
        return true;
    }

    @Override
    public void encrypt(final byte[] raw, final int offset, final int size) {
        _gameCrypt.encrypt(raw, offset, size);
    }
}