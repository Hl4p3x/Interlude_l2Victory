package ru.j2dev.gameserver.model.base;

import ru.j2dev.gameserver.stats.Stats;

import java.util.Arrays;

public enum Element {
    FIRE(0, Stats.ATTACK_FIRE, Stats.DEFENCE_FIRE),
    WATER(1, Stats.ATTACK_WATER, Stats.DEFENCE_WATER),
    WIND(2, Stats.ATTACK_WIND, Stats.DEFENCE_WIND),
    EARTH(3, Stats.ATTACK_EARTH, Stats.DEFENCE_EARTH),
    HOLY(4, Stats.ATTACK_HOLY, Stats.DEFENCE_HOLY),
    UNHOLY(5, Stats.ATTACK_UNHOLY, Stats.DEFENCE_UNHOLY),
    NONE(-2, null, null);

    public static final Element[] VALUES = Arrays.copyOf(values(), 6);

    private final byte id;
    private final Stats attack;
    private final Stats defence;

    Element(final int id, final Stats attack, final Stats defence) {
        this.id = (byte) id;
        this.attack = attack;
        this.defence = defence;
    }

    public static Element getElementById(final int id) {
        for (final Element e : VALUES) {
            if (e.getId() == id) {
                return e;
            }
        }
        return NONE;
    }

    public static Element getReverseElement(final Element element) {
        switch (element) {
            case WATER: {
                return FIRE;
            }
            case FIRE: {
                return WATER;
            }
            case WIND: {
                return EARTH;
            }
            case EARTH: {
                return WIND;
            }
            case HOLY: {
                return UNHOLY;
            }
            case UNHOLY: {
                return HOLY;
            }
            default: {
                return NONE;
            }
        }
    }

    public static Element getElementByName(final String name) {
        for (final Element e : VALUES) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return NONE;
    }

    public byte getId() {
        return id;
    }

    public Stats getAttack() {
        return attack;
    }

    public Stats getDefence() {
        return defence;
    }
}
