package ai.custom;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class MutantChest extends Fighter {
    public MutantChest(final NpcInstance actor) {
        super(actor);
        actor.startImmobilized();
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        if (Rnd.chance(30)) {
            Functions.npcSay(actor, "\u0412\u0440\u0430\u0433\u0438! \u0412\u0441\u044e\u0434\u0443 \u0432\u0440\u0430\u0433\u0438! \u0412\u0441\u0435 \u0441\u044e\u0434\u0430, \u0432\u0440\u0430\u0433\u0438 \u0437\u0434\u0435\u0441\u044c!");
        }
        actor.deleteMe();
    }
}
