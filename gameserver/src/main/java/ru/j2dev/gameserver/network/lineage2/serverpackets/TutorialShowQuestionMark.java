package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket {
    private final int _number;

    public TutorialShowQuestionMark(final int number) {
        _number = number;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xa1);
        writeD(_number);
    }
}
