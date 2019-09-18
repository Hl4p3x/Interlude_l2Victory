package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.items.ManufactureItem;

import java.util.Collection;
import java.util.List;

public class RecipeShopManageList extends L2GameServerPacket {
    private final List<ManufactureItem> createList;
    private final Collection<Recipe> recipes;
    private final int sellerId;
    private final long adena;
    private final boolean isDwarven;

    public RecipeShopManageList(final Player seller, final boolean isDwarvenCraft) {
        sellerId = seller.getObjectId();
        adena = seller.getAdena();
        isDwarven = isDwarvenCraft;
        if (isDwarven) {
            recipes = seller.getDwarvenRecipeBook();
        } else {
            recipes = seller.getCommonRecipeBook();
        }
        createList = seller.getCreateList();
        for (final ManufactureItem mi : createList) {
            if (!seller.findRecipe(mi.getRecipeId())) {
                createList.remove(mi);
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd8);
        writeD(sellerId);
        writeD((int) Math.min(adena, 2147483647L));
        writeD(isDwarven ? 0 : 1);
        writeD(recipes.size());
        int i = 1;
        for (final Recipe recipe : recipes) {
            writeD(recipe.getId());
            writeD(i++);
        }
        writeD(createList.size());
        createList.forEach(mi -> {
            writeD(mi.getRecipeId());
            writeD(0);
            writeD((int) mi.getCost());
        });
    }
}
