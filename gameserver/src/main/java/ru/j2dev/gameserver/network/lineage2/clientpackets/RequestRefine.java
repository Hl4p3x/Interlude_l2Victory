package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.items.IRefineryHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPutCommissionResultForVariationMake;

public final class RequestRefine extends L2GameClientPacket {
    private static final int GEMSTONE_D = 2130;
    private static final int GEMSTONE_C = 2131;
    private static final int GEMSTONE_B = 2132;
    private int _targetItemObjId;
    private int _refinerItemObjId;
    private int _gemstoneItemObjId;
    private long _gemstoneCount;

    @Override
    protected void readImpl() {
        _targetItemObjId = readD();
        _refinerItemObjId = readD();
        _gemstoneItemObjId = readD();
        _gemstoneCount = readD();
    }

    @Override
    protected void runImpl() {
        if (_gemstoneCount <= 0L) {
            return;
        }
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
        final ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
        final ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
        final IRefineryHandler IRefineryHandler = activeChar.getRefineryHandler();
        if (targetItem == null || refinerItem == null || gemstoneItem == null || IRefineryHandler == null) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutCommissionResultForVariationMake.FAIL_PACKET);
            return;
        }
        IRefineryHandler.onRequestRefine(activeChar, targetItem, refinerItem, gemstoneItem, _gemstoneCount);
    }
}
