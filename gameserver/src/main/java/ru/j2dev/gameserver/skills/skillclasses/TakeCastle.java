package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class TakeCastle extends Skill {
    public TakeCastle(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (!super.checkCondition(activeChar, target, forceUse, dontMove, first)) {
            return false;
        }
        if (activeChar == null || !activeChar.isPlayer()) {
            return false;
        }
        final Player player = (Player) activeChar;
        if (player.getClan() == null || !player.isClanLeader()) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }
        final CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
        if (siegeEvent == null) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }
        if (siegeEvent.getSiegeClan("attackers", player.getClan()) == null) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }
        if (player.isMounted()) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }
        if (!player.isInRangeZ(target, 185L)) {
            player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
            return false;
        }
        if (!siegeEvent.getResidence().getZone().checkIfInZone(activeChar) || !siegeEvent.getResidence().getZone().checkIfInZone(target)) {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
            return false;
        }
        if (first) {
            siegeEvent.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, "defenders");
        }
        return true;
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (!target.isArtefact()) {
                    continue;
                }
                final Player player = (Player) activeChar;
                final CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
                if (siegeEvent == null) {
                    continue;
                }
                siegeEvent.broadcastTo(new SystemMessage2(SystemMsg.CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT).addString(player.getClan().getName()), "attackers", "defenders");
                siegeEvent.processStep(player.getClan());
            }
        }
        getEffects(activeChar, activeChar, getActivateRate() > 0, false);
    }
}
