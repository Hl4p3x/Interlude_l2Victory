package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestTimeCheck extends L2GameClientPacket {
    private int unk;
    private int unk2;

    @Override
    protected void readImpl() {
        unk = readD();
        unk2 = readD();
    }

    @Override
    protected void runImpl() {
    }
}
