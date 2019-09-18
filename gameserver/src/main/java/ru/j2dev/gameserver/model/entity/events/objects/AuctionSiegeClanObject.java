package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.model.pledge.Clan;

public class AuctionSiegeClanObject extends SiegeClanObject {
    private long _bid;

    public AuctionSiegeClanObject(final String type, final Clan clan, final long param) {
        this(type, clan, param, System.currentTimeMillis());
    }

    public AuctionSiegeClanObject(final String type, final Clan clan, final long param, final long date) {
        super(type, clan, param, date);
        _bid = param;
    }

    @Override
    public long getParam() {
        return _bid;
    }

    public void setParam(final long param) {
        _bid = param;
    }
}
