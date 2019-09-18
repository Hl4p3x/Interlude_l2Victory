package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeShopItemInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.util.List;

public class RequestRecipeShopMakeDo extends L2GameClientPacket {
    private int _manufacturerId;
    private int _recipeId;
    private long _price;

    @Override
    protected void readImpl() {
        _manufacturerId = readD();
        _recipeId = readD();
        _price = readD();
    }

    @Override
    protected void runImpl() {
        final Player buyer = getClient().getActiveChar();
        if (buyer == null) {
            return;
        }
        if (buyer.isActionsDisabled()) {
            buyer.sendActionFailed();
            return;
        }
        if (buyer.isInStoreMode()) {
            buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (buyer.isInTrade()) {
            buyer.sendActionFailed();
            return;
        }
        if (buyer.isFishing()) {
            buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
            return;
        }
        if (!buyer.getPlayerAccess().UseTrade) {
            buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
            return;
        }
        final Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
        if (manufacturer == null || manufacturer.getPrivateStoreType() != 5 || !manufacturer.isInActingRange(buyer)) {
            buyer.sendActionFailed();
            return;
        }
        Recipe recipe = null;
        for (final ManufactureItem mi : manufacturer.getCreateList()) {
            if (mi.getRecipeId() == _recipeId && _price == mi.getCost()) {
                recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
                break;
            }
        }
        if (recipe == null) {
            buyer.sendActionFailed();
            return;
        }
        int success = 0;
        if (recipe.getProducts().isEmpty() || recipe.getMaterials().isEmpty()) {
            manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer).addItemName(recipe.getItem()));
            buyer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer).addItemName(recipe.getItem()));
            return;
        }
        if (!manufacturer.findRecipe(_recipeId)) {
            buyer.sendActionFailed();
            return;
        }
        if (manufacturer.getCurrentMp() < recipe.getMpConsume()) {
            manufacturer.sendPacket(Msg.NOT_ENOUGH_MP);
            buyer.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
            return;
        }
        final List<Pair<ItemTemplate, Long>> materials = recipe.getMaterials();
        final List<Pair<ItemTemplate, Long>> products = recipe.getProducts();
        buyer.getInventory().writeLock();
        try {
            if (buyer.getAdena() < _price) {
                buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
                return;
            }
            for (final Pair<ItemTemplate, Long> material : materials) {
                final ItemTemplate materialItem = material.getKey();
                final long materialAmount = material.getValue();
                if (materialAmount <= 0L) {
                    continue;
                }
                final ItemInstance item = buyer.getInventory().getItemByItemId(materialItem.getItemId());
                if (item == null || item.getCount() < materialAmount) {
                    buyer.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
                    return;
                }
            }
            int totalWeight = 0;
            long totalSlotCount = 0L;
            for (final Pair<ItemTemplate, Long> product : products) {
                totalWeight += (int) (product.getKey().getWeight() * product.getValue());
                totalSlotCount += product.getKey().isStackable() ? 1L : product.getValue();
            }
            if (!buyer.getInventory().validateWeight(totalWeight) || !buyer.getInventory().validateCapacity(totalSlotCount)) {
                buyer.sendPacket(Msg.THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
                return;
            }
            if (!buyer.reduceAdena(_price, false)) {
                buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
                return;
            }
            materials.forEach(material2 -> {
                final ItemTemplate materialItem2 = material2.getKey();
                final long materialAmount2 = material2.getValue();
                if (materialAmount2 <= 0L) {
                    return;
                }
                buyer.getInventory().destroyItemByItemId(materialItem2.getItemId(), materialAmount2);
                buyer.sendPacket(SystemMessage2.removeItems(materialItem2.getItemId(), materialAmount2));
            });
            final long tax = TradeHelper.getTax(manufacturer, _price);
            if (tax > 0L) {
                _price -= tax;
                manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax", manufacturer).addNumber(tax));
            }
            manufacturer.addAdena(_price);
        } finally {
            buyer.getInventory().writeUnlock();
        }
        products.forEach(product2 -> manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.GotOrder", manufacturer).addItemName(product2.getKey())));
        manufacturer.reduceCurrentMp(recipe.getMpConsume(), null);
        manufacturer.sendStatusUpdate(false, false, 11);
        final int rareRate = recipe.getRareSuccessRate();
        if (rareRate > 0 && Rnd.chance(rareRate)) {
            recipe.getRareProducts().forEach(product2 -> ItemFunctions.addItem(buyer, product2.getKey().getItemId(), product2.getValue(), true));
            success = 1;
        } else if (Rnd.chance(recipe.getSuccessRate())) {
            products.forEach(product2 -> ItemFunctions.addItem(buyer, product2.getKey().getItemId(), product2.getValue(), true));
            success = 1;
        }
        if (success == 0) {
            for (final Pair<ItemTemplate, Long> product3 : products) {
                final int itemId2 = product3.getKey().getItemId();
                SystemMessage sm = new SystemMessage(1150);
                sm.addString(manufacturer.getName());
                sm.addItemName(itemId2);
                sm.addNumber(_price);
                buyer.sendPacket(sm);
                sm = new SystemMessage(1149);
                sm.addString(buyer.getName());
                sm.addItemName(itemId2);
                sm.addNumber(_price);
                manufacturer.sendPacket(sm);
            }
        } else {
            for (final Pair<ItemTemplate, Long> product3 : products) {
                final int itemId2 = product3.getKey().getItemId();
                final long count2 = product3.getValue();
                if (count2 > 1L) {
                    SystemMessage sm = new SystemMessage(1148);
                    sm.addString(manufacturer.getName());
                    sm.addItemName(itemId2);
                    sm.addNumber(count2);
                    sm.addNumber(_price);
                    buyer.sendPacket(sm);
                    sm = new SystemMessage(1152);
                    sm.addString(buyer.getName());
                    sm.addItemName(itemId2);
                    sm.addNumber(count2);
                    sm.addNumber(_price);
                    manufacturer.sendPacket(sm);
                } else {
                    SystemMessage sm = new SystemMessage(1146);
                    sm.addString(manufacturer.getName());
                    sm.addItemName(itemId2);
                    sm.addNumber(_price);
                    buyer.sendPacket(sm);
                    sm = new SystemMessage(1151);
                    sm.addString(buyer.getName());
                    sm.addItemName(itemId2);
                    sm.addNumber(_price);
                    manufacturer.sendPacket(sm);
                }
            }
        }
        buyer.sendChanges();
        buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
    }
}
