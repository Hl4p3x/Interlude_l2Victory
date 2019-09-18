package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.MultiSellEntry;
import ru.j2dev.gameserver.model.base.MultiSellIngredient;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemAttributes;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.util.ArrayList;
import java.util.List;

public class RequestMultiSellChoose extends L2GameClientPacket {
    private int _listId;
    private int _entryId;
    private long _amount;

    @Override
    protected void readImpl() {
        _listId = readD();
        _entryId = readD();
        _amount = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _amount < 1L) {
            return;
        }
        final MultiSellListContainer list1 = activeChar.getMultisell();
        if (list1 == null) {
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
            return;
        }
        if (list1.getListId() != _listId) {
            Log.add("Player " + activeChar.getName() + " trying to change multisell list id, ban this player!", "illegal-actions");
            activeChar.sendActionFailed();
            activeChar.setMultisell(null);
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
        final NpcInstance merchant = activeChar.getLastNpc();
        if (list1.getListId() >= 0 && !activeChar.isGM() && !NpcInstance.canBypassCheck(activeChar, merchant)) {
            activeChar.setMultisell(null);
            return;
        }
        MultiSellEntry entry = null;
        for (final MultiSellEntry $entry : list1.getEntries()) {
            if ($entry.getEntryId() == _entryId) {
                entry = $entry;
                break;
            }
        }
        if (entry == null) {
            return;
        }
        final boolean keepenchant = list1.isKeepEnchant();
        final boolean notax = list1.isNoTax();
        final List<ItemData> items = new ArrayList<>();
        final PcInventory inventory = activeChar.getInventory();
        long totalPrice = 0L;
        final Castle castle = (merchant != null) ? merchant.getCastle(activeChar) : null;
        inventory.writeLock();
        try {
            final long tax = SafeMath.mulAndCheck(entry.getTax(), _amount);
            long slots = 0L;
            long weight = 0L;
            for (final MultiSellIngredient i : entry.getProduction()) {
                if (i.getItemId() <= 0) {
                    continue;
                }
                final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(i.getItemId());
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(SafeMath.mulAndCheck(i.getItemCount(), _amount), item.getWeight()));
                if (item.isStackable()) {
                    if (inventory.getItemByItemId(i.getItemId()) != null) {
                        continue;
                    }
                    ++slots;
                } else {
                    slots = SafeMath.addAndCheck(slots, _amount);
                }
            }
            if (!inventory.validateWeight(weight)) {
                activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                activeChar.sendActionFailed();
                return;
            }
            if (!inventory.validateCapacity(slots)) {
                activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                activeChar.sendActionFailed();
                return;
            }
            if (entry.getIngredients().size() == 0) {
                activeChar.sendActionFailed();
                activeChar.setMultisell(null);
                return;
            }
            for (final MultiSellIngredient ingridient : entry.getIngredients()) {
                final int ingridientItemId = ingridient.getItemId();
                final long ingridientItemCount = ingridient.getItemCount();
                final int ingridientEnchant = ingridient.getItemEnchant();
                final long totalAmount = ingridient.getMantainIngredient() ? ingridientItemCount : SafeMath.mulAndCheck(ingridientItemCount, _amount);
                switch (ingridientItemId) {
                    case -200:
                        if (activeChar.getClan() == null) {
                            activeChar.sendPacket(Msg.YOU_ARE_NOT_A_CLAN_MEMBER);
                            return;
                        }
                        if (activeChar.getClan().getReputationScore() < totalAmount) {
                            activeChar.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                            return;
                        }
                        if (activeChar.getClan().getLeaderId() != activeChar.getObjectId()) {
                            activeChar.sendPacket(new SystemMessage(9).addString(activeChar.getName()));
                            return;
                        }
                        if (!ingridient.getMantainIngredient()) {
                            items.add(new ItemData(ingridientItemId, totalAmount, null));
                        }
                        break;
                    case -100:
                        if (activeChar.getPcBangPoints() < totalAmount) {
                            activeChar.sendPacket(Msg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
                            return;
                        }
                        if (!ingridient.getMantainIngredient()) {
                            items.add(new ItemData(ingridientItemId, totalAmount, null));
                        }
                        break;
                    default:
                        final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(ingridientItemId);
                        if (!template.isStackable()) {
                            for (int j = 0; j < ingridientItemCount * _amount; ++j) {
                                final List<ItemInstance> list2 = inventory.getItemsByItemId(ingridientItemId);
                                if (keepenchant) {
                                    ItemInstance itemToTake = null;
                                    for (final ItemInstance item2 : list2) {
                                        final ItemData itmd = new ItemData(item2.getItemId(), item2.getCount(), item2);
                                        if ((item2.getEnchantLevel() == ingridientEnchant || !item2.getTemplate().isEquipment()) && !items.contains(itmd) && item2.canBeExchanged(activeChar)) {
                                            itemToTake = item2;
                                            break;
                                        }
                                    }
                                    if (itemToTake == null) {
                                        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                                        return;
                                    }
                                    if (!ingridient.getMantainIngredient()) {
                                        items.add(new ItemData(itemToTake.getItemId(), 1L, itemToTake));
                                    }
                                } else {
                                    ItemInstance itemToTake = null;
                                    for (final ItemInstance item2 : list2) {
                                        if (!items.contains(new ItemData(item2.getItemId(), item2.getCount(), item2)) && (itemToTake == null || item2.getEnchantLevel() < itemToTake.getEnchantLevel()) && !item2.isShadowItem() && !item2.isTemporalItem() && (!item2.isAugmented() || Config.ALT_ALLOW_DROP_AUGMENTED) && ItemFunctions.checkIfCanDiscard(activeChar, item2)) {
                                            itemToTake = item2;
                                            if (itemToTake.getEnchantLevel() == 0) {
                                                break;
                                            }
                                            continue;
                                        }
                                    }
                                    if (itemToTake == null) {
                                        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                                        return;
                                    }
                                    if (!ingridient.getMantainIngredient()) {
                                        items.add(new ItemData(itemToTake.getItemId(), 1L, itemToTake));
                                    }
                                }
                            }
                        } else {
                            if (ingridientItemId == 57) {
                                totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(ingridientItemCount, _amount));
                            }
                            final ItemInstance item3 = inventory.getItemByItemId(ingridientItemId);
                            if (item3 == null || item3.getCount() < totalAmount) {
                                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                                return;
                            }
                            if (!ingridient.getMantainIngredient()) {
                                items.add(new ItemData(item3.getItemId(), totalAmount, item3));
                            }
                        }
                        break;
                }
                if (activeChar.getAdena() < totalPrice) {
                    activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
            }
            int enchantLevel = 0;
            ItemAttributes attributes = null;
            int variation_stat1 = 0;
            int variation_stat2 = 0;
            for (final ItemData id : items) {
                final long count = id.getCount();
                if (count > 0L) {
                    switch (id.getId()) {
                        case -200:
                            activeChar.getClan().incReputation((int) (-count), false, "MultiSell");
                            activeChar.sendPacket(new SystemMessage(1787).addNumber(count));
                            break;
                        case -100:
                            activeChar.reducePcBangPoints((int) count);
                            break;
                        default:
                            final ItemInstance it = id.getItem();
                            if (!inventory.destroyItem(id.getItem(), count)) {
                                return;
                            }
                            if (keepenchant && id.getItem().canBeEnchanted(true)) {
                                enchantLevel = id.getItem().getEnchantLevel();
                                attributes = id.getItem().getAttributes();
                                variation_stat1 = id.getItem().getVariationStat1();
                                variation_stat2 = id.getItem().getVariationStat2();
                            }
                            activeChar.sendPacket(SystemMessage2.removeItems(id.getId(), count));
                            Log.LogItem(activeChar, ItemLog.MultiSellIngredient, it, count, 0L, _listId);
                            break;
                    }
                }
            }
            if (tax > 0L && !notax && castle != null) {
                activeChar.sendMessage(new CustomMessage("trade.HavePaidTax", activeChar).addNumber(tax));
                if (merchant != null && merchant.getReflection() == ReflectionManager.DEFAULT) {
                    castle.addToTreasury(tax, true, false);
                }
            }
            for (final MultiSellIngredient in : entry.getProduction()) {
                if (in.getItemId() <= 0) {
                    if (in.getItemId() == -200) {
                        activeChar.getClan().incReputation((int) (in.getItemCount() * _amount), false, "MultiSell");
                        activeChar.sendPacket(new SystemMessage(1781).addNumber(in.getItemCount() * _amount));
                    } else {
                        if (in.getItemId() != -100) {
                            continue;
                        }
                        activeChar.addPcBangPoints((int) (in.getItemCount() * _amount), false);
                    }
                } else if (ItemTemplateHolder.getInstance().getTemplate(in.getItemId()).isStackable()) {
                    final long total = SafeMath.mulAndLimit(in.getItemCount(), _amount);
                    inventory.addItem(in.getItemId(), total);
                    activeChar.sendPacket(SystemMessage2.obtainItems(in.getItemId(), total, 0));
                    Log.LogItem(activeChar, ItemLog.MultiSellProduct, in.getItemId(), total, 0L, _listId);
                } else {
                    for (int k = 0; k < _amount; ++k) {
                        for (int l = 0; l < in.getItemCount(); ++l) {
                            final ItemInstance product = ItemFunctions.createItem(in.getItemId());
                            if (keepenchant) {
                                if (product.canBeEnchanted(true)) {
                                    product.setEnchantLevel(enchantLevel);
                                    if (attributes != null) {
                                        product.setAttributes(attributes.clone());
                                    }
                                    if (variation_stat1 != 0 || variation_stat2 != 0) {
                                        product.setVariationStat1(variation_stat1);
                                        product.setVariationStat2(variation_stat2);
                                    }
                                }
                            } else {
                                product.setEnchantLevel(in.getItemEnchant());
                                product.setAttributes(in.getItemAttributes().clone());
                            }
                            inventory.addItem(product);
                            activeChar.sendPacket(SystemMessage2.obtainItems(product));
                            Log.LogItem(activeChar, ItemLog.MultiSellProduct, product, product.getCount(), 0L, _listId);
                        }
                    }
                }
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        } finally {
            inventory.writeUnlock();
        }
        activeChar.sendChanges();
        if (!list1.isShowAll()) {
            MultiSellHolder.getInstance().SeparateAndSend(list1, activeChar, (castle == null) ? 0.0 : castle.getTaxRate());
        }
    }

    private class ItemData {
        private final int _id;
        private final long _count;
        private final ItemInstance _item;

        public ItemData(final int id, final long count, final ItemInstance item) {
            _id = id;
            _count = count;
            _item = item;
        }

        public int getId() {
            return _id;
        }

        public long getCount() {
            return _count;
        }

        public ItemInstance getItem() {
            return _item;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof ItemData)) {
                return false;
            }
            final ItemData i = (ItemData) obj;
            return _id == i._id && _count == i._count && _item == i._item;
        }
    }
}
