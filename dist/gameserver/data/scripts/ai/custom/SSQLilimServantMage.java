package ai.custom;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class SSQLilimServantMage extends Mystic {
    private boolean _attacked;

    public SSQLilimServantMage(final NpcInstance actor) {
        super(actor);
        _attacked = false;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        super.onEvtAttacked(attacker, damage);
        if (Rnd.chance(30) && !_attacked) {
            Functions.npcSay(getActor(), "Who dares enter this place?");
            _attacked = true;
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        if (Rnd.chance(30)) {
            Functions.npcSay(getActor(), "Lord Shilen... some day... you will accomplish... this mission...");
        }
        super.onEvtDead(killer);
    }
}
