package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.base.Element;

import java.io.Serializable;

public class ItemAttributes implements Serializable {
    private static final long serialVersionUID = 401594188363005415L;
    private int fire;
    private int water;
    private int wind;
    private int earth;
    private int holy;
    private int unholy;

    public ItemAttributes() {
        this(0, 0, 0, 0, 0, 0);
    }

    public ItemAttributes(final int fire, final int water, final int wind, final int earth, final int holy, final int unholy) {
        this.fire = fire;
        this.water = water;
        this.wind = wind;
        this.earth = earth;
        this.holy = holy;
        this.unholy = unholy;
    }

    public int getFire() {
        return fire;
    }

    public void setFire(final int fire) {
        this.fire = fire;
    }

    public int getWater() {
        return water;
    }

    public void setWater(final int water) {
        this.water = water;
    }

    public int getWind() {
        return wind;
    }

    public void setWind(final int wind) {
        this.wind = wind;
    }

    public int getEarth() {
        return earth;
    }

    public void setEarth(final int earth) {
        this.earth = earth;
    }

    public int getHoly() {
        return holy;
    }

    public void setHoly(final int holy) {
        this.holy = holy;
    }

    public int getUnholy() {
        return unholy;
    }

    public void setUnholy(final int unholy) {
        this.unholy = unholy;
    }

    public Element getElement() {
        if (fire > 0) {
            return Element.FIRE;
        }
        if (water > 0) {
            return Element.WATER;
        }
        if (wind > 0) {
            return Element.WIND;
        }
        if (earth > 0) {
            return Element.EARTH;
        }
        if (holy > 0) {
            return Element.HOLY;
        }
        if (unholy > 0) {
            return Element.UNHOLY;
        }
        return Element.NONE;
    }

    public int getValue() {
        if (fire > 0) {
            return fire;
        }
        if (water > 0) {
            return water;
        }
        if (wind > 0) {
            return wind;
        }
        if (earth > 0) {
            return earth;
        }
        if (holy > 0) {
            return holy;
        }
        if (unholy > 0) {
            return unholy;
        }
        return 0;
    }

    public void setValue(final Element element, final int value) {
        switch (element) {
            case FIRE: {
                fire = value;
                break;
            }
            case WATER: {
                water = value;
                break;
            }
            case WIND: {
                wind = value;
                break;
            }
            case EARTH: {
                earth = value;
                break;
            }
            case HOLY: {
                holy = value;
                break;
            }
            case UNHOLY: {
                unholy = value;
                break;
            }
        }
    }

    public int getValue(final Element element) {
        switch (element) {
            case FIRE: {
                return fire;
            }
            case WATER: {
                return water;
            }
            case WIND: {
                return wind;
            }
            case EARTH: {
                return earth;
            }
            case HOLY: {
                return holy;
            }
            case UNHOLY: {
                return unholy;
            }
            default: {
                return 0;
            }
        }
    }

    @Override
    public ItemAttributes clone() {
        return new ItemAttributes(fire, water, wind, earth, holy, unholy);
    }
}
