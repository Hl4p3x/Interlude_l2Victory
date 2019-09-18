package ai.tower_of_insolence;


import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.instances.NpcInstance;

/**
 * @author Mangol
 */
public class chest_of_hallate extends DefaultAI {
    public chest_of_hallate(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        AddTimerEx(23404, 1000 * 120);
    }

    @Override
    protected void onEvtTimerFiredEx(final int timer_id, final Object arg1, final Object arg2) {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }
        if (timer_id == 23404) {
            actor.deleteMe();
        } else {
            super.onEvtTimerFiredEx(timer_id, arg1, arg2);
        }
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}
