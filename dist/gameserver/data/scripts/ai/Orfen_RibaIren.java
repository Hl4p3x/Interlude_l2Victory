package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class Orfen_RibaIren extends Fighter {
    private static final int Orfen_id = 29014;

    public Orfen_RibaIren(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean createNewTask() {
        return defaultNewTask();
    }

    @Override
    protected void onEvtClanAttacked(final Creature attacked_member, final Creature attacker, final int damage) {
        super.onEvtClanAttacked(attacked_member, attacker, damage);
        final NpcInstance actor = getActor();
        if (_healSkills.length == 0) {
            return;
        }
        if (attacked_member.isDead() || actor.isDead() || attacked_member.getCurrentHpPercents() > 50.0) {
            return;
        }
        int heal_chance;
        if (attacked_member.getNpcId() == actor.getNpcId()) {
            heal_chance = ((attacked_member.getObjectId() == actor.getObjectId()) ? 100 : 0);
        } else {
            heal_chance = ((attacked_member.getNpcId() == 29014) ? 90 : 10);
        }
        if (Rnd.chance(heal_chance) && canUseSkill(_healSkills[0], attacked_member, -1.0)) {
            addTaskAttack(attacked_member, _healSkills[0], 1000000);
        }
    }
}
