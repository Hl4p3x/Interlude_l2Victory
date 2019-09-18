package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExReplyWritePost extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC_TRUE = new ExReplyWritePost(1);
    public static final L2GameServerPacket STATIC_FALSE = new ExReplyWritePost(0);

    private int _reply;

    public ExReplyWritePost(final int i) {
        _reply = i;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xb4);
        writeD(_reply);
    }
}
