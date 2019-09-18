package ru.j2dev.dataparser.holder.variationdata.support;

public class VariationFee {
    private final int _stoneId;
    private final int _feeItemId;
    private final long _feeItemCount;
    private final long _cancelFee;

    public VariationFee(final int stoneId, final int feeItemId, final long feeItemCount, final long cancelFee) {
        _stoneId = stoneId;
        _feeItemId = feeItemId;
        _feeItemCount = feeItemCount;
        _cancelFee = cancelFee;
    }

    public int getStoneId() {
        return _stoneId;
    }

    public int getFeeItemId() {
        return _feeItemId;
    }

    public long getFeeItemCount() {
        return _feeItemCount;
    }

    public long getCancelFee() {
        return _cancelFee;
    }
}
