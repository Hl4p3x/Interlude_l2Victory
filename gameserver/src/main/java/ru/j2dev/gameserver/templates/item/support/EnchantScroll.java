package ru.j2dev.gameserver.templates.item.support;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.commons.util.TroveUtils;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.item.ItemGrade;

public class EnchantScroll extends EnchantItem {
    private final EnchantScrollOnFailAction _failResultType;
    private final boolean _isInfallible;
    private final boolean _hasAVE;
    private final int _failResultLevel;
    private final int _increment;
    private TIntSet _items;

    public EnchantScroll(final int itemId, final int increment, final double chanceMod, final int minLvl, final int maxLvl, final EnchantTargetType ett, final EnchantScrollOnFailAction frt, final int lrl, final boolean isInfallible, final boolean hasAVE) {
        super(itemId, chanceMod, minLvl, maxLvl, ett);
        _items = TroveUtils.EMPTY_INT_SET;
        _increment = increment;
        _failResultLevel = lrl;
        _failResultType = frt;
        _isInfallible = isInfallible;
        _hasAVE = hasAVE;
    }

    public int getIncrement() {
        return _increment;
    }

    public int getFailResultLevel() {
        return _failResultLevel;
    }

    public EnchantScrollOnFailAction getOnFailAction() {
        return _failResultType;
    }

    public void addItemRestrict(final int item_type) {
        if (_items.isEmpty()) {
            _items = new TIntHashSet();
        }
        _items.add(item_type);
    }

    public boolean isHasAbnormalVisualEffect() {
        return _hasAVE;
    }

    public boolean isInfallible() {
        return _isInfallible;
    }

    public boolean isUsableWith(final ItemInstance target) {
        if (!_items.isEmpty() && !_items.contains(target.getItemId())) {
            return false;
        }
        final int toLvl = target.getEnchantLevel() + getIncrement();
        final ItemGrade itemItemGrade = target.getCrystalType();
        return getGrades().contains(itemItemGrade) && toLvl >= getMinLvl() && toLvl <= getMaxLvl() && getTargetType().isUsableOn(target);
    }
}
