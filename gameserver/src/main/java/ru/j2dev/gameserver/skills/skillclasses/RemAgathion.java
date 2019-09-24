package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class RemAgathion extends Skill {
    public RemAgathion(StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(Creature caster, List<Creature> p1) {
        final Player activeChar = caster.getPlayer();

        activeChar.getAgathion().doDespawn();
    }
}
