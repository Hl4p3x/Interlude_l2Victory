package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.ClassType2;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;

import java.util.*;
import java.util.Map.Entry;

public final class SkillAcquireHolder extends AbstractHolder {

    private final TIntObjectHashMap<List<SkillLearn>> _normalSkillTree = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<List<SkillLearn>> _fishingSkillTree = new TIntObjectHashMap<>();
    private final List<SkillLearn> _pledgeSkillTree = new ArrayList<>();

    public static SkillAcquireHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int getMinLevelForNewSkill(final ClassId classId, final int currLevel, final AcquireType type) {
        switch (type) {
            case NORMAL: {
                final List<SkillLearn> skills = _normalSkillTree.get(classId.getId());
                if (skills == null) {
                    info("skill tree for class " + classId.getId() + " is not defined !");
                    return 0;
                }
                int minlevel = 0;
                for (final SkillLearn temp : skills) {
                    if (temp.getMinLevel() > currLevel && (minlevel == 0 || temp.getMinLevel() < minlevel)) {
                        minlevel = temp.getMinLevel();
                    }
                }
                return minlevel;
            }
            default: {
                return 0;
            }
        }
    }

    public Collection<SkillLearn> getAvailableSkills(final Player player, final AcquireType type) {
        return getAvailableSkills(player, player.getClassId(), type, null);
    }

    public Collection<SkillLearn> getAvailableSkills(final Player player, final ClassId classId, final AcquireType type, final SubUnit subUnit) {
        switch (type) {
            case NORMAL: {
                final Collection<SkillLearn> skills = _normalSkillTree.get(classId.getId());
                if (skills == null) {
                    info("skill tree for class " + classId + " is not defined !");
                    return Collections.emptyList();
                }
                return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
            }
            case FISHING: {
                final Collection<SkillLearn> skills = _fishingSkillTree.get(player.getRace().ordinal());
                if (skills == null) {
                    info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
                    return Collections.emptyList();
                }
                return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
            }
            case CLAN: {
                final Collection<SkillLearn> skills = _pledgeSkillTree;
                final Collection<Skill> skls = player.getClan().getSkills();
                return getAvaliableList(skills, skls.toArray(new Skill[0]), player.getClan().getLevel());
            }
            default: {
                return Collections.emptyList();
            }
        }
    }

    private Collection<SkillLearn> getAvaliableList(final Collection<SkillLearn> skillLearns, final Skill[] skills, final int level) {
        return getAvaliableList(skillLearns, skills, level, null);
    }

    private Collection<SkillLearn> getAvaliableList(final Collection<SkillLearn> skillLearns, final Skill[] skills, final int level, final Player target) {
        final Map<Integer, SkillLearn> skillLearnMap = new TreeMap<>();
        for (final SkillLearn temp : skillLearns) {
            if (temp.getMinLevel() <= level) {
                if (target != null && temp.getClassType2() != ClassType2.None) {
                    boolean learnable = false;
                    for (final Entry<Integer, SubClass> e : target.getSubClasses().entrySet()) {
                        if (e.getValue().isBase()) {
                            continue;
                        }
                        for (final ClassId ci : ClassId.values()) {
                            if (ci.getId() == e.getKey() && ci.getType2() == temp.getClassType2()) {
                                learnable = true;
                            }
                        }
                    }
                    if (!learnable) {
                        continue;
                    }
                }
                boolean knownSkill = false;
                for (int j = 0; j < skills.length && !knownSkill; ++j) {
                    if (skills[j].getId() == temp.getId()) {
                        knownSkill = true;
                        if (skills[j].getLevel() == temp.getLevel() - 1) {
                            skillLearnMap.put(temp.getId(), temp);
                        }
                    }
                }
                if (knownSkill || temp.getLevel() != 1) {
                    continue;
                }
                skillLearnMap.put(temp.getId(), temp);
            }
        }
        return skillLearnMap.values();
    }

    public SkillLearn getSkillLearn(final Player player, final ClassId classId, final int id, final int level, final AcquireType type) {
        List<SkillLearn> skills;
        switch (type) {
            case NORMAL: {
                skills = _normalSkillTree.get(classId.getId());
                break;
            }
            case FISHING: {
                skills = _fishingSkillTree.get(player.getRace().ordinal());
                break;
            }
            case CLAN: {
                skills = _pledgeSkillTree;
                break;
            }
            default: {
                return null;
            }
        }
        if (skills == null) {
            return null;
        }
        for (final SkillLearn temp : skills) {
            if (temp.getLevel() == level && temp.getId() == id) {
                return temp;
            }
        }
        return null;
    }

