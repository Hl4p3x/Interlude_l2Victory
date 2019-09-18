package ru.j2dev.gameserver.network.lineage2.clientpackets;

class SuperCmdCharacterInfo extends L2GameClientPacket {
    private String _characterName;

    @Override
    protected void readImpl() {
        _characterName = readS();
    }

    @Override
    protected void runImpl() {
    }
}
