package ru.j2dev.gameserver.templates.item;

/**
 * @author: VISTALL
 * @date: 13:52/15.01.2011
 */
public enum Bodypart {
    NONE(0),
    CHEST(0x400),
    BELT(0x10000000),
    RIGHT_BRACELET(0x100000),
    LEFT_BRACELET(0x200000),
    FULL_ARMOR(0x8000),
    HEAD(0x40),
    HAIR(0x10000),
    FACE(0x40000),
    HAIR_ALL(0x80000),
    UNDERWEAR(0x1),
    BACK(0x2000),
    NECKLACE(0x8),
    LEGS(0x800),
    FEET(0x1000),
    GLOVES(0x200),
    RIGHT_HAND(0x80),
    LEFT_HAND(0x100),
    LEFT_RIGHT_HAND(0x4000),
    RIGHT_EAR(0x2),
    LEFT_EAR(0x4),
    RIGHT_FINGER(0x10),
    FORMAL_WEAR(0x20000),
    TALISMAN(0x400000),
    LEFT_FINGER(0x20),
    WOLF(0xffffff9c, CHEST),
    GREAT_WOLF(0xffffff98, CHEST),
    HATCHLING(0xffffff9b, CHEST),
    STRIDER(0xffffff9a, CHEST),
    BABY_PET(0xffffff99, CHEST),
    PENDANT(0xffffff97, NECKLACE);

    private final int _mask;
    private final Bodypart _real;

    Bodypart(final int mask) {
        this(mask, null);
    }

    Bodypart(final int mask, final Bodypart real) {
        _mask = mask;
        _real = real;
    }

    public int mask() {
        return _mask;
    }

    public Bodypart getReal() {
        return _real;
    }
}
