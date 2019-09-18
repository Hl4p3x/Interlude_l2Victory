package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class DeleteHateOfMe extends Skill {
    public DeleteHateOfMe(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (activeChar.isPlayer() && ((Player) activeChar).isGM()) {
                    activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance", (Player) activeChar).addString(getName()).addNumber(getActivateRate()));
                }
                if (target.isNpc() && Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())) {
                    final NpcInstance npc = (NpcInstance) target;
                    npc.getAggroList().remove(activeChar, true);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                }
                getEffects(activeChar, target, true, false);
            }
        }
    }
}
