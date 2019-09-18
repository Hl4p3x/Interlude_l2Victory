package ai.residences.clanhall;

import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.tables.SkillTable;

public class MatchCleric extends MatchFighter {
    public static final Skill HEAL = SkillTable.getInstance().getInfo(4056, 6);

    public MatchCleric(final NpcInstance actor) {
        super(actor);
    }

    public void heal() {
        final NpcInstance actor = getActor();
        addTaskCast(actor, MatchCleric.HEAL);
        doTask();
    }
}
