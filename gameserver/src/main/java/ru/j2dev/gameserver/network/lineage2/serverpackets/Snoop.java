package ru.j2dev.gameserver.network.lineage2.serverpackets;

import java.util.Arrays;

public class Snoop extends L2GameServerPacket {
    private final int _convoID;
    private final String _name;
    private final int _type;
    private final int _fStringId;
    private final String _speaker;
    private final String[] _params;

    public Snoop(final int id, final String name, final int type, final String speaker, final String msg, final int fStringId, final String... params) {
        _convoID = id;
        _name = name;
        _type = type;
        _speaker = speaker;
        _fStringId = fStringId;
        _params = params;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd5);
        writeD(_convoID);
        writeS(_name);
        writeD(0);
        writeD(_type);
        writeS(_speaker);
        Arrays.stream(_params).forEach(this::writeS);
    }
}
