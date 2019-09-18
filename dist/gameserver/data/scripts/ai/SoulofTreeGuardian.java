package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class SoulofTreeGuardian extends Mystic {
    private boolean _firstTimeAttacked;

    public SoulofTreeGuardian(final NpcInstance actor) {
        super(actor);
        _firstTimeAttacked = true;
    }

    @Override
    protected void onEvtSpawn() {
        _firstTimeAttacked = true;
        super.onEvtSpawn();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (_firstTimeAttacked) {
            _firstTimeAttacked = false;
            if (Rnd.chance(10)) {
                Functions.npcSayInRangeCustomMessage(actor, 500, "scripts.ai.SoulofTreeGuardian.WE_MUST_PROTECT");
            }
        } else if (Rnd.chance(10)) {
            Functions.npcSayInRangeCustomMessage(actor, 500, "scripts.ai.SoulofTreeGuardian.GET_OUT");
        }
    }
}
