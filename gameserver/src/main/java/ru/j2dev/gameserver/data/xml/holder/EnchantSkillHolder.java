package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.SkillEnchant;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class EnchantSkillHolder extends AbstractHolder {

    private final Map<Integer, Map<Integer, SkillEnchant>> _skillsEnchantLevels = new TreeMap<>();
    private final Map<Integer, Map<Integer, Map<Integer, SkillEnchant>>> _skillsEnchantRoutes = new TreeMap<>();

    public static EnchantSkillHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addEnchantSkill(final SkillEnchant skillEnchant) {
        final int skillId = skillEnchant.getSkillId();
        Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.computeIfAbsent(skillId, k -> new TreeMap<>());
        skillEnchantLevels.put(skillEnchant.getSkillLevel(), skillEnchant);
        Map<Integer, Map<Integer, SkillEnchant>> skillEnchantRoutes = _skillsEnchantRoutes.computeIfAbsent(skillId, k -> new TreeMap<>());
        final int skillRouteId = skillEnchant.getRouteId();
        Map<Integer, SkillEnchant> skillRouteLevels = skillEnchantRoutes.computeIfAbsent(skillRouteId, k -> new TreeMap<>());
        skillRouteLevels.put(skillEnchant.getSkillLevel(), skillEnchant);
    }

    public SkillEnchant getSkillEnchant(final int skillId, final int skillLvl) {
        final Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
        if (skillEnchantLevels == null) {
            return null;
        }
        return skillEnchantLevels.get(skillLvl);
    }

    public SkillEnchant getSkillEnchant(final int skillId, final int routeId, final int enchantLevel) {
        final Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
        if (skillEnchantLevels == null) {
            return null;
        }
        for (final SkillEnchant skillEnchant : skillEnchantLevels.values()) {
            if (skillEnchant.getRouteId() == routeId && skillEnchant.getEnchantLevel() == enchantLevel) {
                return skillEnchant;
            }
        }
        return null;
    }

    public Map<Integer, Map<Integer, SkillEnchant>> getRoutesOf(final int skillId) {
        final Map<Integer, Map<Integer, SkillEnchant>> skillEnchantRoutes = _skillsEnchantRoutes.get(skillId);
        if (skillEnchantRoutes == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(skillEnchantRoutes);
    }

    public int getFirstSkillLevelOf(final int skillId, final int routeId) {
        final Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
        if (skillEnchantLevels == null) {
            return 0;
        }
        for (final SkillEnchant se : skillEnchantLevels.values()) {
            if (se.getRouteId() == routeId && se.getEnchantLevel() == 1) {
                return se.getSkillLevel();
            }
        }
        return 0;
    }

    public int getMaxEnchantLevelOf(final int skillId) {
        int maxEnchLevel = 0;
        final Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
        if (skillEnchantLevels == null) {
            return 0;
        }
        for (final SkillEnchant se : skillEnchantLevels.values()) {
            if (se.getEnchantLevel() > maxEnchLevel) {
                maxEnchLevel = se.getEnchantLevel();
            }
        }
        return maxEnchLevel;
    }

    public Map<Integer, SkillEnchant> getLevelsOf(final int skillId) {
        final Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
        if (skillEnchantLevels == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(skillEnchantLevels);
    }

    public void addEnchantSkill(final int skillId, final int skillLevel, final int enchantLevel, final int routeId, final long exp, final int sp, final int[] chances, final int itemId, final long itemCount) {
        addEnchantSkill(new SkillEnchant(skillId, skillLevel, enchantLevel, routeId, exp, sp, chances, itemId, itemCount));
    }

    @Override
    public int size() {
        return _skillsEnchantLevels.size();
    }

    @Override
    public void clear() {
        _skillsEnchantLevels.clear();
    }

    private static class LazyHolder {
        private static final EnchantSkillHolder INSTANCE = new EnchantSkillHolder();
    }
}
