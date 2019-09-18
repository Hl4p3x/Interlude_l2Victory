package ru.j2dev.gameserver.tables;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.Summon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PetSkillsTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetSkillsTable.class);
    private static PetSkillsTable _instance = new PetSkillsTable();

    private final TIntObjectHashMap<List<SkillLearn>> _skillTrees = new TIntObjectHashMap<>();

    private PetSkillsTable() {
        load();
    }

    public static PetSkillsTable getInstance() {
        return _instance;
    }

    public void reload() {
        _instance = new PetSkillsTable();
    }

    private void load() {
        int npcId = 0;
        int count = 0;
        int id;
        int lvl;
        int minLvl;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM pets_skills ORDER BY templateId");
            rset = statement.executeQuery();
            while (rset.next()) {
                npcId = rset.getInt("templateId");
                id = rset.getInt("skillId");
                lvl = rset.getInt("skillLvl");
                minLvl = rset.getInt("minLvl");
                List<SkillLearn> list = _skillTrees.get(npcId);
                if (list == null) {
                    _skillTrees.put(npcId, list = new ArrayList<>());
                }
                final SkillLearn skillLearn = new SkillLearn(id, lvl, minLvl, 0, 0, 0L, false, false);
                list.add(skillLearn);
                ++count;
            }
        } catch (Exception e) {
            LOGGER.error("Error while creating pet skill tree (Pet ID " + npcId + ")", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        LOGGER.info("PetSkillsTable: Loaded " + count + " skills.");
    }

    public int getAvailableLevel(final Summon cha, final int skillId) {
        final List<SkillLearn> skills = _skillTrees.get(cha.getNpcId());
        if (skills == null) {
            return 0;
        }
        int lvl = 0;
        for (final SkillLearn temp : skills) {
            if (temp.getId() != skillId) {
                continue;
            }
            if (temp.getLevel() == 0) {
                if (cha.getLevel() < 70) {
                    lvl = cha.getLevel() / 10;
                    if (lvl <= 0) {
                        lvl = 1;
                    }
                } else {
                    lvl = 7 + (cha.getLevel() - 70) / 5;
                }
                final int maxLvl = SkillTable.getInstance().getMaxLevel(temp.getId());
                if (lvl > maxLvl) {
                    lvl = maxLvl;
                    break;
                }
                break;
            } else {
                if (temp.getMinLevel() > cha.getLevel() || temp.getLevel() <= lvl) {
                    continue;
                }
                lvl = temp.getLevel();
            }
        }
        return lvl;
    }
}
