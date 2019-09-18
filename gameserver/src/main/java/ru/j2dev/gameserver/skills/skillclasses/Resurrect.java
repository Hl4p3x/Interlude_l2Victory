package ru.j2dev.gameserver.skills.skillclasses;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.BaseStats;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Resurrect extends Skill {
    private final boolean _canPet;

    public Resurrect(final StatsSet set) {
        super(set);
        _canPet = set.getBool("canPet", false);
    }

    private boolean siegeCheck(final Player player, final Creature target, final boolean forceUse) {
        boolean result = true;
        for (final GlobalEvent e : player.getEvents()) {
            if (!e.canResurrect(player, target, forceUse)) {
                result = false;
            }
        }
        if (result) {
            final SiegeEvent playerEvent = player.getEvent(SiegeEvent.class);
            final SiegeEvent targetEvent = target.getEvent(SiegeEvent.class);
            final boolean playerInZone = player.isInZone(ZoneType.SIEGE);
            final boolean targetInZone = target.isInZone(ZoneType.SIEGE);
            if ((playerEvent == null && playerInZone) || (targetEvent == null && targetInZone)) {
                result = false;
            }
        }
        if (!result) {
            player.sendPacket((new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
            return false;
        }
        return true;
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (!activeChar.isPlayer()) {
            return false;
        }
        if (target == null || (target != activeChar && !target.isDead())) {
            activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        final Player player = (Player) activeChar;
        final Player pcTarget = target.getPlayer();
        if (pcTarget == null) {
            player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        if (player.isOlyParticipant() || pcTarget.isOlyParticipant()) {
            player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        if (!siegeCheck(player, target, forceUse)) {
            return false;
        }
        if (oneTarget()) {
            if (target.isPet()) {
                final Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
                final ReviveAnswerListener reviveAsk = (ask != null && ask.getValue() instanceof ReviveAnswerListener) ? ((ReviveAnswerListener) ask.getValue()) : null;
                if (reviveAsk != null) {
                    if (reviveAsk.isForPet()) {
                        activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
                    } else {
                        activeChar.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
                    }
                    return false;
                }
                if (!_canPet && _targetType != SkillTargetType.TARGET_PET) {
                    player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
                    return false;
                }
            } else if (target.isPlayer()) {
                final Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
                final ReviveAnswerListener reviveAsk = (ask != null && ask.getValue() instanceof ReviveAnswerListener) ? ((ReviveAnswerListener) ask.getValue()) : null;
                if (reviveAsk != null) {
                    if (reviveAsk.isForPet()) {
                        activeChar.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
                    } else {
                        activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
                    }
                    return false;
                }
                if (_targetType == SkillTargetType.TARGET_PET) {
                    player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
                    return false;
                }
                if (pcTarget.isFestivalParticipant()) {
                    player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Resurrect", player));
                    return false;
                }
            }
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        double percent = _power;
        if (percent < 100.0 && !isHandler()) {
            final double wit_bonus = _power * (BaseStats.WIT.calcBonus(activeChar) - 1.0);
            percent += ((wit_bonus > 20.0) ? 20.0 : wit_bonus);
            if (percent > 90.0) {
                percent = 90.0;
            }
        }
        for (final Creature target : targets) {
            if (target == null) {
                continue;
            }
            if (target.getPlayer() == null) {
                continue;
            }
            for (final GlobalEvent e : target.getEvents()) {
                if (!e.canResurrect((Player) activeChar, target, true)) {
                    continue;
                }
            }
            if (target.isPet() && _canPet) {
                if (target.getPlayer() == activeChar) {
                    ((PetInstance) target).doRevive(percent);
                } else {
                    target.getPlayer().reviveRequest((Player) activeChar, percent, true);
                }
            } else {
                if (!target.isPlayer()) {
                    continue;
                }
                if (_targetType == SkillTargetType.TARGET_PET) {
                    continue;
                }
                final Player targetPlayer = (Player) target;
                final Pair<Integer, OnAnswerListener> ask = targetPlayer.getAskListener(false);
                final ReviveAnswerListener reviveAsk = (ask != null && ask.getValue() instanceof ReviveAnswerListener) ? ((ReviveAnswerListener) ask.getValue()) : null;
                if (reviveAsk != null) {
                    continue;
                }
                if (targetPlayer.isFestivalParticipant()) {
                    continue;
                }
                targetPlayer.reviveRequest((Player) activeChar, percent, false);
            }
            getEffects(activeChar, target, getActivateRate() > 0, false);
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
