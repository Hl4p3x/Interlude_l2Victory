package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class CaughtFighter extends Fighter {
    private static final int TIME_TO_LIVE = 60000;
    private final long TIME_TO_DIE;

    public CaughtFighter(final NpcInstance actor) {
        super(actor);
        TIME_TO_DIE = System.currentTimeMillis() + 60000L;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        if (Rnd.chance(75)) {
            Functions.npcSayCustomMessage(getActor(), "scripts.ai.CaughtMob.spawn");
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        if (Rnd.chance(75)) {
            Functions.npcSayCustomMessage(getActor(), "scripts.ai.CaughtMob.death");
        }
        super.onEvtDead(killer);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor != null && System.currentTimeMillis() >= TIME_TO_DIE) {
            actor.deleteMe();
            return false;
        }
        return super.thinkActive();
    }
}
