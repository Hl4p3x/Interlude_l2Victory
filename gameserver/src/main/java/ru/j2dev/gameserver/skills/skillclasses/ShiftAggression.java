package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class ShiftAggression extends Skill {
    public ShiftAggression(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (activeChar.getPlayer() == null) {
            return;
        }
        for (final Creature target : targets) {
            if (target != null) {
                if (!target.isPlayer()) {
                    continue;
                }
                final Player player = (Player) target;
                for (final NpcInstance npc : World.getAroundNpc(activeChar, getSkillRadius(), getSkillRadius())) {
                    final AggroInfo ai = npc.getAggroList().get(activeChar);
                    if (ai == null) {
                        continue;
                    }
                    npc.getAggroList().addDamageHate(player, 0, ai.hate);
                    npc.getAggroList().remove(activeChar, true);
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
