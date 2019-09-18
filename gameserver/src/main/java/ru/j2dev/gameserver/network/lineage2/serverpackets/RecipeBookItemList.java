package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;

import java.util.Collection;

public class RecipeBookItemList extends L2GameServerPacket {
    private final boolean _isDwarvenCraft;
    private final int _currentMp;
    private final Collection<Recipe> _recipes;

    public RecipeBookItemList(final Player player, final boolean isDwarvenCraft) {
        _isDwarvenCraft = isDwarvenCraft;
        _currentMp = (int) player.getCurrentMp();
        if (isDwarvenCraft) {
            _recipes = player.getDwarvenRecipeBook();
        } else {
            _recipes = player.getCommonRecipeBook();
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd6);
        writeD(_isDwarvenCraft ? 0 : 1);
        writeD(_currentMp);
        writeD(_recipes.size());
        _recipes.forEach(recipe -> {
            writeD(recipe.getId());
            writeD(1);
        });
    }
}
