package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.ZoneObject;
import ru.j2dev.gameserver.model.instances.residences.SiegeFlagInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncMul;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class SummonSiegeFlag extends Skill {
    private final FlagType _flagType;
    private final double _advancedMult;

    public SummonSiegeFlag(final StatsSet set) {
        super(set);
        _flagType = set.getEnum("flagType", FlagType.class);
        _advancedMult = set.getDouble("advancedMultiplier", 1.0);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (!activeChar.isPlayer()) {
            return false;
        }
        if (!super.checkCondition(activeChar, target, forceUse, dontMove, first)) {
            return false;
        }
        final Player player = (Player) activeChar;
        if (player.getClan() == null || !player.isClanLeader()) {
            return false;
        }
        switch (_flagType) {
            case OUTPOST:
            case NORMAL:
            case ADVANCED: {
                if (player.isInZone(ZoneType.RESIDENCE)) {
                    player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, (new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
                    return false;
                }
                final SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
                if (siegeEvent == null) {
                    player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, (new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
                    return false;
                }
                boolean inZone = false;
                final List<ZoneObject> zones = siegeEvent.getObjects("flag_zones");
                for (final ZoneObject zone : zones) {
                    if (player.isInZone(zone.getZone())) {
                        inZone = true;
                    }
                }
                if (!inZone) {
                    player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, ((SysMsgContainer) new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
                    return false;
                }
                final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", player.getClan());
                if (siegeClan == null) {
                    player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, (new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
                    return false;
                }
                if (siegeClan.getFlag() != null) {
                    player.sendPacket(SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, (new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)).addSkillName(this));
                    return false;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final Player player = (Player) activeChar;
        final Clan clan = player.getClan();
        if (clan == null || !player.isClanLeader()) {
            return;
        }
        final SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
        if (siegeEvent == null) {
            return;
        }
        final SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", clan);
        if (siegeClan == null) {
            return;
        }
        switch (_flagType) {
            case DESTROY: {
                siegeClan.deleteFlag();
                break;
            }
            default: {
                if (siegeClan.getFlag() != null) {
                    return;
                }
                final SiegeFlagInstance flag = (SiegeFlagInstance) NpcTemplateHolder.getInstance().getTemplate((_flagType == FlagType.OUTPOST) ? 36590 : 35062).getNewInstance();
                flag.setClan(siegeClan);
                flag.addEvent(siegeEvent);
                if (_flagType == FlagType.ADVANCED) {
                    flag.addStatFunc(new FuncMul(Stats.MAX_HP, 80, flag, _advancedMult));
                }
                flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
                flag.setHeading(player.getHeading());
                final int x = (int) (player.getX() + 100.0 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
                final int y = (int) (player.getY() + 100.0 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
                flag.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex()));
                siegeClan.setFlag(flag);
                break;
            }
        }
    }

    public enum FlagType {
        DESTROY,
        NORMAL,
        ADVANCED,
        OUTPOST
    }
}
