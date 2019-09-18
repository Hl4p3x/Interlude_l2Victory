package ru.j2dev.gameserver.model.items;

public class ManufactureItem {
    private final int _recipeId;
    private final long _cost;

    public ManufactureItem(final int recipeId, final long cost) {
        _recipeId = recipeId;
        _cost = cost;
    }

    public int getRecipeId() {
        return _recipeId;
    }

    public long getCost() {
        return _cost;
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && ((ManufactureItem) o).getRecipeId() == getRecipeId());
    }
}
