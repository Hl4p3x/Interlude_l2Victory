package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestExChangeName extends L2GameClientPacket {
    @Override
    protected void readImpl() {
        final int unk1 = readD();
        final String name = readS();
        final int unk2 = readD();
    }

    @Override
    protected void runImpl() {
    }
}
