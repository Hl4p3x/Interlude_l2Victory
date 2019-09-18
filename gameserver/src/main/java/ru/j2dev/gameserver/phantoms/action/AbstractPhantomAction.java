package ru.j2dev.gameserver.phantoms.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.phantoms.model.Phantom;
import ru.j2dev.gameserver.ThreadPoolManager;

import java.util.concurrent.Future;

public abstract class AbstractPhantomAction implements Runnable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPhantomAction.class);
    protected Phantom actor;

    public abstract long getDelay();

    public Future<?> schedule() {
        return ThreadPoolManager.getInstance().schedule(this, actor.getRndDelay(getDelay()));
    }

    public void setActor(final Phantom phantom) {
        actor = phantom;
    }

}
