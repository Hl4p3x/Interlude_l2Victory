package ru.j2dev.gameserver.model.actor.instances.player;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.dao.CharacterFriendDAO;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2Friend;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2FriendStatus;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

public class FriendList {
    private final Player _owner;
    private Map<Integer, Friend> _friendList;

    public FriendList(final Player owner) {
        _friendList = Collections.emptyMap();
        _owner = owner;
    }

    public void restore() {
        _friendList = CharacterFriendDAO.getInstance().select(_owner);
    }

    public void removeFriend(final String name) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        final int objectId = removeFriend0(name);
        if (objectId > 0) {
            final Player friendChar = World.getPlayer(objectId);
            _owner.sendPacket(new SystemMessage(133).addString(name), new L2Friend(name, false, friendChar != null, objectId));
            if (friendChar != null) {
                friendChar.sendPacket(new SystemMessage(481).addString(_owner.getName()), new L2Friend(_owner, false));
            }
        } else {
            _owner.sendPacket(new SystemMessage(171).addString(name));
        }
    }

    public void notifyFriends(final boolean login) {
        try {
            for (final Friend friend : _friendList.values()) {
                final Player friendPlayer = GameObjectsStorage.getPlayer(friend.getObjectId());
                if (friendPlayer != null) {
                    final Friend thisFriend = friendPlayer.getFriendList().getList().get(_owner.getObjectId());
                    if (thisFriend == null) {
                        continue;
                    }
                    thisFriend.update(_owner, login);
                    if (login) {
                        friendPlayer.sendPacket(new SystemMessage(503).addString(_owner.getName()));
                    }
                    friendPlayer.sendPacket(new L2FriendStatus(_owner, login));
                    friend.update(friendPlayer, login);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addFriend(final Player friendPlayer) {
        _friendList.put(friendPlayer.getObjectId(), new Friend(friendPlayer));
        CharacterFriendDAO.getInstance().insert(_owner, friendPlayer);
    }

    private int removeFriend0(final String name) {
        if (name == null) {
            return 0;
        }
        Integer objectId = 0;
        for (final Entry<Integer, Friend> entry : _friendList.entrySet()) {
            if (name.equalsIgnoreCase(entry.getValue().getName())) {
                objectId = entry.getKey();
                break;
            }
        }
        if (objectId > 0) {
            _friendList.remove(objectId);
            CharacterFriendDAO.getInstance().delete(_owner, objectId);
            return objectId;
        }
        return 0;
    }

    public Map<Integer, Friend> getList() {
        return _friendList;
    }

    @Override
    public String toString() {
        return "FriendList[owner=" + _owner.getName() + "]";
    }
}
