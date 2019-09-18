package ru.j2dev.dataparser.holder.eventdata;

/**
 * @author KilRoy
 */
public class EventItemInfoDrop {
    private final int itemId;
    private final long itemCount;
    private final int itemChance;

    public EventItemInfoDrop(final int itemId, final long itemCount, final int itemChance) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.itemChance = itemChance;
    }

    public int getItemId() {
        return itemId;
    }

    public long getItemCount() {
        return itemCount;
    }

    public int getItemChance() {
        return itemChance;
    }
}