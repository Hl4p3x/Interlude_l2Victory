package ru.j2dev.gameserver.templates.item.support;

public class VariationGroupData {
    private final long _cancelPrice;
    private final int _mineralItemId;
    private final int _gemstoneItemId;
    private final long _gemstoneItemCnt;

    public VariationGroupData(final int mineralItemId, final int gemstoneItemId, final long gemstoneItemCnt, final long cancelPrice) {
        _mineralItemId = mineralItemId;
        _gemstoneItemId = gemstoneItemId;
        _gemstoneItemCnt = gemstoneItemCnt;
        _cancelPrice = cancelPrice;
    }

    public long getCancelPrice() {
        return _cancelPrice;
    }

    public int getMineralItemId() {
        return _mineralItemId;
    }

    public int getGemstoneItemId() {
        return _gemstoneItemId;
    }

    public long getGemstoneItemCnt() {
        return _gemstoneItemCnt;
    }
}
