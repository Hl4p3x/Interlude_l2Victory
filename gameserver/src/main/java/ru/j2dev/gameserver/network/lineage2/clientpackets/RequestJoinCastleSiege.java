package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.CastleSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.entity.residence.ResidenceType;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.Privilege;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeAttackerList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CastleSiegeDefenderList;

public class RequestJoinCastleSiege extends L2GameClientPacket {
    private int _id;
    private boolean _isAttacker;
    private boolean _isJoining;

    private static void registerAtCastle(final Player player, final Castle castle, final boolean attacker, final boolean join) {
        final CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
        final Clan playerClan = player.getClan();
        if (player.getClan().isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        SiegeClanObject siegeClan;
        if (attacker) {
            siegeClan = siegeEvent.getSiegeClan("attackers", playerClan);
        } else {
            siegeClan = siegeEvent.getSiegeClan("defenders", playerClan);
            if (siegeClan == null) {
                siegeClan = siegeEvent.getSiegeClan("defenders_waiting", playerClan);
            }
        }
        if (join) {
            Residence registeredCastle = null;
            for (final Residence residence : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                SiegeClanObject tempCastle = residence.getSiegeEvent().getSiegeClan("attackers", playerClan);
                if (tempCastle == null) {
                    tempCastle = residence.getSiegeEvent().getSiegeClan("defenders", playerClan);
                }
                if (tempCastle == null) {
                    tempCastle = residence.getSiegeEvent().getSiegeClan("defenders_waiting", playerClan);
                }
                if (tempCastle != null) {
                    registeredCastle = residence;
                }
            }
            if (attacker) {
                if (castle.getOwnerId() == playerClan.getClanId()) {
                    player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
                    return;
                }
                final Alliance alliance = playerClan.getAlliance();
                if (alliance != null) {
                    for (final Clan clan : alliance.getMembers()) {
                        if (clan.getCastle() == castle.getId()) {
                            player.sendPacket(SystemMsg.YOU_CANNOT_REGISTER_AS_AN_ATTACKER_BECAUSE_YOU_ARE_IN_AN_ALLIANCE_WITH_THE_CASTLE_OWNING_CLAN);
                            return;
                        }
                    }
                }
                if (playerClan.getCastle() > 0) {
                    player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
                    return;
                }
                if (siegeClan != null) {
                    player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
                    return;
                }
                if (playerClan.getLevel() < 5) {
                    player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
                    return;
                }
                if (registeredCastle != null) {
                    player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
                    return;
                }
                if (siegeEvent.isRegistrationOver()) {
                    player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
                    return;
                }
                if (castle.getSiegeDate().getTimeInMillis() == 0L) {
                    player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
                    return;
                }
                final int allSize = siegeEvent.getObjects("attackers").size();
                if (allSize >= 20) {
                    player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
                    return;
                }
                siegeClan = new SiegeClanObject("attackers", playerClan, 0L);
                siegeEvent.addObject("attackers", siegeClan);
                SiegeClanDAO.getInstance().insert(castle, siegeClan);
                player.sendPacket(new CastleSiegeAttackerList(castle));
            } else {
                if (castle.getOwnerId() == 0) {
                    return;
                }
                if (castle.getOwnerId() == playerClan.getClanId()) {
                    player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
                    return;
                }
                if (playerClan.getCastle() > 0) {
                    player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
                    return;
                }
                if (siegeClan != null) {
                    player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
                    return;
                }
                if (playerClan.getLevel() < 5) {
                    player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
                    return;
                }
                if (registeredCastle != null) {
                    player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
                    return;
                }
                if (castle.getSiegeDate().getTimeInMillis() == 0L) {
                    player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
                    return;
                }
                if (siegeEvent.isRegistrationOver()) {
                    player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
                    return;
                }
                siegeClan = new SiegeClanObject("defenders_waiting", playerClan, 0L);
                siegeEvent.addObject("defenders_waiting", siegeClan);
                SiegeClanDAO.getInstance().insert(castle, siegeClan);
                player.sendPacket(new CastleSiegeDefenderList(castle));
            }
        } else {
            if (siegeClan == null) {
                siegeClan = siegeEvent.getSiegeClan("defenders_refused", playerClan);
            }
            if (siegeClan == null) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
                return;
            }
            if (siegeEvent.isRegistrationOver()) {
                player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
                return;
            }
            siegeEvent.removeObject(siegeClan.getType(), siegeClan);
            SiegeClanDAO.getInstance().delete(castle, siegeClan);
            if ("attackers".equals(siegeClan.getType())) {
                player.sendPacket(new CastleSiegeAttackerList(castle));
            } else {
                player.sendPacket(new CastleSiegeDefenderList(castle));
            }
        }
    }

    private static void registerAtClanHall(final Player player, final ClanHall clanHall, final boolean join) {
        final ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
        final Clan playerClan = player.getClan();
        SiegeClanObject siegeClan = siegeEvent.getSiegeClan("attackers", playerClan);
        if (join) {
            if (playerClan.getHasHideout() > 0) {
                player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE);
                return;
            }
            if (siegeClan != null) {
                player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
                return;
            }
            if (playerClan.getLevel() < 4) {
                player.sendPacket(SystemMsg.ONLY_CLANS_WHO_ARE_LEVEL_4_OR_ABOVE_CAN_REGISTER_FOR_BATTLE_AT_DEVASTATED_CASTLE_AND_FORTRESS_OF_THE_DEAD);
                return;
            }
            if (siegeEvent.isRegistrationOver()) {
                player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
                return;
            }
            final int allSize = siegeEvent.getObjects("attackers").size();
            if (allSize >= 20) {
                player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
                return;
            }
            siegeClan = new SiegeClanObject("attackers", playerClan, 0L);
            siegeEvent.addObject("attackers", siegeClan);
            SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
        } else {
            if (siegeClan == null) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
                return;
            }
            if (siegeEvent.isRegistrationOver()) {
                player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
                return;
            }
            siegeEvent.removeObject(siegeClan.getType(), siegeClan);
            SiegeClanDAO.getInstance().delete(clanHall, siegeClan);
        }
        player.sendPacket(new CastleSiegeAttackerList(clanHall));
    }

    @Override
    protected void readImpl() {
        _id = readD();
        _isAttacker = (readD() == 1);
        _isJoining = (readD() == 1);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR)) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        final Residence residence = ResidenceHolder.getInstance().getResidence(_id);
        if (residence.getType() == ResidenceType.Castle) {
            registerAtCastle(player, (Castle) residence, _isAttacker, _isJoining);
        } else if (residence.getType() == ResidenceType.ClanHall && _isAttacker) {
            registerAtClanHall(player, (ClanHall) residence, _isJoining);
        }
    }
}
