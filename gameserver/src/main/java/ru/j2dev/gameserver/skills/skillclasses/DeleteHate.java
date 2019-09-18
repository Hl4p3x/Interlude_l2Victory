package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class DeleteHate extends Skill {
    public DeleteHate(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isRaid()) {
                    continue;
                }
                if (getActivateRate() > 0) {
                    if (activeChar.isPlayer() && ((Player) activeChar).isGM()) {
                        activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance", (Player) activeChar).addString(getName()).addNumber(getActivateRate()));
                    }
                    if (!Rnd.chance(getActivateRate())) {
                        return;
                    }
                }
                if (target.isNpc()) {
                    final NpcInstance npc = (NpcInstance) target;
                    npc.getAggroList().clear(false);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                }
                getEffects(activeChar, target, false, false);
            }
        }
    }
}
