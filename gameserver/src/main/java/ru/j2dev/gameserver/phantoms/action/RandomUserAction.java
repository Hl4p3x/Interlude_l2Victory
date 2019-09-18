package ru.j2dev.gameserver.phantoms.action;

import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.network.lineage2.clientpackets.RequestActionUse;

public class RandomUserAction extends AbstractPhantomAction {
    @Override
    public long getDelay() {
        return 0L;
    }

    @Override
    public void run() {
        final int actionId = PhantomConfig.userActions[Rnd.get(PhantomConfig.userActions.length)];
        final RequestActionUse.Action actionOptional = RequestActionUse.Action.find(actionId);
    }
}
