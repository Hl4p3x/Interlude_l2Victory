package ru.j2dev.gameserver.model;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.List;

public class Recipe {
    private final int _id;
    private final ItemTemplate _item;
    private final ERecipeType _type;
    private final int _requiredSkillLvl;
    private final int _mpConsume;
    private final int _successRate;
    private final int _rareSuccessRate;
    private final List<Pair<ItemTemplate, Long>> _materials;
    private final List<Pair<ItemTemplate, Long>> _products;
    private final List<Pair<ItemTemplate, Long>> _rareProducts;
    private final List<Pair<ItemTemplate, Long>> _npcFees;

    public Recipe(final int id, final ItemTemplate item, final ERecipeType type, final int requiredSkillLvl, final int mpConsume, final int successRate, final int rareSuccessRate, final List<Pair<ItemTemplate, Long>> materials, final List<Pair<ItemTemplate, Long>> products, final List<Pair<ItemTemplate, Long>> rareProducts, final List<Pair<ItemTemplate, Long>> npcFees) {
        _id = id;
        _item = item;
        _type = type;
        _requiredSkillLvl = requiredSkillLvl;
        _mpConsume = mpConsume;
        _successRate = successRate;
        _rareSuccessRate = rareSuccessRate;
        _materials = materials;
        _products = products;
        _rareProducts = rareProducts;
        _npcFees = npcFees;
    }

    public int getId() {
        return _id;
    }

    public ItemTemplate getItem() {
        return _item;
    }

    public ERecipeType getType() {
        return _type;
    }

    public int getRequiredSkillLvl() {
        return _requiredSkillLvl;
    }

    public int getMpConsume() {
        return _mpConsume;
    }

    public int getSuccessRate() {
        return _successRate;
    }

    public int getRareSuccessRate() {
        return _rareSuccessRate;
    }

    public List<Pair<ItemTemplate, Long>> getMaterials() {
        return _materials;
    }

    public List<Pair<ItemTemplate, Long>> getProducts() {
        return _products;
    }

    public List<Pair<ItemTemplate, Long>> getRareProducts() {
        return _rareProducts;
    }

    public List<Pair<ItemTemplate, Long>> getNpcFees() {
        return _npcFees;
    }

    @Override
    public int hashCode() {
        return _id;
    }

    @Override
    public String toString() {
        return "Recipe{_id=" + _id + ", _item=" + _item + ", _type=" + _type + ", _requiredSkillLvl=" + _requiredSkillLvl + ", _mpConsume=" + _mpConsume + ", _successRate=" + _successRate + ", _materials=" + _materials + ", _products=" + _products + ", _npcFees=" + _npcFees + '}';
    }

    public enum ERecipeType {
        ERT_DWARF,
        ERT_COMMON;

        public boolean isApplicableBy(final Player player) {
            return this != ERT_DWARF || player.getRace() == Race.dwarf;
        }
    }
}
