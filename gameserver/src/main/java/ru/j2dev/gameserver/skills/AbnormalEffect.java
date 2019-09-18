package ru.j2dev.gameserver.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect {
    NULL("null", 0x0),
    BLEEDING("bleeding", 0x1),
    POISON("poison", 0x2),
    REDCIRCLE("redcircle", 0x4),
    ICE("ice", 0x8),
    AFFRAID("affraid", 0x10),
    CONFUSED("confused", 0x20),
    STUN("stun", 0x40),
    SLEEP("sleep", 0x80),
    MUTED("muted", 0x100),
    ROOT("root", 0x200),
    HOLD_1("hold1", 0x400),
    HOLD_2("hold2", 0x800),
    UNKNOWN_13("unk13", 0x1000),
    BIG_HEAD("bighead", 0x2000),
    FLAME("flame", 0x4000),
    UNKNOWN_16("unk16", 0x8000),
    GROW("grow", 0x10000),
    FLOATING_ROOT("floatroot", 0x20000),
    DANCE_STUNNED("dancestun", 0x40000),
    FIREROOT_STUN("firerootstun", 0x80000),
    STEALTH("shadow", 0x100000),
    IMPRISIONING_1("imprison1", 0x200000),
    IMPRISIONING_2("imprison2", 0x400000),
    MAGIC_CIRCLE("magiccircle", 0x800000),
    ICE2("ice2", 0x1000000),
    EARTHQUAKE("earthquake", 0x2000000),
    UNKNOWN_27("unk27", 0x4000000),
    INVULNERABLE("invul1", 0x8000000),
    VITALITY("vitality", 0x10000000),
    REAL_TARGET("realtarget", 0x20000000),
    DEATH_MARK("deathmark", 0x40000000),
    SOUL_SHOCK("soulshock", Integer.MIN_VALUE),
    S_INVULNERABLE("invul2", 0x1, true),
    S_AIR_STUN("redglow", 0x2, true),
    S_AIR_ROOT("redglow2", 0x4, true),
    S_BAGUETTE_SWORD("baguettesword", 0x8, true),
    S_YELLOW_AFFRO("yellowafro", 0x10, true),
    S_PINK_AFFRO("pinkafro", 0x20, true),
    S_BLACK_AFFRO("blackafro", 0x40, true),
    S_UNKNOWN8("sunk8", 0x80, true),
    S_STIGMA("stigma", 0x100, true),
    S_UNKNOWN10("sunk10", 0x200, true),
    FROZEN_PILLAR("frozenpillar", 0x400, true),
    S_VESPER1("vesper1", 0x800, true),
    S_VESPER2("vesper2", 0x1000, true),
    S_VESPER3("vesper3", 0x2000, true),
    S_SOA_RESP("soa_respawn", 0x4000, true),
    S_ARCANE_SHIELD("arcane_invul", 0x8000, true),
    S_UNKNOWN17("sunk17", 0x10000, true),
    S_UNKNOWN18("sunk18", 0x20000, true),
    S_UNKNOWN19("sunk19", 0x40000, true),
    S_NAVIT("nevitSystem", 0x80000, true),
    S_UNKNOWN21("sunk21", 0x100000, true),
    S_LETTER("letter_collection_buff", 0x200000, true),
    S_UNKNOWN23("sunk23", 0x400000, true),
    S_UNKNOWN24("sunk24", 0x800000, true),
    S_UNKNOWN25("sunk25", 0x1000000, true),
    S_UNKNOWN26("sunk26", 0x2000000, true),
    S_UNKNOWN27("sunk27", 0x4000000, true),
    S_UNKNOWN28("sunk28", 0x8000000, true),
    S_UNKNOWN29("sunk29", 0x10000000, true),
    S_UNKNOWN30("sunk30", 0x20000000, true),
    S_UNKNOWN31("sunk31", 0x40000000, true),
    S_UNKNOWN32("sunk32", Integer.MIN_VALUE, true),
    E_AFRO_1("afrobaguette1", 0x1, false, true),
    E_AFRO_2("afrobaguette2", 0x2, false, true),
    E_AFRO_3("afrobaguette3", 0x4, false, true),
    E_EVASWRATH("evaswrath", 0x8, false, true),
    E_HEADPHONE("headphone", 0x10, false, true),
    E_VESPER_1("vesper1", 0x20, false, true),
    E_VESPER_2("vesper2", 0x40, false, true),
    E_VESPER_3("vesper3", 0x80, false, true);

    private final int _mask;
    private final String _name;
    private final boolean _special;
    private final boolean _event;

    AbnormalEffect(final String name, final int mask) {
        _name = name;
        _mask = mask;
        _special = false;
        _event = false;
    }

    AbnormalEffect(final String name, final int mask, final boolean special) {
        _name = name;
        _mask = mask;
        _special = special;
        _event = false;
    }

    AbnormalEffect(final String name, final int mask, final boolean special, final boolean event) {
        _name = name;
        _mask = mask;
        _special = special;
        _event = event;
    }

    public static AbnormalEffect getByName(final String name) {
        for (final AbnormalEffect eff : values()) {
            if (eff.getName().equals(name)) {
                return eff;
            }
        }
        throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
    }

    public final int getMask() {
        return _mask;
    }

    public final String getName() {
        return _name;
    }

    public final boolean isSpecial() {
        return _special;
    }

    public final boolean isEvent() {
        return _event;
    }
}
