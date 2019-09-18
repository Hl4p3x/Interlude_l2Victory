package ai;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class WitchWarder extends Fighter {
    private static final int DESPAWN_TIME = 180000;
    private long _wait_timeout;
    private boolean _wait;

    public WitchWarder(final NpcInstance actor) {
        super(actor);
        _wait_timeout = 0L;
        _wait = false;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (_def_think) {
            doTask();
            _wait = false;
            return true;
        }
        if (!_wait) {
            _wait = true;
            _wait_timeout = System.currentTimeMillis() + 180000L;
        }
        if (_wait_timeout != 0L && _wait && _wait_timeout < System.currentTimeMillis()) {
            actor.deleteMe();
        }
        return super.thinkActive();
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}
