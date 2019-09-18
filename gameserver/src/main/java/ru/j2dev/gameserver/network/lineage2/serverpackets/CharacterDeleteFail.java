package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class CharacterDeleteFail extends L2GameServerPacket {
    public static int REASON_DELETION_FAILED = 1;
    public static int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
    public static int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;

    private int _error;

    public CharacterDeleteFail(final int error) {
        _error = error;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x24);
        writeD(_error);
    }
}
