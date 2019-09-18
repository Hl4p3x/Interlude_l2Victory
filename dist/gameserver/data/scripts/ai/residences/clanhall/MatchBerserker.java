package ai.residences.clanhall;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.tables.SkillTable;

public class MatchBerserker extends MatchFighter {
    public static final Skill ATTACK_SKILL = SkillTable.getInstance().getInfo(4032, 6);

    public MatchBerserker(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public void onEvtAttacked(final Creature attacker, final int dam) {
        super.onEvtAttacked(attacker, dam);
        if (Rnd.chance(10)) {
            addTaskCast(attacker, MatchBerserker.ATTACK_SKILL);
        }
    }
}
