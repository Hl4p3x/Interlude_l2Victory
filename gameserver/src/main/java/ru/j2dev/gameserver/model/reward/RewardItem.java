package ru.j2dev.gameserver.model.reward;

public class RewardItem {
    public final int itemId;
    public long count;
    public boolean isAdena;
    public boolean isSealStone;

    public RewardItem(final int itemId) {
        this.itemId = itemId;
        count = 1L;
    }
}
