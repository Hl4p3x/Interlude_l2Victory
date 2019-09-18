package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class PetitionVote extends L2GameClientPacket {
    private int _type;
    private int _unk1;
    private String _petitionText;

    @Override
    protected void runImpl() {
    }

    @Override
    protected void readImpl() {
        _type = readD();
        _unk1 = readD();
        _petitionText = readS(4096);
    }
}
