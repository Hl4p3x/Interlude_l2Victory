package ru.j2dev.gameserver.model.base;

import java.util.ArrayList;
import java.util.List;

public class MultiSellEntry {
    private final List<MultiSellIngredient> _ingredients;
    private final List<MultiSellIngredient> _production;
    private int _entryId;
    private long _tax;

    public MultiSellEntry() {
        _ingredients = new ArrayList<>();
        _production = new ArrayList<>();
    }

    public MultiSellEntry(final int id) {
        _ingredients = new ArrayList<>();
        _production = new ArrayList<>();
        _entryId = id;
    }

    public MultiSellEntry(final int id, final int product, final int prod_count, final int enchant) {
        _ingredients = new ArrayList<>();
        _production = new ArrayList<>();
        _entryId = id;
        addProduct(new MultiSellIngredient(product, prod_count, enchant));
    }

    public int getEntryId() {
        return _entryId;
    }

    public void setEntryId(final int entryId) {
        _entryId = entryId;
    }

    public void addIngredient(final MultiSellIngredient ingredient) {
        if (ingredient.getItemCount() > 0L) {
            _ingredients.add(ingredient);
        }
    }

    public List<MultiSellIngredient> getIngredients() {
        return _ingredients;
    }

    public void addProduct(final MultiSellIngredient ingredient) {
        _production.add(ingredient);
    }

    public List<MultiSellIngredient> getProduction() {
        return _production;
    }

    public long getTax() {
        return _tax;
    }

    public void setTax(final long tax) {
        _tax = tax;
    }

    @Override
    public int hashCode() {
        return _entryId;
    }

    @Override
    public MultiSellEntry clone() {
        final MultiSellEntry ret = new MultiSellEntry(_entryId);
        for (final MultiSellIngredient i : _ingredients) {
            ret.addIngredient(i.clone());
        }
        for (final MultiSellIngredient i : _production) {
            ret.addProduct(i.clone());
        }
        return ret;
    }
}
