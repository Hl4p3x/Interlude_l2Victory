package ru.j2dev.gameserver.templates.manor;

public class CropProcure {
    final int _rewardType;
    final int _cropId;
    final long _buy;
    long _buyResidual;
    long _price;

    public CropProcure(final int id) {
        _cropId = id;
        _buyResidual = 0L;
        _rewardType = 0;
        _buy = 0L;
        _price = 0L;
    }

    public CropProcure(final int id, final long amount, final int type, final long buy, final long price) {
        _cropId = id;
        _buyResidual = amount;
        _rewardType = type;
        _buy = buy;
        _price = price;
        if (_price < 0L) {
            _price = 0L;
        }
    }

    public int getReward() {
        return _rewardType;
    }

    public int getId() {
        return _cropId;
    }

    public long getAmount() {
        return _buyResidual;
    }

    public void setAmount(final long amount) {
        _buyResidual = amount;
    }

    public long getStartAmount() {
        return _buy;
    }

    public long getPrice() {
        return _price;
    }
}
