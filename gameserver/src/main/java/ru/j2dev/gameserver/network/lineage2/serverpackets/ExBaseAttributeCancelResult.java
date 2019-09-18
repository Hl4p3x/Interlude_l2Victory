package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;

public class ExBaseAttributeCancelResult extends L2GameServerPacket {
    private final boolean _result;
    private final int _objectId;
    private final Element _element;

    public ExBaseAttributeCancelResult(final boolean result, final ItemInstance item, final Element element) {
        _result = result;
        _objectId = item.getObjectId();
        _element = element;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x75);
        writeD(_result);
        writeD(_objectId);
        writeD(_element.getId());
    }
}
