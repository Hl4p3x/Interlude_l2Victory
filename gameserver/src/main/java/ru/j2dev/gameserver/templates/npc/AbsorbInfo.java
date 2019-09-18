package ru.j2dev.gameserver.templates.npc;


import gnu.trove.set.hash.TIntHashSet;

public class AbsorbInfo {
    private final boolean _skill;
    private final AbsorbType _absorbType;
    private final int _chance;
    private final int _cursedChance;
    private final TIntHashSet _levels;

    public AbsorbInfo(final boolean skill, final AbsorbType absorbType, final int chance, final int cursedChance, final int min, final int max) {
        _skill = skill;
        _absorbType = absorbType;
        _chance = chance;
        _cursedChance = cursedChance;
        _levels = new TIntHashSet(max - min);
        for (int i = min; i <= max; ++i) {
            _levels.add(i);
        }
    }

    public boolean isSkill() {
        return _skill;
    }

    public AbsorbType getAbsorbType() {
        return _absorbType;
    }

    public int getChance() {
        return _chance;
    }

    public int getCursedChance() {
        return _cursedChance;
    }

    public boolean canAbsorb(final int le) {
        return _levels.contains(le);
    }

    public enum AbsorbType {
        LAST_HIT,
        PARTY_ONE,
        PARTY_ALL,
        PARTY_RANDOM
    }
}
