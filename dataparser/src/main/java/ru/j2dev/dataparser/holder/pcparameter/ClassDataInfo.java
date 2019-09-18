package ru.j2dev.dataparser.holder.pcparameter;

import ru.j2dev.dataparser.holder.pcparameter.bonus.LevelBonus;

/**
 * @author KilRoy
 */
public class ClassDataInfo {
    private final LevelBonus hp;
    private final LevelBonus mp;
    private final LevelBonus cp;

    public ClassDataInfo(final LevelBonus hp, final LevelBonus mp, final LevelBonus cp) {
        this.hp = hp;
        this.mp = mp;
        this.cp = cp;
    }

    public LevelBonus getHp() {
        return hp;
    }

    public LevelBonus getMp() {
        return mp;
    }

    public LevelBonus getCp() {
        return cp;
    }
}