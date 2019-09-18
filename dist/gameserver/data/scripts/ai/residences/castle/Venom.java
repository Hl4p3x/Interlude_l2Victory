package ai.residences.castle;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.NpcUtils;

public class Venom extends Fighter {
    public Venom(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public void onEvtSpawn() {
        super.onEvtSpawn();
        Functions.npcShout(getActor(), "Who dares to covet the throne of our castle!  Leave immediately or you will pay the price of your audacity with your very own blood!");
    }

    @Override
    public void onEvtDead(final Creature killer) {
        super.onEvtDead(killer);
        Functions.npcShout(getActor(), "It's not over yet...  It won't be... over... like this... Never...");
        NpcUtils.spawnSingle(29055, 12589, -49044, -3008, 120000L);
    }
}
