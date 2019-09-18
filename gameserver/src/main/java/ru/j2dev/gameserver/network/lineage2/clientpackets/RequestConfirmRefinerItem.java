package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.items.IRefineryHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPutIntensiveResultForVariationMake;

public class RequestConfirmRefinerItem extends L2GameClientPacket {
    private int _targetItemObjId;
    private int _refinerItemObjId;

    @Override
    protected void readImpl() {
        _targetItemObjId = readD();
        _refinerItemObjId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
        final ItemInstance mineralItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
        final IRefineryHandler refineryHandler = activeChar.getRefineryHandler();
        if (targetItem == null || mineralItem == null || refineryHandler == null) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutIntensiveResultForVariationMake.FAIL_PACKET);
            return;
        }
        refineryHandler.onPutMineralItem(activeChar, targetItem, mineralItem);
    }
}
