package ru.j2dev.gameserver.templates;

import gnu.trove.map.hash.TIntIntHashMap;
import ru.j2dev.gameserver.model.Skill;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class CubicTemplate {
    private final int _id;
    private final int _level;
    private final int _delay;
    private final List<Entry<Integer, List<SkillInfo>>> _skills;

    public CubicTemplate(final int id, final int level, final int delay) {
        _skills = new ArrayList<>(3);
        _id = id;
        _level = level;
        _delay = delay;
    }

    public void putSkills(final int chance, final List<SkillInfo> skill) {
        _skills.add(new SimpleImmutableEntry<>(chance, skill));
    }

    public Iterable<Entry<Integer, List<SkillInfo>>> getSkills() {
        return _skills;
    }

    public int getDelay() {
        return _delay;
    }

    public int getId() {
        return _id;
    }

    public int getLevel() {
        return _level;
    }

    public enum ActionType {
        ATTACK,
        DEBUFF,
        CANCEL,
        HEAL
    }

    public static class SkillInfo {
        private final Skill _skill;
        private final int _chance;
        private final ActionType _actionType;
        private final boolean _canAttackDoor;
        private final int _minHp;
        private final int _minHpPercent;
        private final TIntIntHashMap _chanceList;

        public SkillInfo(final Skill skill, final int chance, final ActionType actionType, final boolean canAttackDoor, final int minHp, final int minHpPercent, final TIntIntHashMap set) {
            _skill = skill;
            _chance = chance;
            _actionType = actionType;
            _canAttackDoor = canAttackDoor;
            _minHp = minHp;
            _minHpPercent = minHpPercent;
            _chanceList = set;
        }

        public int getChance() {
            return _chance;
        }

        public ActionType getActionType() {
            return _actionType;
        }

        public Skill getSkill() {
            return _skill;
        }

        public boolean isCanAttackDoor() {
            return _canAttackDoor;
        }

        public int getMinHp() {
            return _minHp;
        }

        public int getMinHpPercent() {
            return _minHpPercent;
        }

        public int getChance(final int a) {
            return _chanceList.get(a);
        }
    }
}
