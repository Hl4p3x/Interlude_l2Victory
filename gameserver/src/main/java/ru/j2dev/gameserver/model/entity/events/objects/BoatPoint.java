package ru.j2dev.gameserver.model.entity.events.objects;

import org.jdom2.Element;
import ru.j2dev.gameserver.utils.Location;

public class BoatPoint extends Location {
    private final int _fuel;
    private int _speed1;
    private int _speed2;
    private boolean _teleport;

    public BoatPoint(final int x, final int y, final int z, final int h, final int speed1, final int speed2, final int fuel, final boolean teleport) {
        super(x, y, z, h);
        _speed1 = speed1;
        _speed2 = speed2;
        _fuel = fuel;
        _teleport = teleport;
    }

    public static BoatPoint parse(final Element element) {
        final int speed1 = (element.getAttributeValue("speed1") == null) ? 0 : Integer.parseInt(element.getAttributeValue("speed1"));
        final int speed2 = (element.getAttributeValue("speed2") == null) ? 0 : Integer.parseInt(element.getAttributeValue("speed2"));
        final int x = Integer.parseInt(element.getAttributeValue("x"));
        final int y = Integer.parseInt(element.getAttributeValue("y"));
        final int z = Integer.parseInt(element.getAttributeValue("z"));
        final int h = (element.getAttributeValue("h") == null) ? 0 : Integer.parseInt(element.getAttributeValue("h"));
        final int fuel = (element.getAttributeValue("fuel") == null) ? 0 : Integer.parseInt(element.getAttributeValue("fuel"));
        final boolean teleport = Boolean.parseBoolean(element.getAttributeValue("teleport"));
        return new BoatPoint(x, y, z, h, speed1, speed2, fuel, teleport);
    }

    public int getSpeed1() {
        return _speed1;
    }

    public void setSpeed1(final int speed1) {
        _speed1 = speed1;
    }

    public int getSpeed2() {
        return _speed2;
    }

    public void setSpeed2(final int speed2) {
        _speed2 = speed2;
    }

    public int getFuel() {
        return _fuel;
    }

    public boolean isTeleport() {
        return _teleport;
    }

    public void setTeleport(final boolean teleport) {
        _teleport = teleport;
    }
}
