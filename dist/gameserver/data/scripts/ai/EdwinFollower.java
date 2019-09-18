package ai;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class EdwinFollower extends DefaultAI {
    private static final int EDWIN_ID = 32072;
    private static final int DRIFT_DISTANCE = 200;
    private long _wait_timeout;
    private HardReference<? extends Creature> _edwinRef;

    public EdwinFollower(final NpcInstance actor) {
        super(actor);
        _wait_timeout = 0L;
        _edwinRef = HardReferences.emptyRef();
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean randomAnimation() {
        return false;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        final Creature edwin = _edwinRef.get();
        if (edwin == null) {
            if (System.currentTimeMillis() > _wait_timeout) {
                _wait_timeout = System.currentTimeMillis() + 15000L;
                for (final NpcInstance npc : World.getAroundNpc(actor)) {
                    if (npc.getNpcId() == 32072) {
                        _edwinRef = npc.getRef();
                        return true;
                    }
                }
            }
        } else if (!actor.isMoving()) {
            final int x = edwin.getX() + Rnd.get(400) - 200;
            final int y = edwin.getY() + Rnd.get(400) - 200;
            final int z = edwin.getZ();
            actor.setRunning();
            actor.moveToLocation(x, y, z, 0, true);
            return true;
        }
        return false;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }
}
