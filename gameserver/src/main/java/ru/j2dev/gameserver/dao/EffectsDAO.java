package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.dbutils.SqlBatch;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.tables.SkillTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class EffectsDAO {
    private static final int SUMMON_SKILL_OFFSET = 100000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectsDAO.class);


    public static EffectsDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void restoreEffects(final Player player) {
        int objectId = player.getObjectId();
        int id = player.getActiveClassId();
        Connection con = null;
        PreparedStatement statement;
        ResultSet rset;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `character_effects_save` WHERE `object_id`=? AND `id`=? ORDER BY `order` ASC");
            statement.setInt(1, objectId);
            statement.setInt(2, id);
            rset = statement.executeQuery();
            while (rset.next()) {
                final int skillId = rset.getInt("skill_id");
                final int skillLvl = rset.getInt("skill_level");
                final int effectCount = rset.getInt("effect_count");
                final long effectCurTime = rset.getLong("effect_cur_time");
                final long duration = rset.getLong("duration");
                final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (skill == null) {
                    continue;
                }
                for (final EffectTemplate et : skill.getEffectTemplates()) {
                    if (et != null) {
                        final Env env = new Env(player, player, skill);
                        final Effect effect = et.getEffect(env);
                        if (effect != null) {
                            if (!effect.isOneTime()) {
                                effect.setCount(effectCount);
                                effect.setPeriod((effectCount == 1) ? (duration - effectCurTime) : duration);
                                player.getEffectList().addEffect(effect);
                            }
                        }
                    }
                }
            }
            DbUtils.closeQuietly(statement, rset);
            statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id = ? AND id=?");
            statement.setInt(1, objectId);
            statement.setInt(2, id);
            statement.execute();
            DbUtils.close(statement);
        } catch (Exception e) {
            LOGGER.error("Could not restore active effects data!", e);
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    public void deleteEffects(final int objectId, final int skillId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id = ? AND id=?");
            statement.setInt(1, objectId);
            statement.setInt(2, SUMMON_SKILL_OFFSET + skillId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("Could not delete effects active effects data!" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void insert(final Player player) {
        int objectId = player.getObjectId();
        int id = player.getActiveClassId();
        final List<Effect> effects = player.getEffectList().getAllEffects();
        if (effects.isEmpty()) {
            return;
        }
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            int order = 0;
            final SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects_save` (`object_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`id`) VALUES");
            for (Effect effect : effects) {
                if (effect != null && effect.isInUse() && !effect.getSkill().isToggle() && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime) {
                    if (effect.isSaveable()) {
                        String sb = "(" + objectId + "," +
                                effect.getSkill().getId() + "," +
                                effect.getSkill().getLevel() + "," +
                                effect.getCount() + "," +
                                effect.getTime() + "," +
                                effect.getPeriod() + "," +
                                order + "," +
                                id + ")";
                        b.write(sb);
                    }
                    while ((effect = effect.getNext()) != null && effect.isSaveable()) {
                        String sb = "(" + objectId + "," +
                                effect.getSkill().getId() + "," +
                                effect.getSkill().getLevel() + "," +
                                effect.getCount() + "," +
                                effect.getTime() + "," +
                                effect.getPeriod() + "," +
                                order + "," +
                                id + ")";
                        b.write(sb);
                    }
                    ++order;
                }
            }
            if (!b.isEmpty()) {
                statement.executeUpdate(b.close());
            }
        } catch (Exception e) {
            LOGGER.error("Could not store active effects data!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final EffectsDAO INSTANCE = new EffectsDAO();
    }
}
