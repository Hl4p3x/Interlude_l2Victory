package ru.j2dev.gameserver.model;

public class PetData {
    private int _id;
    private int _level;
    private int _feedMax;
    private int _feedBattle;
    private int _feedNormal;
    private int _pAtk;
    private int _pDef;
    private int _mAtk;
    private int _mDef;
    private int _hp;
    private int _mp;
    private int _hpRegen;
    private int _mpRegen;
    private long _exp;
    private int _accuracy;
    private int _evasion;
    private int _critical;
    private int _speed;
    private int _atkSpeed;
    private int _castSpeed;
    private int _maxLoad;
    private int _controlItemId;
    private int _foodId;
    private int _minLevel;
    private int _addFed;
    private boolean _isMountable;

    public int getFeedBattle() {
        return _feedBattle;
    }

    public void setFeedBattle(final int feedBattle) {
        _feedBattle = feedBattle;
    }

    public int getFeedNormal() {
        return _feedNormal;
    }

    public void setFeedNormal(final int feedNormal) {
        _feedNormal = feedNormal;
    }

    public int getHP() {
        return _hp;
    }

    public void setHP(final int petHP) {
        _hp = petHP;
    }

    public int getID() {
        return _id;
    }

    public void setID(final int petID) {
        _id = petID;
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(final int petLevel) {
        _level = petLevel;
    }

    public int getMAtk() {
        return _mAtk;
    }

    public void setMAtk(final int mAtk) {
        _mAtk = mAtk;
    }

    public int getFeedMax() {
        return _feedMax;
    }

    public void setFeedMax(final int feedMax) {
        _feedMax = feedMax;
    }

    public int getMDef() {
        return _mDef;
    }

    public void setMDef(final int mDef) {
        _mDef = mDef;
    }

    public long getExp() {
        return _exp;
    }

    public void setExp(final long exp) {
        _exp = exp;
    }

    public int getMP() {
        return _mp;
    }

    public void setMP(final int mp) {
        _mp = mp;
    }

    public int getPAtk() {
        return _pAtk;
    }

    public void setPAtk(final int pAtk) {
        _pAtk = pAtk;
    }

    public int getPDef() {
        return _pDef;
    }

    public void setPDef(final int pDef) {
        _pDef = pDef;
    }

    public int getAccuracy() {
        return _accuracy;
    }

    public void setAccuracy(final int accuracy) {
        _accuracy = accuracy;
    }

    public int getEvasion() {
        return _evasion;
    }

    public void setEvasion(final int evasion) {
        _evasion = evasion;
    }

    public int getCritical() {
        return _critical;
    }

    public void setCritical(final int critical) {
        _critical = critical;
    }

    public int getSpeed() {
        return _speed;
    }

    public void setSpeed(final int speed) {
        _speed = speed;
    }

    public int getAtkSpeed() {
        return _atkSpeed;
    }

    public void setAtkSpeed(final int atkSpeed) {
        _atkSpeed = atkSpeed;
    }

    public int getCastSpeed() {
        return _castSpeed;
    }

    public void setCastSpeed(final int castSpeed) {
        _castSpeed = castSpeed;
    }

    public int getMaxLoad() {
        return (_maxLoad != 0) ? _maxLoad : (_level * 300);
    }

    public void setMaxLoad(final int maxLoad) {
        _maxLoad = maxLoad;
    }

    public int getHpRegen() {
        return _hpRegen;
    }

    public void setHpRegen(final int hpRegen) {
        _hpRegen = hpRegen;
    }

    public int getMpRegen() {
        return _mpRegen;
    }

    public void setMpRegen(final int mpRegen) {
        _mpRegen = mpRegen;
    }

    public int getControlItemId() {
        return _controlItemId;
    }

    public void setControlItemId(final int itemId) {
        _controlItemId = itemId;
    }

    public int getFoodId() {
        return _foodId;
    }

    public void setFoodId(final int id) {
        _foodId = id;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public void setMinLevel(final int level) {
        _minLevel = level;
    }

    public int getAddFed() {
        return _addFed;
    }

    public void setAddFed(final int addFed) {
        _addFed = addFed;
    }

    public boolean isMountable() {
        return _isMountable;
    }

    public void setMountable(final boolean mountable) {
        _isMountable = mountable;
    }
}
