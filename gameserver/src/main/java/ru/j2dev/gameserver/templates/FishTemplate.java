package ru.j2dev.gameserver.templates;

public class FishTemplate {
    private final int _id;
    private final int _level;
    private final String _name;
    private final int _HP;
    private final int _HpRegen;
    private final int _type;
    private final int _group;
    private final int _fish_guts;
    private final int _guts_check_time;
    private final int _wait_time;
    private final int _combat_time;

    public FishTemplate(final int id, final int lvl, final String name, final int HP, final int HpRegen, final int type, final int group, final int fish_guts, final int guts_check_time, final int wait_time, final int combat_time) {
        _id = id;
        _level = lvl;
        _name = name.intern();
        _HP = HP;
        _HpRegen = HpRegen;
        _type = type;
        _group = group;
        _fish_guts = fish_guts;
        _guts_check_time = guts_check_time;
        _wait_time = wait_time;
        _combat_time = combat_time;
    }

    public int getId() {
        return _id;
    }

    public int getLevel() {
        return _level;
    }

    public String getName() {
        return _name;
    }

    public int getHP() {
        return _HP;
    }

    public int getHpRegen() {
        return _HpRegen;
    }

    public int getType() {
        return _type;
    }

    public int getGroup() {
        return _group;
    }

    public int getFishGuts() {
        return _fish_guts;
    }

    public int getGutsCheckTime() {
        return _guts_check_time;
    }

    public int getWaitTime() {
        return _wait_time;
    }

    public int getCombatTime() {
        return _combat_time;
    }
}
