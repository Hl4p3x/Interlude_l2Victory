package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestPrivateStoreList extends L2GameClientPacket {
    private int unk;

    @Override
    protected void readImpl() {
        unk = readD();
    }

    @Override
    protected void runImpl() {
    }
}
