package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class JoinParty extends L2GameServerPacket {
    public static final L2GameServerPacket SUCCESS = new JoinParty(1);
    public static final L2GameServerPacket FAIL = new JoinParty(0);

    private int _response;

    public JoinParty(final int response) {
        _response = response;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x3a);
        writeD(_response);
    }
}
