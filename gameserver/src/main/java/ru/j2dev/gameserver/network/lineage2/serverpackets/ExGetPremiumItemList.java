package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.PremiumItem;

import java.util.Map;

public class ExGetPremiumItemList extends L2GameServerPacket {
    private final int _objectId;
    private final Map<Integer, PremiumItem> _list;

    public ExGetPremiumItemList(final Player activeChar) {
        _objectId = activeChar.getObjectId();
        _list = activeChar.getPremiumItemList();
    }

    @Override
    protected void writeImpl() {
        writeEx(0x86);
        if (!_list.isEmpty()) {
            writeD(_list.size());
            _list.forEach((key, value) -> {
                writeD(key);
                writeD(_objectId);
                writeD(value.getItemId());
                writeQ(value.getCount());
                writeD(0);
                writeS(value.getSender());
            });
        }
    }
}
