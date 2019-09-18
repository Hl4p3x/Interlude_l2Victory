package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.commons.util.Rnd;
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
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.manor.CropProcure;

public class RequestProcureCropList extends L2GameClientPacket {
    private int _count;
    private int[] _items;
    private int[] _crop;
    private int[] _manor;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _count = readD();
        if (_count * 16 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _crop = new int[_count];
        _manor = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _crop[i] = readD();
            _manor[i] = readD();
            _itemQ[i] = readD();
            if (_crop[i] < 1 || _manor[i] < 1 || _itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
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
        if (!activeChar.isGM() && (manor == null || !manor.isInActingRange(activeChar))) {
            activeChar.sendActionFailed();
            return;
        }
        final int currentManorId = (manor == null) ? 0 : manor.getCastle().getId();
        long totalFee = 0L;
        int slots = 0;
        long weight = 0L;
        try {
            for (int i = 0; i < _count; ++i) {
                final int objId = _items[i];
                final int cropId = _crop[i];
                final int manorId = _manor[i];
                final long count = _itemQ[i];
                final ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
                if (item == null || item.getCount() < count || item.getItemId() != cropId) {
                    return;
                }
                final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
                if (castle == null) {
                    return;
                }
                final CropProcure crop = castle.getCrop(cropId, 0);
                if (crop == null || crop.getId() == 0 || crop.getPrice() == 0L) {
                    return;
                }
                if (count > crop.getAmount()) {
                    return;
                }
                final long price = SafeMath.mulAndCheck(count, crop.getPrice());
                long fee = 0L;
                if (currentManorId != 0 && manorId != currentManorId) {
                    fee = price * 5L / 100L;
                }
                totalFee = SafeMath.addAndCheck(totalFee, fee);
                final int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());
                final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(rewardItemId);
                if (template == null) {
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, template.getWeight()));
                if (!template.isStackable() || activeChar.getInventory().getItemByItemId(cropId) == null) {
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
            if (activeChar.getInventory().getAdena() < totalFee) {
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            for (int i = 0; i < _count; ++i) {
                final int objId = _items[i];
                final int cropId = _crop[i];
                final int manorId = _manor[i];
                final long count = _itemQ[i];
                final ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
                if (item != null && item.getCount() >= count) {
                    if (item.getItemId() == cropId) {
                        final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
                        if (castle != null) {
                            final CropProcure crop = castle.getCrop(cropId, 0);
                            if (crop != null && crop.getId() != 0) {
                                if (crop.getPrice() != 0L) {
                                    if (count <= crop.getAmount()) {
                                        final int rewardItemId2 = Manor.getInstance().getRewardItem(cropId, crop.getReward());
                                        final long sellPrice = count * crop.getPrice();
                                        final long rewardPrice = ItemTemplateHolder.getInstance().getTemplate(rewardItemId2).getReferencePrice();
                                        if (rewardPrice != 0L) {
                                            final double reward = sellPrice / rewardPrice;
                                            final long rewardItemCount = (long) reward + ((Rnd.nextDouble() <= reward % 1.0) ? 1 : 0);
                                            if (rewardItemCount < 1L) {
                                                final SystemMessage sm = new SystemMessage(1491);
                                                sm.addItemName(cropId);
                                                sm.addNumber(count);
                                                activeChar.sendPacket(sm);
                                            } else {
                                                long fee2 = 0L;
                                                if (currentManorId != 0 && manorId != currentManorId) {
                                                    fee2 = sellPrice * 5L / 100L;
                                                }
                                                if (activeChar.getInventory().destroyItemByObjectId(objId, count)) {
                                                    if (!activeChar.reduceAdena(fee2, false)) {
                                                        final SystemMessage sm2 = new SystemMessage(1491);
                                                        sm2.addItemName(cropId);
                                                        sm2.addNumber(count);
                                                        activeChar.sendPacket(sm2, Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                                                    } else {
                                                        crop.setAmount(crop.getAmount() - count);
                                                        castle.updateCrop(crop.getId(), crop.getAmount(), 0);
                                                        castle.addToTreasuryNoTax(fee2, false, false);
                                                        if (activeChar.getInventory().addItem(rewardItemId2, rewardItemCount) != null) {
                                                            activeChar.sendPacket(new SystemMessage(1490).addItemName(cropId).addNumber(count), SystemMessage2.removeItems(cropId, count), SystemMessage2.obtainItems(rewardItemId2, rewardItemCount, 0));
                                                            if (fee2 > 0L) {
                                                                activeChar.sendPacket(new SystemMessage(1607).addNumber(fee2));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        activeChar.sendChanges();
    }
}
