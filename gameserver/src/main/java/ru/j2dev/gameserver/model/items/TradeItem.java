package ru.j2dev.gameserver.model.items;

public final class TradeItem extends ItemInfo {
    private long _price;
    private long _referencePrice;
    private long _currentValue;
    private int _lastRechargeTime;
    private int _rechargeTime;

    public TradeItem() {
    }

    public TradeItem(final ItemInstance item) {
        super(item);
        setReferencePrice(item.getReferencePrice());
    }

    public long getOwnersPrice() {
        return _price;
    }

    public void setOwnersPrice(final long price) {
        _price = price;
    }

    public long getReferencePrice() {
        return _referencePrice;
    }

    public void setReferencePrice(final long price) {
        _referencePrice = price;
    }

    public long getStorePrice() {
        return getReferencePrice() / 2L;
    }

    public long getCurrentValue() {
        return _currentValue;
    }

    public void setCurrentValue(final long value) {
        _currentValue = value;
    }

    public int getRechargeTime() {
        return _rechargeTime;
    }

    public void setRechargeTime(final int rechargeTime) {
        _rechargeTime = rechargeTime;
    }

    public boolean isCountLimited() {
        return getCount() > 0L;
    }

    public int getLastRechargeTime() {
        return _lastRechargeTime;
    }

    public void setLastRechargeTime(final int lastRechargeTime) {
        _lastRechargeTime = lastRechargeTime;
    }
}
