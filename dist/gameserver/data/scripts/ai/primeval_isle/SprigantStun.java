package ai.primeval_isle;

import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.tables.SkillTable;

public class SprigantStun extends DefaultAI {
    private static final int TICK_IN_MILISECONDS = 15000;
    private final Skill SKILL;
    private long _waitTime;

    public SprigantStun(final NpcInstance actor) {
        super(actor);
        SKILL = SkillTable.getInstance().getInfo(5085, 1);
    }

    @Override
    protected boolean thinkActive() {
        if (System.currentTimeMillis() > _waitTime) {
            final NpcInstance actor = getActor();
            actor.doCast(SKILL, actor, false);
            _waitTime = System.currentTimeMillis() + 15000L;
            return true;
        }
        return false;
    }
}
