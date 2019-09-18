package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.dao.ItemsDAO;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

import java.util.Collection;

public class CharSelectInfoPackage {
    private final ItemInstance[] _paperdoll;
    private String _name;
    private int _objectId;
    private int _charId;
    private long _exp;
    private int _sp;
    private int _clanId;
    private int _race;
    private int _classId;
    private int _baseClassId;
    private int _deleteTimer;
    private long _lastAccess;
    private int _face;
    private int _hairStyle;
    private int _hairColor;
    private int _sex;
    private int _level;
    private int _karma;
    private int _pk;
    private int _pvp;
    private int _maxHp;
    private double _currentHp;
    private int _maxMp;
    private double _currentMp;
    private int _accesslevel;
    private int _x;
    private int _y;
    private int _z;
    private int _vitalityPoints;

    public CharSelectInfoPackage(final int objectId, final String name) {
        _objectId = 0;
        _charId = 199546;
        _exp = 0L;
        _sp = 0;
        _clanId = 0;
        _race = 0;
        _classId = 0;
        _baseClassId = 0;
        _deleteTimer = 0;
        _lastAccess = 0L;
        _face = 0;
        _hairStyle = 0;
        _hairColor = 0;
        _sex = 0;
        _level = 1;
        _karma = 0;
        _pk = 0;
        _pvp = 0;
        _maxHp = 0;
        _currentHp = 0.0;
        _maxMp = 0;
        _currentMp = 0.0;
        _accesslevel = 0;
        _x = 0;
        _y = 0;
        _z = 0;
        _vitalityPoints = 20000;
        setObjectId(objectId);
        _name = name;
        final Collection<ItemInstance> items = ItemsDAO.getInstance().loadItemsByOwnerIdAndLoc(objectId, ItemLocation.PAPERDOLL);
        _paperdoll = new ItemInstance[17];
        items.stream().filter(item -> item.getEquipSlot() < 17).forEach(item -> _paperdoll[item.getEquipSlot()] = item);
    }

    public int getObjectId() {
        return _objectId;
    }

    public void setObjectId(final int objectId) {
        _objectId = objectId;
    }

    public int getCharId() {
        return _charId;
    }

    public void setCharId(final int charId) {
        _charId = charId;
    }

    public int getClanId() {
        return _clanId;
    }

    public void setClanId(final int clanId) {
        _clanId = clanId;
    }

    public int getClassId() {
        return _classId;
    }

    public void setClassId(final int classId) {
        _classId = classId;
    }

    public int getBaseClassId() {
        return _baseClassId;
    }

    public void setBaseClassId(final int baseClassId) {
        _baseClassId = baseClassId;
    }

    public double getCurrentHp() {
        return _currentHp;
    }

    public void setCurrentHp(final double currentHp) {
        _currentHp = currentHp;
    }

    public double getCurrentMp() {
        return _currentMp;
    }

    public void setCurrentMp(final double currentMp) {
        _currentMp = currentMp;
    }

    public int getDeleteTimer() {
        return _deleteTimer;
    }

    public void setDeleteTimer(final int deleteTimer) {
        _deleteTimer = deleteTimer;
    }

    public long getLastAccess() {
        return _lastAccess;
    }

    public void setLastAccess(final long lastAccess) {
        _lastAccess = lastAccess;
    }

    public long getExp() {
        return _exp;
    }

    public void setExp(final long exp) {
        _exp = exp;
    }

    public int getFace() {
        return _face;
    }

    public void setFace(final int face) {
        _face = face;
    }

    public int getHairColor() {
        return _hairColor;
    }

    public void setHairColor(final int hairColor) {
        _hairColor = hairColor;
    }

    public int getHairStyle() {
        return _hairStyle;
    }

    public void setHairStyle(final int hairStyle) {
        _hairStyle = hairStyle;
    }

    public int getPaperdollObjectId(final int slot) {
        final ItemInstance item = _paperdoll[slot];
        if (item != null) {
            return item.getObjectId();
        }
        return 0;
    }

    public int getPaperdollAugmentationId(final int slot) {
        final ItemInstance item = _paperdoll[slot];
        if (item != null && item.isAugmented()) {
            return (item.getVariationStat1() & 0xFFFF) | item.getVariationStat2() << 16;
        }
        return 0;
    }

    public int getPaperdollItemId(final int slot) {
        final ItemInstance item = _paperdoll[slot];
        if (item != null) {
            return item.getItemId();
        }
        return 0;
    }

    public int getPaperdollEnchantEffect(final int slot) {
        final ItemInstance item = _paperdoll[slot];
        if (item != null) {
            return item.getEnchantLevel();
        }
        return 0;
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(final int level) {
        _level = level;
    }

    public int getMaxHp() {
        return _maxHp;
    }

    public void setMaxHp(final int maxHp) {
        _maxHp = maxHp;
    }

    public int getMaxMp() {
        return _maxMp;
    }

    public void setMaxMp(final int maxMp) {
        _maxMp = maxMp;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public int getRace() {
        return _race;
    }

    public void setRace(final int race) {
        _race = race;
    }

    public int getSex() {
        return _sex;
    }

    public void setSex(final int sex) {
        _sex = sex;
    }

    public int getSp() {
        return _sp;
    }

    public void setSp(final int sp) {
        _sp = sp;
    }

    public int getKarma() {
        return _karma;
    }

    public void setKarma(final int karma) {
        _karma = karma;
    }

    public int getAccessLevel() {
        return _accesslevel;
    }

    public void setAccessLevel(final int accesslevel) {
        _accesslevel = accesslevel;
    }

    public int getX() {
        return _x;
    }

    public void setX(final int x) {
        _x = x;
    }

    public int getY() {
        return _y;
    }

    public void setY(final int y) {
        _y = y;
    }

    public int getZ() {
        return _z;
    }

    public void setZ(final int z) {
        _z = z;
    }

    public int getPk() {
        return _pk;
    }

    public void setPk(final int pk) {
        _pk = pk;
    }

    public int getPvP() {
        return _pvp;
    }

    public void setPvP(final int pvp) {
        _pvp = pvp;
    }

    public int getVitalityPoints() {
        return _vitalityPoints;
    }

    public void setVitalityPoints(final int points) {
        _vitalityPoints = points;
    }
}
