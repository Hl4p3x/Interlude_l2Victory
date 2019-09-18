package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestSendMsnChatLog extends L2GameClientPacket {
    private int unk3;
    private String unk;
    private String unk2;

    @Override
    protected void runImpl() {
    }

    @Override
    protected void readImpl() {
        unk = readS();
        unk2 = readS();
        unk3 = readD();
    }
}
