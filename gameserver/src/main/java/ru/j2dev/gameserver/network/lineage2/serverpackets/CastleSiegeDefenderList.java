package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastleSiegeDefenderList extends L2GameServerPacket {
    public static int OWNER = 1;
    public static int WAITING = 2;
    public static int ACCEPTED = 3;
    public static int REFUSE = 4;

    private int _id;
    private int _registrationValid;
    private List<DefenderClan> _defenderClans;

    public CastleSiegeDefenderList(final Castle castle) {
        _defenderClans = Collections.emptyList();
        _id = castle.getId();
        _registrationValid = ((!castle.getSiegeEvent().isRegistrationOver() && castle.getOwner() != null) ? 1 : 0);
        final List<SiegeClanObject> defenders = castle.getSiegeEvent().getObjects("defenders");
        final List<SiegeClanObject> defendersWaiting = castle.getSiegeEvent().getObjects("defenders_waiting");
        final List<SiegeClanObject> defendersRefused = castle.getSiegeEvent().getObjects("defenders_refused");
        _defenderClans = new ArrayList<>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
        if (castle.getOwner() != null) {
            _defenderClans.add(new DefenderClan(castle.getOwner(), OWNER, 0));
        }
        for (final SiegeClanObject siegeClan : defenders) {
            _defenderClans.add(new DefenderClan(siegeClan.getClan(), ACCEPTED, (int) (siegeClan.getDate() / 1000L)));
        }
        for (final SiegeClanObject siegeClan : defendersWaiting) {
            _defenderClans.add(new DefenderClan(siegeClan.getClan(), WAITING, (int) (siegeClan.getDate() / 1000L)));
        }
        for (final SiegeClanObject siegeClan : defendersRefused) {
            _defenderClans.add(new DefenderClan(siegeClan.getClan(), REFUSE, (int) (siegeClan.getDate() / 1000L)));
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(203);
        writeD(_id);
        writeD(0);
        writeD(_registrationValid);
        writeD(0);
        writeD(_defenderClans.size());
        writeD(_defenderClans.size());
        _defenderClans.forEach(defenderClan -> {
            final Clan clan = defenderClan._clan;
            writeD(clan.getClanId());
            writeS(clan.getName());
            writeS(clan.getLeaderName());
            writeD(clan.getCrestId());
            writeD(defenderClan._time);
            writeD(defenderClan._type);
            writeD(clan.getAllyId());
            final Alliance alliance = clan.getAlliance();
            if (alliance != null) {
                writeS(alliance.getAllyName());
                writeS(alliance.getAllyLeaderName());
                writeD(alliance.getAllyCrestId());
            } else {
                writeS("");
                writeS("");
                writeD(0);
            }
        });
    }

    private static class DefenderClan {
        private final Clan _clan;
        private final int _type;
        private final int _time;

        public DefenderClan(final Clan clan, final int type, final int time) {
            _clan = clan;
            _type = type;
            _time = time;
        }
    }
}
