package ru.j2dev.gameserver.manager;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMessageStack {
    private final Map<Integer, List<L2GameServerPacket>> _stack;

    public PlayerMessageStack() {
        _stack = new HashMap<>();
    }

    public static PlayerMessageStack getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void mailto(final int char_obj_id, final L2GameServerPacket message) {
        final Player cha = GameObjectsStorage.getPlayer(char_obj_id);
        if (cha != null) {
            cha.sendPacket(message);
            return;
        }
        synchronized (_stack) {
            List<L2GameServerPacket> messages;
            if (_stack.containsKey(char_obj_id)) {
                messages = _stack.remove(char_obj_id);
            } else {
                messages = new ArrayList<>();
            }
            messages.add(message);
            _stack.put(char_obj_id, messages);
        }
    }

    public void CheckMessages(final Player cha) {
        List<L2GameServerPacket> messages;
        synchronized (_stack) {
            if (!_stack.containsKey(cha.getObjectId())) {
                return;
            }
            messages = _stack.remove(cha.getObjectId());
        }
        if (messages == null || messages.size() == 0) {
            return;
        }
        messages.forEach(cha::sendPacket);
    }

    private static class LazyHolder {
        private static final PlayerMessageStack INSTANCE = new PlayerMessageStack();
    }
}
