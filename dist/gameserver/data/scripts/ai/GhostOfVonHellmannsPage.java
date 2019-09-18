package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

public class GhostOfVonHellmannsPage extends DefaultAI {
    static final Location[] points = {new Location(51462, -54539, -3176), new Location(51870, -54398, -3176), new Location(52164, -53964, -3176), new Location(52390, -53282, -3176), new Location(52058, -52071, -3104), new Location(52237, -51483, -3112), new Location(52024, -51262, -3096)};
    static final String[] NPCtext = {"Follow me...", "This where that here...", "I want to speak to you..."};

    private int current_point;
    private long wait_timeout;
    private boolean wait;

    public GhostOfVonHellmannsPage(final NpcInstance actor) {
        super(actor);
        current_point = -1;
        wait_timeout = 0L;
        wait = false;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (_def_think) {
            doTask();
            return true;
        }
        if (System.currentTimeMillis() <= wait_timeout || (current_point <= -1 && !Rnd.chance(5))) {
            return randomAnimation();
        }
        if (!wait) {
            switch (current_point) {
                case 6: {
                    wait_timeout = System.currentTimeMillis() + 60000L;
                    return wait = true;
                }
            }
        }
        wait_timeout = 0L;
        wait = false;
        ++current_point;
        if (current_point >= GhostOfVonHellmannsPage.points.length) {
            actor.deleteMe();
            return false;
        }
        addTaskMove(GhostOfVonHellmannsPage.points[current_point], true);
        doTask();
        return true;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }
}
