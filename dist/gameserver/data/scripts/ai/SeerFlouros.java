package ai;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

public class SeerFlouros extends Mystic {
    private static final int MOB = 18560;
    private static final int MOBS_COUNT = 2;
    private static final int[] _hps = {80, 60, 40, 30, 20, 10, 5, -5};

    private int _hpCount;

    public SeerFlouros(final NpcInstance actor) {
        super(actor);
        _hpCount = 0;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (!actor.isDead() && actor.getCurrentHpPercents() < SeerFlouros._hps[_hpCount]) {
            spawnMobs(attacker);
            ++_hpCount;
        }
        super.onEvtAttacked(attacker, damage);
    }

    private void spawnMobs(final Creature attacker) {
        final NpcInstance actor = getActor();
        for (int i = 0; i < 2; ++i) {
            try {
                final SimpleSpawner sp = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(18560));
                sp.setLoc(Location.findPointToStay(actor, 100, 120));
                sp.setReflection(actor.getReflection());
                final NpcInstance npc = sp.doSpawn(true);
                npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        _hpCount = 0;
        super.onEvtDead(killer);
    }
}
