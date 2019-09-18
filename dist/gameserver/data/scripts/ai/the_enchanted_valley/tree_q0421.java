package ai.the_enchanted_valley;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.NpcUtils;

/**
 * @author KilRoy
 * @author Mangol
 */
public class tree_q0421 extends Fighter {
    private static final int guardian_of_tree = 27189;

    public tree_q0421(final NpcInstance actor) {
        super(actor);
        actor.block();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (actor != null && attacker != null) {
            if (attacker.isPlayer()) {
                if (actor.getCurrentHpPercents() <= 67 && Rnd.chance(29)) {
                    actor.doCast(getSkillInfo(4243, 1), attacker, true);
                }
            } else if (attacker.isPet()) {
                super.onEvtAttacked(attacker, damage);
            }
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance npc = getActor();
        if (npc.getDistance(killer) <= 1500) {
            NpcUtils.spawnSingle(guardian_of_tree, getActor().getX(), getActor().getY(), getActor().getZ(), killer, 0, 1, -1);
            for (int i = 1; i <= 19; i++) {
                NpcUtils.spawnSingle(guardian_of_tree, getActor().getX(), getActor().getY(), getActor().getZ(), killer, 0, 1, 0);
            }
        }
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
    }

    @Override
    protected boolean randomAnimation() {
        return false;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}