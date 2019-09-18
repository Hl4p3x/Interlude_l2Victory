package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class StartPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;
    private final String _char;

    public StartPledgeWar(final String pledge, final String charName) {
        _pledgeName = pledge;
        _char = charName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x65);
        writeS(_char);
        writeS(_pledgeName);
    }
}
