package ai.tower_of_insolence;


import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.instances.NpcInstance;

/**
 * @author Mangol
 */
public class chest_of_kernon extends DefaultAI {
    public chest_of_kernon(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        AddTimerEx(23402, 1000 * 120);
    }

    @Override
    protected void onEvtTimerFiredEx(final int timer_id, final Object arg1, final Object arg2) {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }
        if (timer_id == 23402) {
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
