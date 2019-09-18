package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new TutorialCloseHtml();

    @Override
    protected final void writeImpl() {
        writeC(0xa3);
    }
}
