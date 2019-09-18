package ai.the_enchanted_valley;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

/**
 * @author Mangol
 */
public class guardian_of_tree extends Fighter {
    public guardian_of_tree(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtTimerFiredEx(final int timer_id, final Object arg1, final Object arg2) {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }
        if (timer_id == 42101) {
            actor.deleteMe();
        } else {
            super.onEvtTimerFiredEx(timer_id, arg1, arg2);
        }
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        final NpcInstance actor = getActor();
        int i0 = actor.getParam2();
        final int i1 = actor.getParam3();
        final Creature arg = actor.getParam4();
        if (i0 > 0) {
            AddTimerEx(42101, 1000 * 300);
            if (arg != null && actor.getDistance(arg) <= 1500) {
                if (i1 == -1) {
                    actor.doCast(getSkillInfo(4243, 1), arg, true);
                }
                actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, arg, 2000);
            }
        } else {
            i0 = Rnd.get(3);
            switch (i0) {
                case 0:
                    actor.MakeFString(42118, ChatType.ALL);
                    break;
                case 1:
                    actor.MakeFString(42119, ChatType.ALL);
                    break;
                default:
                    actor.MakeFString(42120, ChatType.ALL);
                    break;
            }
        }
    }
}
