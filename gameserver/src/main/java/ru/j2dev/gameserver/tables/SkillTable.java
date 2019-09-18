package ru.j2dev.gameserver.tables;

import gnu.trove.map.hash.TIntIntHashMap;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.skills.SkillsEngine;

import java.util.Collections;
import java.util.Map;

public class SkillTable {
    private static final SkillTable _instance = new SkillTable();

    private Map<Integer, Map<Integer, Skill>> _skills = Collections.emptyMap();
    private TIntIntHashMap _maxLevelsTable = new TIntIntHashMap();
    private TIntIntHashMap _baseLevelsTable = new TIntIntHashMap();

    public static SkillTable getInstance() {
        return _instance;
    }

    public void load() {
        _skills = SkillsEngine.getInstance().loadAllSkills();
        makeLevelsTable();
    }

    public void reload() {
        load();
    }

    public Skill getInfo(final int skillId, final int skillLevel) {
        final Map<Integer, Skill> skillLevels = _skills.get(skillId);
        if (skillLevels == null) {
            return null;
        }
        return skillLevels.get(skillLevel);
    }

    public static int getSkillIndex(final Skill skill) {
        return getSkillIndex(skill.getId(), skill.getLevel());
    }

    public static int getSkillIndex(int skillId, int skillLevel) {
        return (skillId << 16) | skillLevel;
    }

    public int getMaxLevel(final int skillId) {
        return _maxLevelsTable.get(skillId);
    }

    public int getBaseLevel(final int skillId) {
        return _baseLevelsTable.get(skillId);
    }

    private void makeLevelsTable() {
        _skills.values().stream().flatMap(ss -> ss.values().stream()).forEach(s -> {
            final int skillId = s.getId();
            final int level = s.getLevel();
            final int maxLevel = _maxLevelsTable.get(skillId);
            if (level > maxLevel) {
                _maxLevelsTable.put(skillId, level);
            }
            if (_baseLevelsTable.get(skillId) == 0) {
                _baseLevelsTable.put(skillId, s.getBaseLevel());
            }
        });
    }
}
