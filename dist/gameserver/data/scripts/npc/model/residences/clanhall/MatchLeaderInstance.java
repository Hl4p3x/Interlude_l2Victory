package npc.model.residences.clanhall;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class MatchLeaderInstance extends MatchBerserkerInstance {
    public MatchLeaderInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void reduceCurrentHp(double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker.isPlayer()) {
            damage = damage / getMaxHp() / 0.05 * 100.0;
        } else {
            damage = damage / getMaxHp() / 0.05 * 10.0;
        }
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }
}
