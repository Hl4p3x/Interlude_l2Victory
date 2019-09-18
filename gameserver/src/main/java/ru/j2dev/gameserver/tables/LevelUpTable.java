package ru.j2dev.gameserver.tables;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Creature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LevelUpTable {
    private static final Logger LOGGER = Logger.getLogger(LevelUpTable.class.getName());

    private int _maxLvl;
    private int _maxClassID;
    private double[] _hp_table;
    private double[] _cp_table;
    private double[] _mp_table;

    private LevelUpTable() {
        loadData();
    }

    public static LevelUpTable getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        protected static final LevelUpTable INSTANCE = new LevelUpTable();
    }

    private void loadData() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT MAX(`lvl`) FROM `lvlupgain`");
            if (rset.next()) {
                _maxLvl = rset.getInt(1);
            }
            DbUtils.closeQuietly(stmt, rset);
            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT MAX(`class_id`) FROM `lvlupgain`");
            if (rset.next()) {
                _maxClassID = rset.getInt(1);
            }
            DbUtils.closeQuietly(stmt, rset);
            final int max_idx = getIdx(_maxLvl, _maxClassID) + 1;
            _hp_table = new double[max_idx];
            _cp_table = new double[max_idx];
            _mp_table = new double[max_idx];
            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT `class_id`,`lvl`,`hp`,`cp`,`mp` FROM `lvlupgain`");
            while (rset.next()) {
                final int idx = getIdx(rset.getInt("lvl"), rset.getInt("class_id"));
                _hp_table[idx] = rset.getDouble("hp");
                _cp_table[idx] = rset.getDouble("cp");
                _mp_table[idx] = rset.getDouble("mp");
            }
        } catch (SQLException se) {
            LOGGER.log(Level.SEVERE, "Can't load lvlupgain table ", se);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rset);
        }
    }

    public double getMaxHP(final Creature character) {
        if (character.isPlayer()) {
            return _hp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
        }
        return character.getTemplate().getBaseHpMax();
    }

    public double getMaxCP(final Creature character) {
        if (character.isPlayer()) {
            return _cp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
        }
        return character.getTemplate().getBaseCpMax();
    }

    public double getMaxMP(final Creature character) {
        if (character.isPlayer()) {
            return _mp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
        }
        return character.getTemplate().getBaseMpMax();
    }

    private int getIdx(final int lvl, final int class_id) {
        return lvl << 8 | (class_id & 0xFF);
    }
}
