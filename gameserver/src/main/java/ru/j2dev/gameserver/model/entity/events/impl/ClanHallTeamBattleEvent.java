package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.dao.SiegePlayerDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.CTBTeamObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class ClanHallTeamBattleEvent extends SiegeEvent<ClanHall, CTBSiegeClanObject> {
    public static final String TRYOUT_PART = "tryout_part";
    public static final String CHALLENGER_RESTART_POINTS = "challenger_restart_points";
    public static final String FIRST_DOORS = "first_doors";
    public static final String SECOND_DOORS = "second_doors";
    public static final String NEXT_STEP = "next_step";

    public ClanHallTeamBattleEvent(final MultiValueSet<String> set) {
        super(set);
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        final List<CTBSiegeClanObject> attackers = getObjects(ATTACKERS);
        if (attackers.isEmpty()) {
            if (_oldOwner == null) {
                broadcastInZone2((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST)).addResidenceName(getResidence()));
            } else {
                broadcastInZone2(new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
            }
            reCalcNextTime(false);
            return;
        }
        if (_oldOwner != null) {
            addObject(DEFENDERS, new SiegeClanObject(DEFENDERS, _oldOwner, 0L));
        }
        SiegeClanDAO.getInstance().delete(getResidence());
        SiegePlayerDAO.getInstance().delete(getResidence());
        final List<CTBTeamObject> teams = getObjects(TRYOUT_PART);
        for (int i = 0; i < 5; ++i) {
            final CTBTeamObject team = teams.get(i);
            CTBSiegeClanObject siegeClanId = null;
            try {
                siegeClanId = attackers.get(i);
            } catch (Exception ignored) {
            }
            team.setSiegeClan(siegeClanId);
        }
        broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), ATTACKERS, DEFENDERS);
        broadcastTo(SystemMsg.THE_TRYOUTS_ARE_ABOUT_TO_BEGIN, ATTACKERS);
        super.startEvent();
    }

    public void nextStep() {
        broadcastTo(SystemMsg.THE_TRYOUTS_HAVE_BEGUN, ATTACKERS, DEFENDERS);
        updateParticles(true, ATTACKERS, DEFENDERS);
    }

    public void processStep(final CTBTeamObject team) {
        if (team.getSiegeClan() != null) {
            final CTBSiegeClanObject object = team.getSiegeClan();
            object.setEvent(false, this);
            teleportPlayers(SPECTATORS);
        }
        team.despawnObject(this);
        final List<CTBTeamObject> teams = getObjects(TRYOUT_PART);
        boolean hasWinner = false;
        CTBTeamObject winnerTeam = null;
        for (final CTBTeamObject t : teams) {
            if (t.isParticle()) {
                hasWinner = (winnerTeam == null);
                winnerTeam = t;
            }
        }
        if (!hasWinner) {
            return;
        }
        final SiegeClanObject clan = winnerTeam.getSiegeClan();
        if (clan != null) {
            getResidence().changeOwner(clan.getClan());
        }
        stopEvent(true);
    }

    @Override
    public void announce(final int val) {
        final int minute = val / 60;
        if (minute > 0) {
            broadcastTo(new SystemMessage2(SystemMsg.THE_CONTEST_WILL_BEGIN_IN_S1_MINUTES).addInteger(minute), ATTACKERS, DEFENDERS);
        } else {
            broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS).addInteger(val), ATTACKERS, DEFENDERS);
        }
    }

    @Override
    public void stopEvent(final boolean step) {
        final Clan newOwner = getResidence().getOwner();
        if (newOwner != null) {
            if (_oldOwner != newOwner) {
                newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
                newOwner.incReputation(1700, false, toString());
            }
            broadcastTo(new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()), ATTACKERS, DEFENDERS);
            broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), ATTACKERS, DEFENDERS);
        } else {
            broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), ATTACKERS);
        }
        updateParticles(false, ATTACKERS, DEFENDERS);
        removeObjects(DEFENDERS);
        removeObjects(ATTACKERS);
        super.stopEvent(step);
        _oldOwner = null;
    }

    @Override
    public void loadSiegeClans() {
        final List<SiegeClanObject> siegeClanObjectList = SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS);
        addObjects(ATTACKERS, siegeClanObjectList);
        final List<CTBSiegeClanObject> objects = getObjects(ATTACKERS);
        objects.forEach(clan -> clan.select(getResidence()));
    }

    @Override
    public CTBSiegeClanObject newSiegeClan(final String type, final int clanId, final long i, final long date) {
        final Clan clan = ClanTable.getInstance().getClan(clanId);
        return (clan == null) ? null : new CTBSiegeClanObject(type, clan, i, date);
    }

    @Override
    public boolean isParticle(final Player player) {
        if (!isInProgress() || player.getClan() == null) {
            return false;
        }
        final CTBSiegeClanObject object = getSiegeClan(ATTACKERS, player.getClan());
        return object != null && object.getPlayers().contains(player.getObjectId());
    }

    @Override
    public Location getRestartLoc(final Player player, final RestartType type) {
        if (!checkIfInZone(player)) {
            return null;
        }
        final SiegeClanObject attackerClan = getSiegeClan(ATTACKERS, player.getClan());
        Location loc = null;
        switch (type) {
            case TO_VILLAGE: {
                if (attackerClan != null && checkIfInZone(player)) {
                    final List<SiegeClanObject> objectList = getObjects(ATTACKERS);
                    final List<Location> teleportList = getObjects(CHALLENGER_RESTART_POINTS);
                    final int index = objectList.indexOf(attackerClan);
                    loc = teleportList.get(index);
                    break;
                }
                break;
            }
        }
        return loc;
    }

    @Override
    public void action(final String name, final boolean start) {
        if (NEXT_STEP.equalsIgnoreCase(name)) {
            nextStep();
        } else {
            super.action(name, start);
        }
    }

    @Override
    public int getUserRelation(final Player thisPlayer, final int result) {
        return result;
    }

    @Override
    public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result) {
        return result;
    }
}
