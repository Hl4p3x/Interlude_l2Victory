package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.ManorManagerInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.manor.SeedProduction;

public class RequestBuySeed extends L2GameClientPacket {
    private int _count;
    private int _manorId;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _manorId = readD();
        _count = readD();
        if (_count * 8 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _itemQ[i] = readD();
            if (_itemQ[i] < 1L) {
                _count = 0;
                return;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        final GameObject target = activeChar.getTarget();
        final ManorManagerInstance manor = (target instanceof ManorManagerInstance) ? ((ManorManagerInstance) target) : null;
        if (!activeChar.isGM() && (manor == null || !manor.isInActingRange(activeChar))) {
            activeChar.sendActionFailed();
            return;
        }
        final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
        if (castle == null) {
            return;
        }
        long totalPrice = 0L;
        int slots = 0;
        long weight = 0L;
        try {
            for (int i = 0; i < _count; ++i) {
                final int seedId = _items[i];
                final long count = _itemQ[i];
                long price;
                long residual;
                final SeedProduction seed = castle.getSeed(seedId, 0);
                price = seed.getPrice();
                residual = seed.getCanProduce();
                if (price < 1L) {
                    return;
                }
                if (residual < count) {
                    return;
                }
                totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));
                final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(seedId);
                if (item == null) {
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getWeight()));
                if (!item.isStackable() || activeChar.getInventory().getItemByItemId(seedId) == null) {
                    ++slots;
                }
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }
        activeChar.getInventory().writeLock();
        try {
            if (!activeChar.getInventory().validateWeight(weight)) {
                sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!activeChar.getInventory().validateCapacity(slots)) {
                sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            if (!activeChar.reduceAdena(totalPrice, true)) {
                sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            castle.addToTreasuryNoTax(totalPrice, false, true);
            for (int i = 0; i < _count; ++i) {
                final int seedId = _items[i];
                final long count = _itemQ[i];
                final SeedProduction seed2 = castle.getSeed(seedId, 0);
                seed2.setCanProduce(seed2.getCanProduce() - count);
                castle.updateSeed(seed2.getId(), seed2.getCanProduce(), 0);
                activeChar.getInventory().addItem(seedId, count);
                activeChar.sendPacket(SystemMessage2.obtainItems(seedId, count, 0));
            }
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        activeChar.sendChanges();
    }
}
