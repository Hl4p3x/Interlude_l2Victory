package ru.j2dev.gameserver.model.actor.instances.player;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.gameserver.model.Player;

public class Friend {
    private final int _objectId;
    private String _name;
    private int _classId;
    private int _level;
    private HardReference<Player> _playerRef;

    public Friend(final int objectId, final String name, final int classId, final int level) {
        _playerRef = HardReferences.emptyRef();
        _objectId = objectId;
        _name = name;
        _classId = classId;
        _level = level;
    }

    public Friend(final Player player) {
        _playerRef = HardReferences.emptyRef();
        _objectId = player.getObjectId();
        update(player, true);
    }

    public void update(final Player player, final boolean set) {
        _level = player.getLevel();
        _name = player.getName();
        _classId = player.getActiveClassId();
        _playerRef = (set ? player.getRef() : HardReferences.emptyRef());
    }

    public String getName() {
        final Player player = getPlayer();
        return (player == null) ? _name : player.getName();
    }

    public int getObjectId() {
        return _objectId;
    }

    public int getClassId() {
        final Player player = getPlayer();
        return (player == null) ? _classId : player.getActiveClassId();
    }

    public int getLevel() {
        final Player player = getPlayer();
        return (player == null) ? _level : player.getLevel();
    }

    public boolean isOnline() {
        final Player player = _playerRef.get();
        return player != null && !player.isInOfflineMode();
    }

    public Player getPlayer() {
        final Player player = _playerRef.get();
        return (player != null && !player.isInOfflineMode()) ? player : null;
    }
}
