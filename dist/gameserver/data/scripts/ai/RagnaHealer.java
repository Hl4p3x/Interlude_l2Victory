package ai;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Priest;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.List;

public class RagnaHealer extends Priest {
    private long lastFactionNotifyTime;

    public RagnaHealer(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }
        if (System.currentTimeMillis() - lastFactionNotifyTime > 10000L) {
            lastFactionNotifyTime = System.currentTimeMillis();
            final List<NpcInstance> around = actor.getAroundNpc(500, 300);
            if (around != null && !around.isEmpty()) {
                for (final NpcInstance npc : around) {
                    if (npc.isMonster() && npc.getNpcId() >= 22691 && npc.getNpcId() <= 22702) {
                        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
                    }
                }
            }
        }
        super.onEvtAttacked(attacker, damage);
    }
}
