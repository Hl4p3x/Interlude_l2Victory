package handler.items;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeBookItemList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.util.Collection;

public class Recipes extends ScriptItemHandler {
    private static int[] _itemIds;

    public Recipes() {
        final Collection<Recipe> rc = RecipeHolder.getInstance().getRecipes();
        _itemIds = new int[rc.size()];
        int i = 0;
        for (final Recipe r : rc) {
            _itemIds[i++] = r.getItem().getItemId();
        }
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final Recipe recipe = RecipeHolder.getInstance().getRecipeByItem(item);
        switch (recipe.getType()) {
            case ERT_DWARF: {
                if (player.getDwarvenRecipeLimit() <= 0) {
                    player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE);
                    break;
                }
                if (player.getDwarvenRecipeBook().size() >= player.getDwarvenRecipeLimit()) {
                    player.sendPacket(Msg.NO_FURTHER_RECIPES_MAY_BE_REGISTERED);
                    return false;
                }
                if (recipe.getRequiredSkillLvl() > player.getSkillLevel(172)) {
                    player.sendPacket(Msg.CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE);
                    return false;
                }
                if (player.hasRecipe(recipe)) {
                    player.sendPacket(Msg.THAT_RECIPE_IS_ALREADY_REGISTERED);
                    return false;
                }
                if (!player.getInventory().destroyItem(item, 1L)) {
                    player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                    return false;
                }
                player.registerRecipe(recipe, true);
                player.sendPacket(new SystemMessage(851).addItemName(item.getItemId()));
                player.sendPacket(new RecipeBookItemList(player, true));
                return true;
            }
            case ERT_COMMON: {
                if (player.getCommonRecipeLimit() <= 0) {
                    player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE);
                    break;
                }
                if (player.getCommonRecipeBook().size() >= player.getCommonRecipeLimit()) {
                    player.sendPacket(Msg.NO_FURTHER_RECIPES_MAY_BE_REGISTERED);
                    return false;
                }
                if (player.hasRecipe(recipe)) {
                    player.sendPacket(Msg.THAT_RECIPE_IS_ALREADY_REGISTERED);
                    return false;
                }
                if (!player.getInventory().destroyItem(item, 1L)) {
                    player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                    return false;
                }
                player.registerRecipe(recipe, true);
                player.sendPacket(new SystemMessage(851).addItemName(item.getItemId()));
                player.sendPacket(new RecipeBookItemList(player, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }
}
