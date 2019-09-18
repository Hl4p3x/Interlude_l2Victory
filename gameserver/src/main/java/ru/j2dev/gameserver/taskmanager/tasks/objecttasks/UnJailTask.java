package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:50
 * group j2dev
 */
public class UnJailTask extends RunnableImpl {
    private final HardReference<Player> _playerRef;

    public UnJailTask(final Player player) {
        _playerRef = player.getRef();
    }

    @Override
    public void runImpl() {
        final Player player = _playerRef.get();
        if (player == null) {
            return;
        }
        player.unblock();
        player.standUp();
        player.teleToLocation(17817, 170079, -3530, ReflectionManager.DEFAULT);
    }
}
