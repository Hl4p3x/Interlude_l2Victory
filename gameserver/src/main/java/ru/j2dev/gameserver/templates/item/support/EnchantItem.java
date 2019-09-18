package ru.j2dev.gameserver.templates.item.support;

import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.util.HashSet;
import java.util.Set;

public abstract class EnchantItem {
    private final int _itemId;
    private final double _chanceMod;

    private final Set<ItemGrade> _Item_grades;
    private final int _minLvl;
    private final int _maxLvl;
    private final EnchantTargetType _targetType;

    public EnchantItem(final int itemId, final double chanceMod, final int minLvl, final int maxLvl, final EnchantTargetType ett) {
        _itemId = itemId;
        _chanceMod = chanceMod;
        _minLvl = minLvl;
        _maxLvl = maxLvl;
        _targetType = ett;
        _Item_grades = new HashSet<>();
    }

    public int getItemId() {
        return _itemId;
    }

    public double getChanceMod() {
        return _chanceMod;
    }

    public int getMinLvl() {
        return _minLvl;
    }

    public int getMaxLvl() {
        return _maxLvl;
    }

    public EnchantTargetType getTargetType() {
        return _targetType;
    }

    public void setItemGrades(final Set<ItemGrade> itemGrade) {
        _Item_grades.addAll(itemGrade);
    }

    public Set<ItemGrade> getGrades() {
        return _Item_grades;
    }
}
