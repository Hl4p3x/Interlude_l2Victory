package ru.j2dev.gameserver.templates.item;

/**
 * Created by JunkyFunky
 * on 07.07.2018 21:56
 * group j2dev
 */
public enum ItemGrade {
    NONE(0, 0),
    D(1458, 1),
    C(1459, 2),
    B(1460, 3),
    A(1461, 4),
    S(1462, 5);

    public final int cry;
    public final int externalOrdinal;

    ItemGrade(final int crystal, final int ext) {
        cry = crystal;
        externalOrdinal = ext;
    }

    public int gradeOrd() {
        return externalOrdinal;
    }
}
