package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:49
 * group j2dev
 */
public class PvPFlagTask extends RunnableImpl {
    private final HardReference<Player> _playerRef;

    public PvPFlagTask(final Player player) {
        _playerRef = player.getRef();
    }

    @Override
    public void runImpl() {
        final Player player = _playerRef.get();
        if (player == null) {
            return;
        }
        final long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
        if (diff > Config.PVP_TIME + Config.PVP_BLINKING_UNFLAG_TIME) {
            player.stopPvPFlag();
        } else if (diff > Config.PVP_TIME) {
            player.updatePvPFlag(2);
        } else {
            player.updatePvPFlag(1);
        }
    }
}
