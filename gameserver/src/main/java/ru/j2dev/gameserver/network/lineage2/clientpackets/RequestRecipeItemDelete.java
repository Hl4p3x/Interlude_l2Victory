package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.Recipe.ERecipeType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeBookItemList;

public class RequestRecipeItemDelete extends L2GameClientPacket {
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
        if (activeChar.getPrivateStoreType() == 5) {
            activeChar.sendActionFailed();
            return;
        }
        final Recipe recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
        if (recipe == null) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.unregisterRecipe(_recipeId);
        activeChar.sendPacket(new RecipeBookItemList(activeChar, recipe.getType() == ERecipeType.ERT_DWARF));
    }
}
