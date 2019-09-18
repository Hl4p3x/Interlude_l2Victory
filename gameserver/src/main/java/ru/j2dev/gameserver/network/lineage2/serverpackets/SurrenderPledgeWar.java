package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;
    private final String _char;

    public SurrenderPledgeWar(final String pledge, final String charName) {
        _pledgeName = pledge;
        _char = charName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x69);
        writeS(_pledgeName);
        writeS(_char);
    }
}
