package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.Location;

public class TeleportLocation extends Location {
    private final long _price;
    private final int _minLevel;
    private final int _maxLevel;
    private final ItemTemplate _item;
    private final String _name;
    private final int _castleId;

    public TeleportLocation(final int item, final long price, final int minLevel, final int maxLevel, final String name, final int castleId) {
        _price = price;
        _minLevel = minLevel;
        _maxLevel = maxLevel;
        _name = name;
        _item = ItemTemplateHolder.getInstance().getTemplate(item);
        _castleId = castleId;
    }

    public TeleportLocation(final int item, final long price, final String name, final int castleId) {
        _price = price;
        _minLevel = 0;
        _maxLevel = 0;
        _name = name;
        _item = ItemTemplateHolder.getInstance().getTemplate(item);
        _castleId = castleId;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public int getMaxLevel() {
        return _maxLevel;
    }

    public long getPrice() {
        return _price;
    }

    public ItemTemplate getItem() {
        return _item;
    }

    public String getName() {
        return _name;
    }

    public int getCastleId() {
        return _castleId;
    }
}
