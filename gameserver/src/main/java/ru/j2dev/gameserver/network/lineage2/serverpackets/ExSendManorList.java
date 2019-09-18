package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.entity.residence.Castle;

import java.util.Collection;

public class ExSendManorList extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x1b);
        final Collection<Castle> residences = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        writeD(residences.size());
        residences.forEach(castle -> {
            writeD(castle.getId());
            writeS(castle.getName().toLowerCase());
        });
    }
}
