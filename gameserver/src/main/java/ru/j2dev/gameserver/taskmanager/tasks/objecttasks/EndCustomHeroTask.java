package ru.j2dev.gameserver.taskmanager.tasks.objecttasks;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;

/**
 * Created by JunkyFunky
 * on 09.03.2018 11:52
 * group j2dev
 */
public class EndCustomHeroTask extends RunnableImpl {
    private final HardReference<Player> _playerRef;

    public EndCustomHeroTask(final Player player) {
        _playerRef = player.getRef();
    }

    @Override
    public void runImpl() {
        final Player player = _playerRef.get();
        if (player == null) {
            return;
        }
        if (player.getVar("CustomHeroEndTime") == null || HeroManager.getInstance().isCurrentHero(player)) {
            return;
        }
        player.setHero(false);
        HeroManager.removeSkills(player);
        player.broadcastUserInfo(true);
    }
}
