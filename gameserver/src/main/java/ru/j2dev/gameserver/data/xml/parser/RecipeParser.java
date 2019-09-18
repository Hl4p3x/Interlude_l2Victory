package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.model.Recipe.ERecipeType;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeParser extends AbstractFileParser<RecipeHolder> {

    protected RecipeParser() {
        super(RecipeHolder.getInstance());
    }

    public static RecipeParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/recipe.xml");
    }

    @Override
    protected void readData(final RecipeHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(recipeListElement -> "recipe".equalsIgnoreCase(recipeListElement.getName())).forEach(recipeListElement -> {
            final int recipeId = Integer.parseInt(recipeListElement.getAttributeValue("id"));
            final int minCraftSkillLvl = Integer.parseInt(recipeListElement.getAttributeValue("level"));
            final int craftMpConsume = Integer.parseInt(recipeListElement.getAttributeValue("mp_consume"));
            final int successRate = Integer.parseInt(recipeListElement.getAttributeValue("success_rate"));
            int rareSuccessRate = 0;
            if (recipeListElement.getAttributeValue("rare_success_rate") != null) {
                rareSuccessRate = Integer.parseInt(recipeListElement.getAttributeValue("rare_success_rate"));
            }
            final int recipeItemId = Integer.parseInt(recipeListElement.getAttributeValue("item_id"));
            final ItemTemplate recipeItem = ItemTemplateHolder.getInstance().getTemplate(recipeItemId);
            final ERecipeType recipeType = Boolean.parseBoolean(recipeListElement.getAttributeValue("is_common")) ? ERecipeType.ERT_COMMON : ERecipeType.ERT_DWARF;
            final List<Pair<ItemTemplate, Long>> materials = new ArrayList<>();
            final List<Pair<ItemTemplate, Long>> products = new ArrayList<>();
            final List<Pair<ItemTemplate, Long>> rare_products = new ArrayList<>();
            final List<Pair<ItemTemplate, Long>> npcFees = new ArrayList<>();
            recipeListElement.getChildren().forEach(recipeElement -> {
                if ("materials".equalsIgnoreCase(recipeElement.getName())) {
                    addParametrs(materials, recipeElement);
                } else if ("products".equalsIgnoreCase(recipeElement.getName())) {
                    addParametrs(products, recipeElement);
                } else if ("rare_products".equalsIgnoreCase(recipeElement.getName())) {
                    addParametrs(rare_products, recipeElement);
                } else if ("npc_fee".equalsIgnoreCase(recipeElement.getName())) {
                    addParametrs(npcFees, recipeElement);
                }
            });
            if (recipeItem == null) {
                warn("Skip recipe " + recipeId);
            } else if (products.isEmpty()) {
                warn("Recipe " + recipeId + " have empty product list. Skip");
            } else {
                if (products.size() > 1) {
                    warn("Recipe " + recipeId + " have more than one product. Skip");
                }
                if (materials.isEmpty()) {
                    warn("Recipe " + recipeId + " have empty material list. Skip");
                } else {
                    final Recipe recipe = new Recipe(recipeId, recipeItem, recipeType, minCraftSkillLvl, craftMpConsume, successRate, rareSuccessRate, Collections.unmodifiableList(materials), Collections.unmodifiableList(products), Collections.unmodifiableList(rare_products), Collections.unmodifiableList(npcFees));
                    holder.addRecipe(recipe);
                }
            }
        });
    }

    private void addParametrs(List<Pair<ItemTemplate, Long>> materials, Element recipeElement) {
        for (final Element recipeMaterialElement : recipeElement.getChildren()) {
            final Pair<ItemTemplate, Long> material = parseItem(recipeMaterialElement);
            if (material == null) {
                return;
            }
            materials.add(material);
        }
    }

    private Pair<ItemTemplate, Long> parseItem(final Element itemElement) {
        if (!"item".equalsIgnoreCase(itemElement.getName())) {
            return null;
        }
        final int itemId = Integer.parseInt(itemElement.getAttributeValue("id"));
        final ItemTemplate itemTemplate = ItemTemplateHolder.getInstance().getTemplate(itemId);
        final long itemCount = Long.parseLong(itemElement.getAttributeValue("count"));
        return Pair.of(itemTemplate, itemCount);
    }

    private static class LazyHolder {
        protected static final RecipeParser INSTANCE = new RecipeParser();
    }
}
