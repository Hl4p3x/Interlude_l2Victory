package ru.j2dev.gameserver.model.entity.residence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.TeleportLocation;
import ru.j2dev.gameserver.tables.SkillTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

public class ResidenceFunction {
    public static final int TELEPORT = 1;
    public static final int ITEM_CREATE = 2;
    public static final int RESTORE_HP = 3;
    public static final int RESTORE_MP = 4;
    public static final int RESTORE_EXP = 5;
    public static final int SUPPORT = 6;
    public static final int CURTAIN = 7;
    public static final int PLATFORM = 8;
    public static final String A = "";
    public static final String W = "W";
    public static final String M = "M";
    private static final Logger LOGGER = LoggerFactory.getLogger(ResidenceFunction.class);
    private static final Object[][][] buffs_template = {{ // level 0 - no buff
    }, {
            // level 1
            {getSkillInfo(4342, 1), A}, {getSkillInfo(4343, 1), A}, {getSkillInfo(4344, 1), A}, {getSkillInfo(4346, 1), A}, {getSkillInfo(4345, 1), W},}, {
            // level 2
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W},}, {
            // level 3
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W},}, {
            // level 4
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W}, {getSkillInfo(4347, 2), A}, {getSkillInfo(4349, 1), A}, {getSkillInfo(4350, 1), W}, {getSkillInfo(4348, 2), A},}, {
            // level 5
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W}, {getSkillInfo(4347, 2), A}, {getSkillInfo(4349, 1), A}, {getSkillInfo(4350, 1), W}, {getSkillInfo(4348, 2), A}, {getSkillInfo(4351, 2), M}, {getSkillInfo(4352, 1), A}, {getSkillInfo(4353, 2), W}, {getSkillInfo(4358, 1), W}, {getSkillInfo(4354, 1), W},}, { // level 6 - unused
    }, {
            // level 7
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W}, {getSkillInfo(4347, 6), A}, {getSkillInfo(4349, 2), A}, {getSkillInfo(4350, 4), W}, {getSkillInfo(4348, 6), A}, {getSkillInfo(4351, 6), M}, {getSkillInfo(4352, 2), A}, {getSkillInfo(4353, 6), W}, {getSkillInfo(4358, 3), W}, {getSkillInfo(4354, 4), W},}, {
            // level 8
            {getSkillInfo(4342, 2), A}, {getSkillInfo(4343, 3), A}, {getSkillInfo(4344, 3), A}, {getSkillInfo(4346, 4), A}, {getSkillInfo(4345, 3), W}, {getSkillInfo(4347, 6), A}, {getSkillInfo(4349, 2), A}, {getSkillInfo(4350, 4), W}, {getSkillInfo(4348, 6), A}, {getSkillInfo(4351, 6), M}, {getSkillInfo(4352, 2), A}, {getSkillInfo(4353, 6), W}, {getSkillInfo(4358, 3), W}, {getSkillInfo(4354, 4), W}, {getSkillInfo(4355, 1), M}, {getSkillInfo(4356, 1), M}, {getSkillInfo(4357, 1), W}, {getSkillInfo(4359, 1), W}, {getSkillInfo(4360, 1), W},}, { // level 9 - unused
    }, { // level 10 - unused
    }, {
            // level 11
            {getSkillInfo(4342, 3), A}, {getSkillInfo(4343, 4), A}, {getSkillInfo(4344, 4), A}, {getSkillInfo(4346, 5), A}, {getSkillInfo(4345, 4), W},}, {
            // level 12
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W},}, {
            // level 13
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W},}, {
            // level 14
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W}, {getSkillInfo(4347, 8), A}, {getSkillInfo(4349, 3), A}, {getSkillInfo(4350, 5), W}, {getSkillInfo(4348, 8), A},}, {
            // level 15
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W}, {getSkillInfo(4347, 8), A}, {getSkillInfo(4349, 3), A}, {getSkillInfo(4350, 5), W}, {getSkillInfo(4348, 8), A}, {getSkillInfo(4351, 8), M}, {getSkillInfo(4352, 3), A}, {getSkillInfo(4353, 8), W}, {getSkillInfo(4358, 4), W}, {getSkillInfo(4354, 5), W},}, { // level 16 - unused
    }, {
            // level 17
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W}, {getSkillInfo(4347, 12), A}, {getSkillInfo(4349, 4), A}, {getSkillInfo(4350, 8), W}, {getSkillInfo(4348, 12), A}, {getSkillInfo(4351, 12), M}, {getSkillInfo(4352, 4), A}, {getSkillInfo(4353, 12), W}, {getSkillInfo(4358, 6), W}, {getSkillInfo(4354, 8), W},}, {
            // level 18
            {getSkillInfo(4342, 4), A}, {getSkillInfo(4343, 6), A}, {getSkillInfo(4344, 6), A}, {getSkillInfo(4346, 8), A}, {getSkillInfo(4345, 6), W}, {getSkillInfo(4347, 12), A}, {getSkillInfo(4349, 4), A}, {getSkillInfo(4350, 8), W}, {getSkillInfo(4348, 12), A}, {getSkillInfo(4351, 12), M}, {getSkillInfo(4352, 4), A}, {getSkillInfo(4353, 12), W}, {getSkillInfo(4358, 6), W}, {getSkillInfo(4354, 8), W}, {getSkillInfo(4355, 4), M}, {getSkillInfo(4356, 4), M}, {getSkillInfo(4357, 3), W}, {getSkillInfo(4359, 4), W}, {getSkillInfo(4360, 4), W},},};

    private final int _id;
    private final int _type;
    private final Calendar _endDate;
    private final Map<Integer, Integer> _leases = new ConcurrentSkipListMap<>();
    private final Map<Integer, TeleportLocation[]> _teleports = new ConcurrentSkipListMap<>();
    private final Map<Integer, int[]> _buylists = new ConcurrentSkipListMap<>();
    private final Map<Integer, Object[][]> _buffs = new ConcurrentSkipListMap<>();
    private int _level;
    private boolean _inDebt;
    private boolean _active;

    public ResidenceFunction(final int id, final int type) {
        _id = id;
        _type = type;
        _endDate = Calendar.getInstance();
    }

    private static Skill getSkillInfo(final int id, final int level) {
        return SkillTable.getInstance().getInfo(id, level);
    }

    public int getResidenceId() {
        return _id;
    }

    public int getType() {
        return _type;
    }

    public int getLevel() {
        return _level;
    }

    public void setLvl(final int lvl) {
        _level = lvl;
    }

    public long getEndTimeInMillis() {
        return _endDate.getTimeInMillis();
    }

    public void setEndTimeInMillis(final long time) {
        _endDate.setTimeInMillis(time);
    }

    public boolean isInDebt() {
        return _inDebt;
    }

    public void setInDebt(final boolean inDebt) {
        _inDebt = inDebt;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean active) {
        _active = active;
    }

    public void updateRentTime(final boolean inDebt) {
        setEndTimeInMillis(System.currentTimeMillis() + 86400000L);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE residence_functions SET endTime=?, inDebt=? WHERE type=? AND id=?");
            statement.setInt(1, (int) (getEndTimeInMillis() / 1000L));
            statement.setInt(2, inDebt ? 1 : 0);
            statement.setInt(3, getType());
            statement.setInt(4, getResidenceId());
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public TeleportLocation[] getTeleports() {
        return getTeleports(_level);
    }

    public TeleportLocation[] getTeleports(final int level) {
        return _teleports.get(level);
    }

    public void addTeleports(final int level, final TeleportLocation[] teleports) {
        _teleports.put(level, teleports);
    }

    public int getLease() {
        if (_level == 0) {
            return 0;
        }
        return getLease(_level);
    }

    public int getLease(final int level) {
        return _leases.get(level);
    }

    public void addLease(final int level, final int lease) {
        _leases.put(level, lease);
    }

    public int[] getBuylist() {
        return getBuylist(_level);
    }

    public int[] getBuylist(final int level) {
        return _buylists.get(level);
    }

    public void addBuylist(final int level, final int[] buylist) {
        _buylists.put(level, buylist);
    }

    public Object[][] getBuffs() {
        return getBuffs(_level);
    }

    public Object[][] getBuffs(final int level) {
        return _buffs.get(level);
    }

    public void addBuffs(final int level) {
        _buffs.put(level, buffs_template[level]);
    }

    public Set<Integer> getLevels() {
        return _leases.keySet();
    }
}
