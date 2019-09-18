package ru.j2dev.gameserver.templates.item.support;

import gnu.trove.set.TIntSet;
import ru.j2dev.gameserver.model.entity.SevenSigns;

public class MerchantGuard {
    private final int _itemId;
    private final int _npcId;
    private final int _max;
    private final TIntSet _ssq;

    public MerchantGuard(final int itemId, final int npcId, final int max, final TIntSet ssq) {
        _itemId = itemId;
        _npcId = npcId;
        _max = max;
        _ssq = ssq;
    }

    public int getItemId() {
        return _itemId;
    }

    public int getNpcId() {
        return _npcId;
    }

    public int getMax() {
        return _max;
    }

    public boolean isValidSSQPeriod() {
        return SevenSigns.getInstance().getCurrentPeriod() == 3 && _ssq.contains(SevenSigns.getInstance().getSealOwner(3));
    }
}
