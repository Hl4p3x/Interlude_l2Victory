package ru.j2dev.gameserver.templates;

public class SoulCrystal {
    private final int _itemId;
    private final int _level;
    private final int _nextItemId;
    private final int _cursedNextItemId;

    public SoulCrystal(final int itemId, final int level, final int nextItemId, final int cursedNextItemId) {
        _itemId = itemId;
        _level = level;
        _nextItemId = nextItemId;
        _cursedNextItemId = cursedNextItemId;
    }

    public int getItemId() {
        return _itemId;
    }

    public int getLevel() {
        return _level;
    }

    public int getNextItemId() {
        return _nextItemId;
    }

    public int getCursedNextItemId() {
        return _cursedNextItemId;
    }
}
