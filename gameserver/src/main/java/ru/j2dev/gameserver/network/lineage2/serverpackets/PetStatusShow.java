package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Summon;

public class PetStatusShow extends L2GameServerPacket {
    private final int _summonType;

    public PetStatusShow(final Summon summon) {
        _summonType = summon.getSummonType();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb0);
        writeD(_summonType);
    }
}
