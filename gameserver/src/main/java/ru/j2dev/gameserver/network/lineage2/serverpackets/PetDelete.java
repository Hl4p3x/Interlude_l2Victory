package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PetDelete extends L2GameServerPacket {
    private final int _petId;
    private final int _petnum;

    public PetDelete(final int petId, final int petnum) {
        _petId = petId;
        _petnum = petnum;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb6);
        writeD(_petId);
        writeD(_petnum);
    }
}
