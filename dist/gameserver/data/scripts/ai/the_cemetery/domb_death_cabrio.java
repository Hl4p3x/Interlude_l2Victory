package ai.the_cemetery;


import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.NpcUtils;

/**
 * @author Mangol
 */
public class domb_death_cabrio extends Fighter {
    private final int coffer_of_the_dead = 31027;

    public domb_death_cabrio(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        NpcUtils.spawnSingle(coffer_of_the_dead, getActor().getX(), getActor().getY(), getActor().getZ());
        super.onEvtDead(killer);
    }
}
