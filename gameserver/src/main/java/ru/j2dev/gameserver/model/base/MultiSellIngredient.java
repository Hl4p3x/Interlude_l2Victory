package ru.j2dev.gameserver.model.base;

import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.items.ItemAttributes;

public class MultiSellIngredient implements Cloneable {
    private int _itemId;
    private long _itemCount;
    private int _itemEnchant;
    private ItemAttributes _itemAttributes;
    private boolean _mantainIngredient;

    public MultiSellIngredient(final int itemId, final long itemCount) {
        this(itemId, itemCount, 0);
    }

    public MultiSellIngredient(final int itemId, final long itemCount, final int enchant) {
        _itemId = itemId;
        _itemCount = itemCount;
        _itemEnchant = enchant;
        _mantainIngredient = false;
        _itemAttributes = new ItemAttributes();
    }

    @Override
    public MultiSellIngredient clone() {
        final MultiSellIngredient mi = new MultiSellIngredient(_itemId, _itemCount, _itemEnchant);
        mi.setMantainIngredient(_mantainIngredient);
        mi.setItemAttributes(_itemAttributes.clone());
        return mi;
    }

    public int getItemId() {
        return _itemId;
    }

    public void setItemId(final int itemId) {
        _itemId = itemId;
    }

    public long getItemCount() {
        return _itemCount;
    }

    public void setItemCount(final long itemCount) {
        _itemCount = itemCount;
    }

    public boolean isStackable() {
        return _itemId <= 0 || ItemTemplateHolder.getInstance().getTemplate(_itemId).isStackable();
    }

    public int getItemEnchant() {
        return _itemEnchant;
    }

    public void setItemEnchant(final int itemEnchant) {
        _itemEnchant = itemEnchant;
    }

    public ItemAttributes getItemAttributes() {
        return _itemAttributes;
    }

    public void setItemAttributes(final ItemAttributes attr) {
        _itemAttributes = attr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + (int) (_itemCount ^ _itemCount >>> 32);
        for (final Element e : Element.VALUES) {
            result = 31 * result + _itemAttributes.getValue(e);
        }
        result = 31 * result + _itemEnchant;
        result = 31 * result + _itemId;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultiSellIngredient other = (MultiSellIngredient) obj;
        if (_itemId != other._itemId) {
            return false;
        }
        if (_itemCount != other._itemCount) {
            return false;
        }
        if (_itemEnchant != other._itemEnchant) {
            return false;
        }
        for (final Element e : Element.VALUES) {
            if (_itemAttributes.getValue(e) != other._itemAttributes.getValue(e)) {
                return false;
            }
        }
        return true;
    }

    public boolean getMantainIngredient() {
        return _mantainIngredient;
    }

    public void setMantainIngredient(final boolean mantainIngredient) {
        _mantainIngredient = mantainIngredient;
    }
}
