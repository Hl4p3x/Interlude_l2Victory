package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.item.support.EnchantItem;
import ru.j2dev.gameserver.templates.item.support.EnchantScroll;

import java.util.HashMap;
import java.util.Map;

public class EnchantItemHolder extends AbstractHolder {

    private final Map<Integer, EnchantItem> _items = new HashMap<>();

    public static EnchantItemHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public void log() {
        info("load " + _items.size() + " enchant item(s).");
    }

    public EnchantItem getEnchantItem(final int item_id) {
        return _items.get(item_id);
    }

    public EnchantScroll getEnchantScroll(final int item_id) {
        final EnchantItem ei = getEnchantItem(item_id);
        if (ei instanceof EnchantScroll) {
            return (EnchantScroll) ei;
        }
        return null;
    }

    public void addEnchantItem(final EnchantItem ei) {
        _items.put(ei.getItemId(), ei);
    }

    public int[] getScrollIds() {
        final TIntSet is = new TIntHashSet();
        _items.values().stream().filter(ei -> ei instanceof EnchantScroll).mapToInt(EnchantItem::getItemId).forEach(is::add);
        return is.toArray(new int[is.size()]);
    }

    @Override
    public int size() {
        return _items.size();
    }

    @Override
    public void clear() {
        _items.clear();
    }

    private static class LazyHolder {
        private static final EnchantItemHolder INSTANCE = new EnchantItemHolder();
    }
}
