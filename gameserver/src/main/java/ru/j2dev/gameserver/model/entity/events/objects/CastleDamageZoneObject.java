package ru.j2dev.gameserver.model.entity.events.objects;

public class CastleDamageZoneObject extends ZoneObject {
    private final long _price;

    public CastleDamageZoneObject(final String name, final long price) {
        super(name);
        _price = price;
    }

    public long getPrice() {
        return _price;
    }
}
