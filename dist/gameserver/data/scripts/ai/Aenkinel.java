package ai;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Aenkinel extends Fighter {
    public Aenkinel(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        if (actor.getNpcId() == 25694 || actor.getNpcId() == 25695) {
            final Reflection ref = actor.getReflection();
            ref.setReenterTime(System.currentTimeMillis());
        }
        if (actor.getNpcId() == 25694) {
            for (int i = 0; i < 4; ++i) {
                actor.getReflection().addSpawnWithoutRespawn(18820, actor.getLoc(), 250);
            }
        } else if (actor.getNpcId() == 25695) {
            for (int i = 0; i < 4; ++i) {
                actor.getReflection().addSpawnWithoutRespawn(18823, actor.getLoc(), 250);
            }
        }
        super.onEvtDead(killer);
    }
}
