package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.items.IRefineryHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPutItemResultForVariationMake;

public class RequestConfirmTargetItem extends L2GameClientPacket {
    private int _itemObjId;

    @Override
    protected void readImpl() {
        _itemObjId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
        final IRefineryHandler refineryHandler = activeChar.getRefineryHandler();
        if (item == null || refineryHandler == null) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutItemResultForVariationMake.FAIL_PACKET);
            return;
        }
        refineryHandler.onPutTargetItem(activeChar, item);
    }
}
