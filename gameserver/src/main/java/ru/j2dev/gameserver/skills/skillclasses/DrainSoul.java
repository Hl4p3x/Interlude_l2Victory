package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class DrainSoul extends Skill {
    public DrainSoul(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (target == null || !target.isMonster() || target.isDead()) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (!activeChar.isPlayer() || activeChar.isDead()) {
            return;
        }
        for (final Creature c : targets) {
            if (c == null || c.isDead() || !c.isMonster()) {
                return;
            }
            final MonsterInstance monster = (MonsterInstance) c;
            if (monster.getTemplate().getAbsorbInfo().isEmpty()) {
                continue;
            }
            monster.addAbsorber(activeChar.getPlayer());
        }
    }
}
