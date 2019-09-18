package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;
import java.util.Objects;

public class Call extends Skill {
    final boolean _party;
    final int _requestWithCrystal;

    public Call(final StatsSet set) {
        super(set);
        _party = set.getBool("party", false);
        _requestWithCrystal = set.getInteger("requestWithCrystal", -1);
    }

    public static SystemMessage canSummonHere(final Player activeChar) {
        if (activeChar.isAlikeDead() || activeChar.isOlyParticipant() || activeChar.isInObserverMode() || activeChar.isFlying() || activeChar.isFestivalParticipant()) {
            return Msg.NOTHING_HAPPENED;
        }
        if (activeChar.isInZoneBattle() || activeChar.isInZone(ZoneType.SIEGE) || activeChar.isInZone(ZoneType.no_restart) || activeChar.isInZone(ZoneType.no_summon) || activeChar.isInBoat() || activeChar.getReflection() != ReflectionManager.DEFAULT || activeChar.isInZone(ZoneType.fun)) {
            return Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
        }
        if (activeChar.isInStoreMode() || activeChar.isProcessingRequest()) {
            return Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;
        }
        return null;
    }

    public static SystemMessage canBeSummoned(final Creature target) {
        if (target == null || !target.isPlayer() || target.isFlying() || target.isInObserverMode() || target.getPlayer().isFestivalParticipant() || !target.getPlayer().getPlayerAccess().UseTeleport) {
            return Msg.INVALID_TARGET;
        }
        if (target.isOlyParticipant()) {
            return Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;
        }
        if (target.isInZoneBattle() || target.isInZone(ZoneType.SIEGE) || target.isInZone(ZoneType.no_restart) || target.isInZone(ZoneType.no_summon) || target.getReflection() != ReflectionManager.DEFAULT || target.isInBoat() || target.isInZone(ZoneType.fun)) {
            return Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
        }
        if (target.isAlikeDead()) {
            return new SystemMessage(1844).addString(target.getName());
        }
        if (target.getPvpFlag() != 0 || target.isInCombat()) {
            return new SystemMessage(1843).addString(target.getName());
        }
        final Player pTarget = (Player) target;
        if (pTarget.getPrivateStoreType() != 0 || pTarget.isProcessingRequest()) {
            return new SystemMessage(1898).addString(target.getName());
        }
        return null;
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (activeChar.isPlayer()) {
            if (_party && ((Player) activeChar).getParty() == null) {
                return false;
            }
            SystemMessage msg = canSummonHere((Player) activeChar);
            if (msg != null) {
                activeChar.sendPacket(msg);
                return false;
            }
            if (!_party) {
                if (activeChar == target) {
                    return false;
                }
                msg = canBeSummoned(target);
                if (msg != null) {
                    activeChar.sendPacket(msg);
                    return false;
                }
            }
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (!activeChar.isPlayer()) {
            return;
        }
        final SystemMessage msg = canSummonHere((Player) activeChar);
        if (msg != null) {
            activeChar.sendPacket(msg);
            return;
        }
        if (_party) {
            if (((Player) activeChar).getParty() != null) {
                ((Player) activeChar).getParty().getPartyMembers().stream().filter(target -> !target.equals(activeChar) && canBeSummoned(target) == null).forEach(target -> {
                    if (_requestWithCrystal >= 0) {
                        target.summonCharacterRequest(activeChar, Location.findPointToStay(activeChar, 100, 150), _requestWithCrystal);
                    } else {
                        target.stopMove();
                        target.teleToLocation(Location.findPointToStay(activeChar, 100, 150), activeChar.getGeoIndex());
                    }
                    getEffects(activeChar, target, getActivateRate() > 0, false);
                });
            }
            if (isSSPossible()) {
                activeChar.unChargeShots(isMagic());
            }
            return;
        }
        targets.stream().filter(Objects::nonNull).filter(target2 -> canBeSummoned(target2) == null).forEach(target2 -> {
            if (_requestWithCrystal >= 0) {
                ((Player) target2).summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), _requestWithCrystal);
            } else {
                target2.stopMove();
                target2.teleToLocation(Location.findPointToStay(activeChar, 100, 150), activeChar.getGeoIndex());
            }
            getEffects(activeChar, target2, getActivateRate() > 0, false);
        });
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
