package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeItemMakeInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.List;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket {
    private int _recipeId;

    @Override
    protected void readImpl() {
        _recipeId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isProcessingRequest()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        final Recipe recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
        if (recipe == null || recipe.getMaterials().isEmpty() || recipe.getProducts().isEmpty()) {
            activeChar.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
            return;
        }
        if (activeChar.getCurrentMp() < recipe.getMpConsume()) {
            activeChar.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeItemMakeInfo(activeChar, recipe, 0));
            return;
        }
        if (!activeChar.findRecipe(_recipeId)) {
            activeChar.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, ActionFail.STATIC);
            return;
        }
        boolean succeed = false;
        final List<Pair<ItemTemplate, Long>> materials = recipe.getMaterials();
        final List<Pair<ItemTemplate, Long>> products = recipe.getProducts();
        activeChar.getInventory().writeLock();
        try {
            for (final Pair<ItemTemplate, Long> material : materials) {
                final ItemTemplate materialItem = material.getKey();
                final long materialAmount = material.getValue();
                if (materialAmount <= 0L) {
                    continue;
                }
                if (Config.ALT_GAME_UNREGISTER_RECIPE && materialItem.getItemType() == EtcItemType.RECIPE) {
                    final Recipe recipe2 = RecipeHolder.getInstance().getRecipeByItem(materialItem);
                    if (activeChar.hasRecipe(recipe2)) {
                        continue;
                    }
                    activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipe, 0));
                    return;
                } else {
                    final ItemInstance item = activeChar.getInventory().getItemByItemId(materialItem.getItemId());
                    if (item == null || item.getCount() < materialAmount) {
                        activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipe, 0));
                        return;
                    }
                    continue;
                }
            }
            int totalWeight = 0;
            long totalSlotCount = 0L;
            for (final Pair<ItemTemplate, Long> product : products) {
                totalWeight += (int) (product.getKey().getWeight() * product.getValue());
                totalSlotCount += product.getKey().isStackable() ? 1L : product.getValue();
            }
            if (!activeChar.getInventory().validateWeight(totalWeight) || !activeChar.getInventory().validateCapacity(totalSlotCount)) {
                activeChar.sendPacket(Msg.WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE, new RecipeItemMakeInfo(activeChar, recipe, 0));
                return;
            }
            for (final Pair<ItemTemplate, Long> material2 : materials) {
                final ItemTemplate materialItem2 = material2.getKey();
                final long materialAmount2 = material2.getValue();
                if (materialAmount2 <= 0L) {
                    continue;
                }
                if (Config.ALT_GAME_UNREGISTER_RECIPE && materialItem2.getItemType() == EtcItemType.RECIPE) {
                    activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByItem(materialItem2).getId());
                } else {
                    if (!activeChar.getInventory().destroyItemByItemId(materialItem2.getItemId(), materialAmount2)) {
                        continue;
                    }
                    activeChar.sendPacket(SystemMessage2.removeItems(materialItem2.getItemId(), materialAmount2));
                }
            }
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        activeChar.resetWaitSitTime();
        activeChar.reduceCurrentMp(recipe.getMpConsume(), null);
        final int rareRate = recipe.getRareSuccessRate();
        if (rareRate > 0 && Rnd.chance(rareRate)) {
            recipe.getRareProducts().forEach(product2 -> ItemFunctions.addItem(activeChar, product2.getKey().getItemId(), product2.getValue(), true));
            succeed = true;
        } else if (Rnd.chance(recipe.getSuccessRate())) {
            products.forEach(product2 -> ItemFunctions.addItem(activeChar, product2.getKey().getItemId(), product2.getValue(), true));
            succeed = true;
        }
        if (!succeed) {
            for (final Pair<ItemTemplate, Long> product2 : products) {
                activeChar.sendPacket(new SystemMessage(960).addItemName(product2.getKey().getItemId()));
            }
        }
        activeChar.sendPacket(new RecipeItemMakeInfo(activeChar, recipe, succeed ? 0 : 1));
    }
}
