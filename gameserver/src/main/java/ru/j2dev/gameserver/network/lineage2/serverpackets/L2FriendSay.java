package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class L2FriendSay extends L2GameServerPacket {
    private final String _sender;
    private final String _receiver;
    private final String _message;

    public L2FriendSay(final String sender, final String reciever, final String message) {
        _sender = sender;
        _receiver = reciever;
        _message = message;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xfd);
        writeD(0);
        writeS(_receiver);
        writeS(_sender);
        writeS(_message);
    }
}
