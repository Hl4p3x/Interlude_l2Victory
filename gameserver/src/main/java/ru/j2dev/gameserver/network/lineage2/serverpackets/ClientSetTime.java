package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new ClientSetTime();

    @Override
    protected final void writeImpl() {
        writeC(0xec);
        writeD(GameTimeController.getInstance().getGameTime());
        writeD(6);
    }
}
