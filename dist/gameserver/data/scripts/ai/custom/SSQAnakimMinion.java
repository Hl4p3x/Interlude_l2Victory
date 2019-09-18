package ai.custom;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.List;

public class SSQAnakimMinion extends Fighter {
    private final int[] _enemies;

    public SSQAnakimMinion(final NpcInstance actor) {
        super(actor);
        _enemies = new int[]{32717, 32716};
        actor.setHasChatWindow(false);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        ThreadPoolManager.getInstance().schedule(new Attack(), 3000L);
    }

    private NpcInstance getEnemy() {
        final List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
        if (around != null && !around.isEmpty()) {
            for (final NpcInstance npc : around) {
                if (ArrayUtils.contains(_enemies, npc.getNpcId())) {
                    return npc;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    public class Attack extends RunnableImpl {
        @Override
        public void runImpl() {
            if (getEnemy() != null) {
                getActor().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEnemy(), 10000000);
            }
        }
    }
}
