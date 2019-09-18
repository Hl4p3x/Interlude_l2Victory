package ru.j2dev.gameserver.skills;

import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.funcs.Func;

/**
 * @author VISTALL
 * @date 0:15/03.06.2011
 */
public class SkillEntry {
    public static final SkillEntry[] EMPTY_ARRAY = new SkillEntry[0];

    private final SkillEntryType _entryType;
    private final Skill _skill;

    private boolean _disabled;

    private SkillEntry(final SkillEntryType key, final Skill value) {
        _entryType = key;
        _skill = value;
    }

    public boolean isDisabled() {
        return _disabled;
    }

    public void setDisabled(final boolean disabled) {
        _disabled = disabled;
    }

    public SkillEntryType getEntryType() {
        return _entryType;
    }

    public Skill getTemplate() {
        return _skill;
    }

    public int getId() {
        return _skill.getId();
    }

    public int getDisplayId() {
        return _skill.getDisplayId();
    }

    public int getLevel() {
        return _skill.getLevel();
    }

    public int getDisplayLevel() {
        return _skill.getDisplayLevel();
    }

    public Skill.SkillType getSkillType() {
        return _skill.getSkillType();
    }

    public String getName() {
        return _skill.getName();
    }

    /*public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        return _skill.checkCondition(this, activeChar, target, forceUse, dontMove, first);
    }

    public SystemMsg checkTarget(final Creature activeChar, final Creature target, final Creature aimingTarget, final boolean forceUse, final boolean first) {
        return _skill.checkTarget(activeChar, target, aimingTarget, forceUse, first);
    }

    public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster) {
        _skill.getEffects(this, effector, effected, calcChance, applyOnCaster);
    }

    public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final long timeConst, final double timeMult, final int timeFix, final boolean skillReflected) {
        _skill.getEffects(this, effector, effected, calcChance, applyOnCaster, timeConst, timeMult, timeFix, skillReflected);
    }

    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        _skill.useSkill(this, activeChar, targets);
    } */

    public SkillEntry copyTo(final SkillEntryType entryType) {
        return new SkillEntry(entryType, _skill);
    }

    public Func[] getStatFuncs() {
        return _skill.getStatFuncs(this);
    }

    public int getLevelWithoutEnchant() {
        return getDisplayLevel() > 100 ? getTemplate().getBaseLevel() : getLevel();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return _skill.hashCode();
    }

    @Override
    public String toString() {
        return _skill.toString();
    }
}
