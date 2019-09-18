package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.ItemInfoCache;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket {
    private int _objectId;

    @Override
    protected void readImpl() {
        _objectId = readD();
    }

    @Override
    protected void runImpl() {
        final ItemInfo item;
        if ((item = ItemInfoCache.getInstance().get(_objectId)) == null) {
            sendPacket(ActionFail.STATIC);
        } else {
            sendPacket(new ExRpItemLink(item));
        }
    }
}
