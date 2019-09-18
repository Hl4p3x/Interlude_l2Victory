package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.ManorManagerInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.Collections;
import java.util.List;

public class RequestProcureCrop extends L2GameClientPacket {
    private int _manorId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;
    private List<CropProcure> _procureList;

    public RequestProcureCrop() {
        _procureList = Collections.emptyList();
    }

    @Override
    protected void readImpl() {
        _manorId = readD();
        _count = readD();
        if (_count * 16 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            readD();
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
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        final GameObject target = activeChar.getTarget();
        final ManorManagerInstance manor = (target instanceof ManorManagerInstance) ? ((ManorManagerInstance) target) : null;
        if (!activeChar.isGM() && (!activeChar.isInActingRange(manor))) {
            activeChar.sendActionFailed();
            return;
        }
        final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
        if (castle == null) {
            return;
        }
        int slots = 0;
        long weight = 0L;
        try {
            for (int i = 0; i < _count; ++i) {
                final int itemId = _items[i];
                final long count = _itemQ[i];
                final CropProcure crop = castle.getCrop(itemId, 0);
                if (crop == null) {
                    return;
                }
                final int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
                long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));
                rewradItemCount = SafeMath.mulAndCheck(count, rewradItemCount);
                final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(rewradItemId);
                if (template == null) {
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
                if (!template.isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null) {
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
            _procureList = castle.getCropProcure(0);
            for (int i = 0; i < _count; ++i) {
                final int itemId = _items[i];
                final long count = _itemQ[i];
                final int rewradItemId2 = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
                long rewradItemCount2 = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));
                rewradItemCount2 = SafeMath.mulAndCheck(count, rewradItemCount2);
                if (activeChar.getInventory().destroyItemByItemId(itemId, count)) {
                    final ItemInstance item = activeChar.getInventory().addItem(rewradItemId2, rewradItemCount2);
                    if (item != null) {
                        activeChar.sendPacket(SystemMessage2.obtainItems(rewradItemId2, rewradItemCount2, 0));
                    }
                }
            }
        } catch (ArithmeticException ae) {
            _count = 0;
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        activeChar.sendChanges();
    }
}
