package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:49
 * group j2dev
 */
public class WaterTask extends RunnableImpl {
    private final HardReference<Player> _playerRef;

    public WaterTask(final Player player) {
        _playerRef = player.getRef();
    }

    @Override
    public void runImpl() {
        final Player player = _playerRef.get();
        if (player == null) {
            return;
        }
        if (player.isDead() || !player.isInWater()) {
            player.stopWaterTask();
            return;
        }
        final double reduceHp = (player.getMaxHp() < 100) ? 1.0 : (player.getMaxHp() / 100);
        player.reduceCurrentHp(reduceHp, player, null, false, false, true, false, false, false, false);
        player.sendPacket(new SystemMessage(297).addNumber((long) reduceHp));
    }
}
