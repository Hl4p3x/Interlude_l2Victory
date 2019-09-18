package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestSEKCustom extends L2GameClientPacket {
    private int SlotNum;
    private int Direction;

    @Override
    protected void readImpl() {
        SlotNum = readD();
        Direction = readD();
    }

    @Override
    protected void runImpl() {
    }
}
