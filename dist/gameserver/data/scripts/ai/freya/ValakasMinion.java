package ai.freya;

import bosses.ValakasManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class ValakasMinion extends Mystic {
    public ValakasMinion(final NpcInstance actor) {
        super(actor);
        actor.startImmobilized();
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        ValakasManager.getZone().getInsidePlayers().forEach(player -> notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 5000));
    }
}
