package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.Collections;
import java.util.List;

public class CastleSiegeAttackerList extends L2GameServerPacket {
    private final int _id;
    private final int _registrationValid;
    private List<SiegeClanObject> _clans;

    public CastleSiegeAttackerList(final Residence residence) {
        _clans = Collections.emptyList();
        _id = residence.getId();
        _registrationValid = (residence.getSiegeEvent().isRegistrationOver() ? 0 : 1);
        _clans = residence.getSiegeEvent().getObjects("attackers");
    }

    @Override
    protected final void writeImpl() {
        writeC(0xca);
        writeD(_id);
        writeD(0x0);
        writeD(_registrationValid);
        writeD(0x0);
        writeD(_clans.size());
        writeD(_clans.size());
        _clans.forEach(siegeClan -> {
            final Clan clan = siegeClan.getClan();
            writeD(clan.getClanId());
            writeS(clan.getName());
            writeS(clan.getLeaderName());
            writeD(clan.getCrestId());
            writeD((int) (siegeClan.getDate() / 1000L));
            final Alliance alliance = clan.getAlliance();
            writeD(clan.getAllyId());
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
}
