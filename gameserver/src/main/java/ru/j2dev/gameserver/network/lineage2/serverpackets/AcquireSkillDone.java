package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AcquireSkillDone extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new AcquireSkillDone();

    @Override
    protected void writeImpl() {
        writeC(0x25);
    }
}
