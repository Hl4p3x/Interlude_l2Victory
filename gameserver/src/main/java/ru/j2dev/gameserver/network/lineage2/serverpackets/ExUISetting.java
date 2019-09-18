package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExUISetting extends L2GameServerPacket {
    private final byte[] data;

    public ExUISetting(final Player player) {
        data = player.getKeyBindings();
    }

    @Override
    protected void writeImpl() {
        writeEx(0x70);
        writeD(data.length);
        writeB(data);
    }
}
