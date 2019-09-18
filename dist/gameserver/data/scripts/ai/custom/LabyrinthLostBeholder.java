package ai.custom;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.ReflectionBossInstance;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncSet;

public class LabyrinthLostBeholder extends Fighter {
    public LabyrinthLostBeholder(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        final Reflection r = actor.getReflection();
        if (!r.isDefault() && checkMates(actor.getNpcId()) && findLostCaptain() != null) {
            findLostCaptain().addStatFunc(new FuncSet(Stats.MAGIC_DEFENCE, 48, this, findLostCaptain().getTemplate().getBaseMDef() * 0.66));
        }
        super.onEvtDead(killer);
    }

    private boolean checkMates(final int id) {
        return getActor().getReflection().getNpcs().stream().noneMatch(n -> n.getNpcId() == id && !n.isDead());
    }

    private NpcInstance findLostCaptain() {
        return getActor().getReflection().getNpcs().stream().filter(n -> n instanceof ReflectionBossInstance).findFirst().orElse(null);
    }
}
