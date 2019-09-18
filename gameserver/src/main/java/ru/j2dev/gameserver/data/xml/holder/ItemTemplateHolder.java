package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public final class ItemTemplateHolder extends AbstractHolder {

    private final TIntObjectHashMap<ItemTemplate> _items = new TIntObjectHashMap<>();
    private ItemTemplate[] _allTemplates;

    public static ItemTemplateHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addItem(final ItemTemplate template) {
        _items.put(template.getItemId(), template);
    }

    private void buildFastLookupTable() {
        int highestId = IntStream.of(_items.keys()).filter(id -> id >= 0).max().orElse(0);
        _allTemplates = new ItemTemplate[highestId + 1];
        _items.valueCollection().forEach(itemTemplate -> _allTemplates[itemTemplate.getItemId()] = itemTemplate);
    }

    public ItemTemplate getTemplate(final int id) {
        final ItemTemplate item = ArrayUtils.valid(_allTemplates, id);
        if (item == null) {
            warn("Not defined item id : " + id + ", or out of range!", new Exception());
            return null;
        }
        return _allTemplates[id];
    }

    public ItemTemplate[] getAllTemplates() {
        return _allTemplates;
    }

    private void itemBreakCrystalPrice() {
        final Map<ItemGrade, Long> refGradeCrystalPrices = new HashMap<>();
        for (ItemGrade itemGrade : ItemGrade.values()) {
            if (itemGrade.cry > 0) {
                final ItemTemplate crystalItem = getTemplate(itemGrade.cry);
                refGradeCrystalPrices.put(itemGrade, (long) Objects.requireNonNull(crystalItem).getReferencePrice());
            }
        }
        final TIntObjectIterator<ItemTemplate> iterator = _items.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            final ItemTemplate itemTemplate = iterator.value();
            if (itemTemplate == null) {
                continue;
            }
            final int crystalCount = itemTemplate.getCrystalCount();
            final long refPrice = itemTemplate.getReferencePrice();
            final ItemGrade itemGrade2 = itemTemplate.getCrystalType();
            final Long crystalPrice = refGradeCrystalPrices.get(itemGrade2);
            if (crystalPrice == null || itemGrade2.cry == itemTemplate.getItemId() || crystalCount == 0) {
                continue;
            }
            if (refPrice == 0L) {
                continue;
            }
            final long crystalizedPrice = crystalCount * crystalPrice;
            if (crystalPrice <= refPrice) {
                continue;
            }
            warn("Reference price (" + refPrice + ") of item \"" + itemTemplate.getItemId() + "\" lower than crystal price (" + crystalizedPrice + ")");
        }
    }

    private void processAdditionalChecks() {
        itemBreakCrystalPrice();
    }

    @Override
    protected void process() {
        buildFastLookupTable();
        processAdditionalChecks();
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
        private static final ItemTemplateHolder INSTANCE = new ItemTemplateHolder();
    }
}
