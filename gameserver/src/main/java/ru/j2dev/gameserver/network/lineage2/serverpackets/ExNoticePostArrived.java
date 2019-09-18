package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExNoticePostArrived extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC_TRUE = new ExNoticePostArrived(1);
    public static final L2GameServerPacket STATIC_FALSE = new ExNoticePostArrived(0);

    private int _anim;

    public ExNoticePostArrived(final int useAnim) {
        _anim = useAnim;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xa9);
        writeD(_anim);
    }
}
