package ru.j2dev.gameserver.templates.item;

import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class ArmorTemplate extends ItemTemplate {
    public static final double EMPTY_RING = 5.0;
    public static final double EMPTY_EARRING = 9.0;
    public static final double EMPTY_NECKLACE = 13.0;
    public static final double EMPTY_HELMET = 12.0;
    public static final double EMPTY_BODY_FIGHTER = 31.0;
    public static final double EMPTY_LEGS_FIGHTER = 18.0;
    public static final double EMPTY_BODY_MYSTIC = 15.0;
    public static final double EMPTY_LEGS_MYSTIC = 8.0;
    public static final double EMPTY_GLOVES = 8.0;
    public static final double EMPTY_BOOTS = 7.0;

    public ArmorTemplate(final StatsSet set) {
        super(set);
        type = set.getEnum("type", ArmorType.class);
        if (_bodyPart == SLOT_NECK || (_bodyPart & SLOT_L_EAR) != 0 || (_bodyPart & SLOT_L_FINGER) != 0) {
            _type1 = TYPE1_WEAPON_RING_EARRING_NECKLACE;
            _type2 = TYPE2_ACCESSORY;
        } else if (_bodyPart == SLOT_HAIR || _bodyPart == SLOT_DHAIR || _bodyPart == SLOT_HAIRALL) {
            _type1 = TYPE1_OTHER;
            _type2 = ItemTemplate.TYPE2_OTHER;
        } else {
            _type1 = TYPE1_SHIELD_ARMOR;
            _type2 = TYPE2_SHIELD_ARMOR;
        }
        if (getItemType() == ArmorType.PET) {
            _type1 = 1;
            switch (_bodyPart) {
                case -100: {
                    _type2 = 6;
                    _bodyPart = 1024;
                    break;
                }
                case -104: {
                    _type2 = 10;
                    _bodyPart = 1024;
                    break;
                }
                case -101: {
                    _type2 = 7;
                    _bodyPart = 1024;
                    break;
                }
                case -105: {
                    _type2 = 11;
                    _bodyPart = 8;
                    break;
                }
                case -103: {
                    _type2 = 12;
                    _bodyPart = 1024;
                    break;
                }
                default: {
                    _type2 = 8;
                    _bodyPart = 1024;
                    break;
                }
            }
        }
    }

    @Override
    public ArmorType getItemType() {
        return (ArmorType) super.type;
    }

    @Override
    public final long getItemMask() {
        return getItemType().mask();
    }

    public enum ArmorType implements ItemType {
        NONE(1, "None"),
        LIGHT(2, "Light"),
        HEAVY(3, "Heavy"),
        MAGIC(4, "Magic"),
        PET(5, "Pet"),
        SIGIL(6, "Sigil");

        public static final ArmorType[] VALUES = values();

        private final long _mask;
        private final String _name;

        ArmorType(final int id, final String name) {
            _mask = 1L << id + WeaponType.VALUES.length;
            _name = name;
        }

        @Override
        public long mask() {
            return _mask;
        }

        @Override
        public String toString() {
            return _name;
        }
    }
}