    public boolean isSkillPossible(final Player player, final Skill skill, final AcquireType type) {
        Clan clan;
        List<SkillLearn> skills;
        switch (type) {
            case NORMAL: {
                skills = _normalSkillTree.get(player.getActiveClassId());
                break;
            }
            case FISHING: {
                skills = _fishingSkillTree.get(player.getRace().ordinal());
                break;
            }
            case CLAN: {
                clan = player.getClan();
                if (clan == null) {
                    return false;
                }
                skills = _pledgeSkillTree;
                break;
            }
            default: {
                return false;
            }
        }
        return isSkillPossible(skills, skill);
    }

    public boolean isSkillPossible(final Player player, final ClassId classId, final Skill skill, final AcquireType type) {
        Clan clan;
        List<SkillLearn> skills;
        switch (type) {
            case NORMAL: {
                skills = _normalSkillTree.get(classId.getId());
                break;
            }
            case FISHING: {
                skills = _fishingSkillTree.get(player.getRace().ordinal());
                break;
            }
            case CLAN: {
                clan = player.getClan();
                if (clan == null) {
                    return false;
                }
                skills = _pledgeSkillTree;
                break;
            }
            default: {
                return false;
            }
        }
        return isSkillPossible(skills, skill);
    }

    private boolean isSkillPossible(final Collection<SkillLearn> skills, final Skill skill) {
        for (final SkillLearn learn : skills) {
            if (learn.getId() == skill.getId() && learn.getLevel() <= skill.getLevel()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSkillPossible(final Player player, final Skill skill) {
        for (final AcquireType aq : AcquireType.VALUES) {
            if (isSkillPossible(player, skill, aq)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSkillPossible(final Player player, final ClassId classId, final Skill skill) {
        for (final AcquireType aq : AcquireType.VALUES) {
            if (isSkillPossible(player, classId, skill, aq)) {
                return true;
            }
        }
        return false;
    }

    public List<SkillLearn> getSkillLearnListByItemId(final Player player, final int itemId) {
        final List<SkillLearn> learns = _normalSkillTree.get(player.getActiveClassId());
        if (learns == null) {
            return Collections.emptyList();
        }
        final List<SkillLearn> l = new ArrayList<>(1);
        for (final SkillLearn $i : learns) {
            if ($i.getItemId() == itemId) {
                l.add($i);
            }
        }
        return l;
    }

    public List<SkillLearn> getAllNormalSkillTreeWithForgottenScrolls() {
        final List<SkillLearn> a = new ArrayList<>();
        final TIntObjectIterator<List<SkillLearn>> i = _normalSkillTree.iterator();
        while (i.hasNext()) {
            i.advance();
            for (final SkillLearn learn : i.value()) {
                if (learn.getItemId() > 0 && learn.isClicked()) {
                    a.add(learn);
                }
            }
        }
        return a;
    }

    public void addAllNormalSkillLearns(final TIntObjectHashMap<List<SkillLearn>> map) {
        int classID;

        for (ClassId classId : ClassId.VALUES) {
            if (classId.getRace() == null) {
                continue;
            }

            classID = classId.getId();

            final List<SkillLearn> temp;

            temp = map.get(classID);
            if (temp == null) {
                info("Not found NORMAL skill learn for class " + classID);
                continue;
            }

            _normalSkillTree.put(classId.getId(), temp);

            ClassId secondparent = classId.getParent(1);
            if (secondparent == classId.getParent(0)) {
                secondparent = null;
            }

            classId = classId.getParent(0);

            while (classId != null) {
                final List<SkillLearn> parentList = _normalSkillTree.get(classId.getId());
                temp.addAll(parentList);

                classId = classId.getParent(0);
                if (classId == null && secondparent != null) {
                    classId = secondparent;
                    secondparent = secondparent.getParent(1);
                }
            }
        }
    }

    public void addAllFishingLearns(final int race, final List<SkillLearn> s) {
        _fishingSkillTree.put(race, s);
    }

    public void addAllPledgeLearns(final List<SkillLearn> s) {
        _pledgeSkillTree.addAll(s);
    }

    @Override
    public void log() {
        info("load " + sizeTroveMap(_normalSkillTree) + " normal learns for " + _normalSkillTree.size() + " classes.");
        info("load " + sizeTroveMap(_fishingSkillTree) + " fishing learns for " + _fishingSkillTree.size() + " races.");
        info("load " + _pledgeSkillTree.size() + " pledge learns.");
    }

    @Deprecated
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        _normalSkillTree.clear();
        _fishingSkillTree.clear();
        _pledgeSkillTree.clear();
    }

    private int sizeTroveMap(final TIntObjectHashMap<List<SkillLearn>> a) {
        int i = 0;
        final TIntObjectIterator<List<SkillLearn>> iterator = a.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            i += iterator.value().size();
        }
        return i;
    }

    private static class LazyHolder {
        private static final SkillAcquireHolder INSTANCE = new SkillAcquireHolder();
    }
}
