package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PledgeShowMemberListDelete extends L2GameServerPacket {
    private final String _player;

    public PledgeShowMemberListDelete(final String playerName) {
        _player = playerName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x56);
        writeS(_player);
    }
}
