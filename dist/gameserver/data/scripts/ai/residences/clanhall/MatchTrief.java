package ai.residences.clanhall;

import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.tables.SkillTable;

public class MatchTrief extends MatchFighter {
    public static final Skill HOLD = SkillTable.getInstance().getInfo(4047, 6);

    public MatchTrief(final NpcInstance actor) {
        super(actor);
    }

    public void hold() {
        final NpcInstance actor = getActor();
        addTaskCast(actor, MatchTrief.HOLD);
        doTask();
    }
}
