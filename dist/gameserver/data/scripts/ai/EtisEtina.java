package ai;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

public class EtisEtina extends Fighter {
    private boolean summonsReleased;
    private NpcInstance summon1;
    private NpcInstance summon2;

    public EtisEtina(final NpcInstance actor) {
        super(actor);
        summonsReleased = false;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (actor.getCurrentHpPercents() < 70.0 && !summonsReleased) {
            summonsReleased = true;
            summon1 = NpcUtils.spawnSingle(18950, Location.findAroundPosition(actor, 150), actor.getReflection());
            summon2 = NpcUtils.spawnSingle(18951, Location.findAroundPosition(actor, 150), actor.getReflection());
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        if (summon1 != null && !summon1.isDead()) {
            summon1.decayMe();
        }
        if (summon2 != null && !summon2.isDead()) {
            summon2.decayMe();
        }
        super.onEvtDead(killer);
    }
}
