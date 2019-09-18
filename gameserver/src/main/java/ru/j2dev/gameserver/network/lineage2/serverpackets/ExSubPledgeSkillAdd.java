package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExSubPledgeSkillAdd extends L2GameServerPacket {
    private final int _type;
    private final int _id;
    private final int _level;

    public ExSubPledgeSkillAdd(final int type, final int id, final int level) {
        _type = type;
        _id = id;
        _level = level;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x76);
        writeD(_type);
        writeD(_id);
        writeD(_level);
    }
}
