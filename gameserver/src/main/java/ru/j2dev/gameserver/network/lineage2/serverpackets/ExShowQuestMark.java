package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket {
    private final int _questId;

    public ExShowQuestMark(final int questId) {
        _questId = questId;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x1a);
        writeD(_questId);
    }
}
