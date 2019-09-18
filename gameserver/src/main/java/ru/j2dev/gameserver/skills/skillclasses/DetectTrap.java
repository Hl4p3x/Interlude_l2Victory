package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.TrapInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcInfo;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class DetectTrap extends Skill {
    public DetectTrap(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null && target.isTrap()) {
                final TrapInstance trap = (TrapInstance) target;
                if (trap.getLevel() > getPower()) {
                    continue;
                }
                trap.setDetected(true);
                for (final Player player : World.getAroundPlayers(trap)) {
                    player.sendPacket(new NpcInfo(trap, player));
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
