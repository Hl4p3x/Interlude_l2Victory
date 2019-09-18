package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class KashasEye extends DefaultAI {
    public KashasEye(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        super.onEvtDead(killer);
        final NpcInstance actor = getActor();
        if (actor != null && killer != null && actor != killer && Rnd.chance(35)) {
            actor.setDisplayId(Rnd.get(18812, 18814));
        }
    }
}
