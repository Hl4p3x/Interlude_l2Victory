package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket {
    private int _event;

    public TutorialEnableClientEvent(final int event) {
        _event = 0;
        _event = event;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xa2);
        writeD(_event);
    }
}
