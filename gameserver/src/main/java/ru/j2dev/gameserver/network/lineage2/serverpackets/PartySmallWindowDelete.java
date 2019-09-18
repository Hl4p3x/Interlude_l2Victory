package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class PartySmallWindowDelete extends L2GameServerPacket {
    private final int _objId;
    private final String _name;

    public PartySmallWindowDelete(final Player member) {
        _objId = member.getObjectId();
        _name = member.getName();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x51);
        writeD(_objId);
        writeS(_name);
    }
}
