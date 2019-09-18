package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class StopPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;
    private final String _char;

    public StopPledgeWar(final String pledge, final String charName) {
        _pledgeName = pledge;
        _char = charName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x67);
        writeS(_pledgeName);
        writeS(_char);
    }
}
