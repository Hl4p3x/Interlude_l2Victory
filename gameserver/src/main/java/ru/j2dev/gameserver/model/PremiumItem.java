package ru.j2dev.gameserver.model;

public class PremiumItem {
    private final int _itemId;
    private final String _sender;
    private long _count;

    public PremiumItem(final int itemid, final long count, final String sender) {
        _itemId = itemid;
        _count = count;
        _sender = sender;
    }

    public void updateCount(final long newcount) {
        _count = newcount;
    }

    public int getItemId() {
        return _itemId;
    }

    public long getCount() {
        return _count;
    }

    public String getSender() {
        return _sender;
    }
}
