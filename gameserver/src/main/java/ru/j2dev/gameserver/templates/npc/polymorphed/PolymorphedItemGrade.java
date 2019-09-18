package ru.j2dev.gameserver.templates.npc.polymorphed;

/**
 * Created by JunkyFunky
 * on 05.01.2018 23:15
 * group j2dev
 */
public enum PolymorphedItemGrade {
    NONE(0, 20),
    D(20, 40),
    C(40, 52),
    B(52, 61),
    A(61, 76),
    S(76, 80);

    private final int _minLvl;
    private final int _maxLvl;

    PolymorphedItemGrade(final int minLvl, final int maxLvl) {
        _minLvl = minLvl;
        _maxLvl = maxLvl;
    }

}