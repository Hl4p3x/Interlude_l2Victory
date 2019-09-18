package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.wrappers.ISmartClient;
import ru.akumu.smartguard.core.wrappers.ISmartPlayer;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.model.Player;

public class PlayerWrapper extends ISmartPlayer {
    private final HardReference<Player> _playerRef;

    public PlayerWrapper(final Player player, final ISmartClient client) {
        super(client);
        _playerRef = player.getRef();
    }

    public PlayerWrapper(final Player player) {
        this(player, new ClientWrapper(player.getNetConnection()));
    }

    protected Player getPlayer() {
        return _playerRef.get();
    }

    @Override
    public boolean isAdmin() {
        final Player player = getPlayer();
        return player != null && player.isGM();
    }

    @Override
    public String getName() {
        final Player player = getPlayer();
        if (player != null) {
            return player.getName();
        }
        return "null";
    }

    @Override
    public int getObjId() {
        final Player player = getPlayer();
        if (player != null) {
            return player.getObjectId();
        }
        return 0;
    }
}