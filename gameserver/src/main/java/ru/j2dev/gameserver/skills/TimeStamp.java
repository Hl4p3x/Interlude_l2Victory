package ru.j2dev.gameserver.skills;

import ru.j2dev.gameserver.model.Skill;

public class TimeStamp {
    private final int _id;
    private final int _level;
    private final long _reuse;
    private final long _endTime;

    public TimeStamp(final int id, final long endTime, final long reuse) {
        _id = id;
        _level = 0;
        _reuse = reuse;
        _endTime = endTime;
    }

    public TimeStamp(final Skill skill, final long reuse) {
        this(skill, System.currentTimeMillis() + reuse, reuse);
    }

    public TimeStamp(final Skill skill, final long endTime, final long reuse) {
        _id = skill.getId();
        _level = skill.getLevel();
        _reuse = reuse;
        _endTime = endTime;
    }

    public long getReuseBasic() {
        if (_reuse == 0L) {
            return getReuseCurrent();
        }
        return _reuse;
    }

    public long getReuseCurrent() {
        return Math.max(_endTime - System.currentTimeMillis(), 0L);
    }

    public long getEndTime() {
        return _endTime;
    }

    public boolean hasNotPassed() {
        return System.currentTimeMillis() < _endTime;
    }

    public int getId() {
        return _id;
    }

    public int getLevel() {
        return _level;
    }
}
