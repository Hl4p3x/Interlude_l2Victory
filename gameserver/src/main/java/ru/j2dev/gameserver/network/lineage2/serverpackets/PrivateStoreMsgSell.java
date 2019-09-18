package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.model.Player;

public class PrivateStoreMsgSell extends L2GameServerPacket {
    private final int _objId;
    private final String _name;

    public PrivateStoreMsgSell(final Player player) {
        _objId = player.getObjectId();
        _name = StringUtils.defaultString(player.getSellStoreName());
    }

    @Override
    protected final void writeImpl() {
        writeC(0x9c);
        writeD(_objId);
        writeS(_name);
    }
}
