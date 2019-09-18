package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class Recall extends Skill {
    private final int _townId;
    private final boolean _clanhall;
    private final boolean _castle;
    private final Location _loc;

    public Recall(final StatsSet set) {
        super(set);
        _townId = set.getInteger("townId", 0);
        _clanhall = set.getBool("clanhall", false);
        _castle = set.getBool("castle", false);
        final String[] cords = set.getString("loc", "").split(";");
        if (cords.length == 3) {
            _loc = new Location(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
        } else {
            _loc = null;
        }
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (getHitTime() == 200) {
            final Player player = activeChar.getPlayer();
            if (_clanhall) {
                if (player.getClan() == null || player.getClan().getHasHideout() == 0 || player.getReflection() == ReflectionManager.JAIL) {
                    activeChar.sendPacket(new SystemMessage(113).addItemName(_itemConsumeId[0]));
                    return false;
                }
            } else if (_castle && (player.getClan() == null || player.getClan().getCastle() == 0 || player.getReflection() == ReflectionManager.JAIL)) {
                activeChar.sendPacket(new SystemMessage(113).addItemName(_itemConsumeId[0]));
                return false;
            }
        }
        if (activeChar.isPlayer()) {
            final Player p = (Player) activeChar;
            if (p.getActiveWeaponFlagAttachment() != null) {
                activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
                return false;
            }
            if (p.isInDuel() || p.getTeam() != TeamType.NONE || isPvPEventParticipant(p)) {
                activeChar.sendMessage(new CustomMessage("common.RecallInDuel", p));
                return false;
            }
            if (p.isOlyParticipant()) {
                activeChar.sendPacket(Msg.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
                return false;
            }
        }
        if (activeChar.isInZone(Zone.ZoneType.no_escape) || (_townId > 0 && activeChar.getReflection() != null && activeChar.getReflection().getCoreLoc() != null) || activeChar.getReflection() == ReflectionManager.JAIL) {
            if (activeChar.isPlayer()) {
                activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Recall.Here", (Player) activeChar));
            }
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                final Player pcTarget = target.getPlayer();
                if (pcTarget == null) {
                    continue;
                }
                if (!pcTarget.getPlayerAccess().UseTeleport) {
                    continue;
                }
                if (pcTarget.getActiveWeaponFlagAttachment() != null) {
                    activeChar.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
                } else if (pcTarget.isFestivalParticipant()) {
                    activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Recall.Festival", (Player) activeChar));
                } else {
                    if (pcTarget.isOlyParticipant()) {
                        activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD);
                        return;
                    }
                    if (pcTarget.isInObserverMode()) {
                        activeChar.sendPacket(new SystemMessage(113).addSkillName(getId(), getLevel()));
                        return;
                    }
                    if (pcTarget.isInDuel() || pcTarget.getTeam() != TeamType.NONE) {
                        activeChar.sendMessage(new CustomMessage("common.RecallInDuel", (Player) activeChar));
                        return;
                    }
                    if (_isItemHandler) {
                        if (_itemConsumeId[0] == 7125) {
                            pcTarget.teleToLocation(17144, 170156, -3502, 0);
                            return;
                        }
                        if (_itemConsumeId[0] == 7127) {
                            pcTarget.teleToLocation(105918, 109759, -3207, 0);
                            return;
                        }
                        if (_itemConsumeId[0] == 7130) {
                            pcTarget.teleToLocation(85475, 16087, -3672, 0);
                            return;
                        }
                        if (_itemConsumeId[0] == 7618) {
                            pcTarget.teleToLocation(149864, -81062, -5618, 0);
                            return;
                        }
                        if (_itemConsumeId[0] == 7619) {
                            pcTarget.teleToLocation(108275, -53785, -2524, 0);
                            return;
                        }
                    }
                    if (_loc != null) {
                        pcTarget.teleToLocation(_loc);
                        return;
                    }
                    switch (_townId) {
                        case 1: {
                            pcTarget.teleToLocation(-83990, 243336, -3700, 0);
                            return;
                        }
                        case 2: {
                            pcTarget.teleToLocation(45576, 49412, -2950, 0);
                            return;
                        }
                        case 3: {
                            pcTarget.teleToLocation(12501, 16768, -4500, 0);
                            return;
                        }
                        case 4: {
                            pcTarget.teleToLocation(-44884, -115063, -80, 0);
                            return;
                        }
                        case 5: {
                            pcTarget.teleToLocation(115790, -179146, -890, 0);
                            return;
                        }
                        case 6: {
                            pcTarget.teleToLocation(-14279, 124446, -3000, 0);
                            return;
                        }
                        case 7: {
                            pcTarget.teleToLocation(-82909, 150357, -3000, 0);
                            return;
                        }
                        case 8: {
                            pcTarget.teleToLocation(19025, 145245, -3107, 0);
                            return;
                        }
                        case 9: {
                            pcTarget.teleToLocation(82272, 147801, -3350, 0);
                            return;
                        }
                        case 10: {
                            pcTarget.teleToLocation(82323, 55466, -1480, 0);
                            return;
                        }
                        case 11: {
                            pcTarget.teleToLocation(144526, 24661, -2100, 0);
                            return;
                        }
                        case 12: {
                            pcTarget.teleToLocation(117189, 78952, -2210, 0);
                            return;
                        }
                        case 13: {
                            pcTarget.teleToLocation(110768, 219824, -3624, 0);
                            return;
                        }
                        case 14: {
                            pcTarget.teleToLocation(43536, -50416, -800, 0);
                            return;
                        }
                        case 15: {
                            pcTarget.teleToLocation(148288, -58304, -2979, 0);
                            return;
                        }
                        case 16: {
                            pcTarget.teleToLocation(87776, -140384, -1536, 0);
                            return;
                        }
                        case 17: {
                            pcTarget.teleToLocation(10568, -24600, -3648, 0);
                            return;
                        }
                        case 18: {
                            pcTarget.teleToLocation(19025, 145245, -3107, 0);
                            return;
                        }
                        default: {
                            if (_castle) {
                                pcTarget.teleToCastle();
                                return;
                            }
                            if (_clanhall) {
                                pcTarget.teleToClanhall();
                                return;
                            }
                            pcTarget.teleToClosestTown();
                        }
                    }
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }

    private boolean isPvPEventParticipant(final Player player) {
        return (boolean) Scripts.getInstance().callScripts(player, "events.TvT2.PvPEvent", "isEventPartisipant");
    }
}
