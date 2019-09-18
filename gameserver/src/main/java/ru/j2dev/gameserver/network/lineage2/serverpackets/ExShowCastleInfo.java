package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExShowCastleInfo extends L2GameServerPacket {
    private List<CastleInfo> _infos;

    public ExShowCastleInfo() {
        _infos = Collections.emptyList();
        final List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        _infos = new ArrayList<>(castles.size());
        castles.forEach(castle -> {
            final String ownerName = ClanTable.getInstance().getClanName(castle.getOwnerId());
            final int id = castle.getId();
            final int tax = castle.getTaxPercent();
            final int nextSiege = (int) (castle.getSiegeDate().getTimeInMillis() / 1000L);
            _infos.add(new CastleInfo(ownerName, id, tax, nextSiege));
        });
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x14);
        writeD(_infos.size());
        _infos.forEach(info -> {
            writeD(info._id);
            writeS(info._ownerName);
            writeD(info._tax);
            writeD(info._nextSiege);
        });
        _infos.clear();
    }

    private static class CastleInfo {
        public final String _ownerName;
        public final int _id;
        public final int _tax;
        public final int _nextSiege;

        public CastleInfo(final String ownerName, final int id, final int tax, final int nextSiege) {
            _ownerName = ownerName;
            _id = id;
            _tax = tax;
            _nextSiege = nextSiege;
        }
    }
}
