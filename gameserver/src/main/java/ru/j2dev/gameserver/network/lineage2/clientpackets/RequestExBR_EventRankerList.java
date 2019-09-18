package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestExBR_EventRankerList extends L2GameClientPacket {
    private int unk;
    private int unk2;
    private int unk3;

    @Override
    protected void readImpl() {
        unk = readD();
        unk2 = readD();
        unk3 = readD();
    }

    @Override
    protected void runImpl() {
    }
}
