package ru.j2dev.gameserver.network.lineage2.clientpackets;

class SuperCmdSummonCmd extends L2GameClientPacket {
    private String _summonName;

    @Override
    protected void readImpl() {
        _summonName = readS();
    }

    @Override
    protected void runImpl() {
    }
}
