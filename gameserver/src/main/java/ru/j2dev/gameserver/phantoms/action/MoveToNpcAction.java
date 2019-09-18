package ru.j2dev.gameserver.phantoms.action;

import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.List;

public class MoveToNpcAction extends AbstractPhantomAction {
    @Override
    public long getDelay() {
        return 0L;
    }

    @Override
    public void run() {
        final List<NpcInstance> npcList = actor.getAroundNpc(PhantomConfig.moveToNpcRange, 200);
        if (npcList.size() == 0) {
            return;
        }
        actor.moveToLocation(Rnd.get(npcList).getLoc(), 100, true);
    }
}
