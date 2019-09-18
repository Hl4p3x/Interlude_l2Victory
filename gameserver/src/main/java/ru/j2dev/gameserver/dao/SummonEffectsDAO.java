package ru.j2dev.gameserver.dao;

import com.stringer.annotations.HideAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.dbutils.SqlBatch;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.tables.SkillTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by JunkyFunky
 * on 04.07.2018 21:15
 * group j2dev
 */
@HideAccess
public class SummonEffectsDAO {
    private static final int SUMMON_SKILL_OFFSET = 100000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectsDAO.class);


    public static SummonEffectsDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void restoreEffects(final Summon playable) {
        int objectId = playable.getPlayer().getObjectId();
        int id = playable.getEffectIdentifier() + SUMMON_SKILL_OFFSET;
        Connection con = null;
        PreparedStatement statement;
        ResultSet rset;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `summon_effects_save` WHERE `object_id`=? AND `id`=? ORDER BY `order` ASC");
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
                        final Env env = new Env(playable, playable, skill);
                        final Effect effect = et.getEffect(env);
                        if (effect != null) {
                            if (!effect.isOneTime()) {
                                effect.setCount(effectCount);
                                effect.setPeriod((effectCount == 1) ? (duration - effectCurTime) : duration);
                                playable.getEffectList().addEffect(effect);
                            }
                        }
                    }
                }
            }
            DbUtils.closeQuietly(statement, rset);
            statement = con.prepareStatement("DELETE FROM summon_effects_save WHERE object_id = ? AND id=?");
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
            statement = con.prepareStatement("DELETE FROM summon_effects_save WHERE object_id = ? AND id=?");
            statement.setInt(1, objectId);
            statement.setInt(2, SUMMON_SKILL_OFFSET + skillId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("Could not delete effects active effects data!" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void insert(final Summon summon) {
        int objectId = summon.getPlayer().getObjectId();
        int id = summon.getEffectIdentifier() + SUMMON_SKILL_OFFSET;
        if (!summon.isSummon()) {
            return;
        }
        final List<Effect> effects = summon.getEffectList().getAllEffects();
        if (effects.isEmpty()) {
            return;
        }
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            int order = 0;
            final SqlBatch b = new SqlBatch("INSERT IGNORE INTO `summon_effects_save` (`object_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`id`) VALUES");
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
                    order++;
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
        private static final SummonEffectsDAO INSTANCE = new SummonEffectsDAO();
    }
}
