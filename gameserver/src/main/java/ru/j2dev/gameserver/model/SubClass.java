package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.Experience;

public class SubClass {
    private int _class;
    private int _level = Config.ALT_LEVEL_AFTER_GET_SUBCLASS;
    private long minExp = Experience.LEVEL[Config.ALT_LEVEL_AFTER_GET_SUBCLASS];
    private long maxExp = Experience.LEVEL[Experience.LEVEL.length - 1];
    private long _exp = Math.max(minExp, Experience.LEVEL[_level]);
    private int _sp;
    private double _Hp = 1.0;
    private double _Mp = 1.0;
    private double _Cp = 1.0;
    private boolean _active;
    private boolean _isBase;
    private DeathPenalty _dp;

    public int getClassId() {
        return _class;
    }

    public void setClassId(final int classId) {
        _class = classId;
    }

    public long getExp() {
        return _exp;
    }

    public void setExp(long val) {
        val = Math.max(val, minExp);
        val = Math.min(val, maxExp);
        _exp = val;
        _level = Experience.getLevel(_exp);
    }

    public long getMaxExp() {
        return maxExp;
    }

    public void addExp(final long val) {
        setExp(_exp + val);
    }

    public long getSp() {
        return Math.min(_sp, Integer.MAX_VALUE);
    }

    public void setSp(long spValue) {
        spValue = Math.max(spValue, 0L);
        spValue = Math.min(spValue, 2147483647L);
        _sp = (int) spValue;
    }

    public void addSp(final long val) {
        setSp(_sp + val);
    }

    public int getLevel() {
        return _level;
    }

    public double getHp() {
        return _Hp;
    }

    public void setHp(final double hpValue) {
        _Hp = hpValue;
    }

    public double getMp() {
        return _Mp;
    }

    public void setMp(final double mpValue) {
        _Mp = mpValue;
    }

    public double getCp() {
        return _Cp;
    }

    public void setCp(final double cpValue) {
        _Cp = cpValue;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean active) {
        _active = active;
    }

    public boolean isBase() {
        return _isBase;
    }

    public void setBase(final boolean base) {
        _isBase = base;
        minExp = Experience.LEVEL[_isBase ? 1 : Config.ALT_LEVEL_AFTER_GET_SUBCLASS];
        maxExp = Experience.LEVEL[(_isBase ? Experience.getMaxLevel() : Experience.getMaxSubLevel()) + 1] - 1L;
    }

    public DeathPenalty getDeathPenalty(final Player player) {
        if (_dp == null) {
            _dp = new DeathPenalty(player, 0);
        }
        return _dp;
    }

    public void setDeathPenalty(final DeathPenalty dp) {
        _dp = dp;
    }

    @Override
    public String toString() {
        return ClassId.VALUES[_class] + " " + _level;
    }
}
