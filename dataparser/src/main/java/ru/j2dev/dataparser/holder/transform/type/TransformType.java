package ru.j2dev.dataparser.holder.transform.type;

/**
 * @author : Mangol
 */
public enum TransformType {
    CURSED(true),
    COMBAT(true),
    NON_COMBAT(false),
    MODE_CHANGE(false),
    RIDING_MODE(false),
    FLYING(true),
    PURE_STAT(true);

    private final boolean isCombat;

    TransformType(final boolean isCombat) {
        this.isCombat = isCombat;
    }

    public boolean isCombatTransform() {
        return isCombat;
    }
}