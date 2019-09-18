package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.base.ClassType2;

public final class SkillLearn implements Comparable<SkillLearn> {
    private final int _id;
    private final int _level;
    private final int _minLevel;
    private final int _cost;
    private final int _itemId;
    private final long _itemCount;
    private final boolean _clicked;
    private final ClassType2 _classType;
    private final boolean _autoLearn;

    public SkillLearn(final int id, final int lvl, final int minLvl, final int cost, final int itemId, final long itemCount, final boolean clicked, final boolean autoLearn) {
        _id = id;
        _level = lvl;
        _minLevel = minLvl;
        _cost = cost;
        _itemId = itemId;
        _itemCount = itemCount;
        _clicked = clicked;
        _classType = ClassType2.None;
        _autoLearn = autoLearn;
    }

    public SkillLearn(final int id, final int lvl, final int minLvl, final int cost, final int itemId, final long itemCount, final boolean clicked, final ClassType2 classType, final boolean autoLearn) {
        _id = id;
        _level = lvl;
        _minLevel = minLvl;
        _cost = cost;
        _itemId = itemId;
        _itemCount = itemCount;
        _clicked = clicked;
        _classType = classType;
        _autoLearn = autoLearn;
    }

    public int getId() {
        return _id;
    }

    public int getLevel() {
        return _level;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public int getCost() {
        return _cost;
    }

    public int getItemId() {
        return _itemId;
    }

    public long getItemCount() {
        return _itemCount;
    }

    public boolean isClicked() {
        return _clicked;
    }

    public boolean canAutoLearn() {
        if (!Config.AUTO_LEARN_FORGOTTEN_SKILLS && isClicked()) {
            return false;
        }
        if (_id == 1405) {
            return Config.AUTO_LEARN_DIVINE_INSPIRATION;
        }
        return _autoLearn;
    }

    @Override
    public int compareTo(final SkillLearn o) {
        if (getId() == o.getId()) {
            return getLevel() - o.getLevel();
        }
        return getId() - o.getId();
    }

    public ClassType2 getClassType2() {
        return _classType;
    }
}
