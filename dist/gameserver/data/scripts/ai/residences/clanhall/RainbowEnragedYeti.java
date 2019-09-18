package ai.residences.clanhall;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class RainbowEnragedYeti extends Fighter {
    public RainbowEnragedYeti(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public void onEvtSpawn() {
        super.onEvtSpawn();
        Functions.npcShoutCustomMessage(getActor(), "clanhall.RainbowEnragedYeti.OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING");
    }
}
