package ai;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;

public class SealDevice extends Fighter {
    private boolean _firstAttack;

    public SealDevice(final NpcInstance actor) {
        super(actor);
        _firstAttack = false;
        actor.block();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (!_firstAttack) {
            actor.broadcastPacket(new MagicSkillUse(actor, actor, 5980, 1, 0, 0L));
            _firstAttack = true;
        }
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }
}
