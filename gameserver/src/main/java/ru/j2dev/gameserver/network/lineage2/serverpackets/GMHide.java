package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class GMHide extends L2GameServerPacket {
    private final int obj_id;

    public GMHide(final int id) {
        obj_id = id;
    }

    @Override
    protected void writeImpl() {
        writeC(0x93);
        writeD(obj_id);
    }
}
