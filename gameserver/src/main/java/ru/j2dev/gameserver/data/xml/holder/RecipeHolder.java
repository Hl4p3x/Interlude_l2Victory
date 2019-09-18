package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class RecipeHolder extends AbstractHolder {

    private final Map<Integer, Recipe> _recipesById = new HashMap<>();
    private final Map<Integer, Recipe> _recipesByRecipeItemId = new HashMap<>();

    public static RecipeHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addRecipe(final Recipe recipe) {
        if (_recipesById.containsKey(recipe.getId())) {
            warn("Recipe \"" + recipe.getId() + "\" already exists.");
        }
        _recipesById.put(recipe.getId(), recipe);
        _recipesByRecipeItemId.put(recipe.getItem().getItemId(), recipe);
    }

    public Recipe getRecipeById(final int recipeId) {
        return _recipesById.get(recipeId);
    }

    public Recipe getRecipeByItem(final ItemTemplate itemTemplate) {
        return getRecipeByItem(itemTemplate.getItemId());
    }

    public Recipe getRecipeByItem(final ItemInstance item) {
        return getRecipeByItem(item.getItemId());
    }

    public Recipe getRecipeByItem(final int itemId) {
        return _recipesByRecipeItemId.get(itemId);
    }

    public Collection<Recipe> getRecipes() {
        return _recipesById.values();
    }

    @Override
    public int size() {
        return _recipesById.size();
    }

    @Override
    public void clear() {
        _recipesById.clear();
        _recipesByRecipeItemId.clear();
    }

    private static class LazyHolder {
        private static final RecipeHolder INSTANCE = new RecipeHolder();
    }
}
