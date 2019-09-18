package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.tables.SkillTable;

public class OnlySkillCaster extends Mystic {

    private static final Skill skillToCast = SkillTable.getInstance().getInfo(4100, 1);
    private static final double chanceToCast = 99.9;

    public OnlySkillCaster(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean createNewTask() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return false;
        }

        clearTasks();

        final Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }

        if (Rnd.chance(chanceToCast) && !actor.isCastingNow() && actor.isInRange(target, skillToCast.getCastRange())) {
            addTaskCast(target, skillToCast);
        }


        return true;
    }

    @Override
    protected boolean randomWalk() {
        return true;
    }

    @Override
    protected boolean randomAnimation() {
        return true;
    }

    @Override
    public boolean canSeeInSilentMove(final Playable target) {
        return true;
    }

    @Override
    public boolean canSeeInHide(final Playable target) {
        return true;
    }
}
