package ru.j2dev.gameserver.model.entity.residence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcEntity;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.EventHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.EventType;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Residence implements JdbcEntity {
    public static final long CYCLE_TIME = 3600000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Residence.class);

    protected final int _id;
    protected final String _name;
    protected final List<ResidenceFunction> _functions = new ArrayList<>();
    protected final List<Skill> _skills = new ArrayList<>();
    protected final Calendar _siegeDate = Calendar.getInstance();
    protected final Calendar _lastSiegeDate = Calendar.getInstance();
    protected final Calendar _ownDate = Calendar.getInstance();
    protected final List<Location> _banishPoints = new ArrayList<>();
    protected final List<Location> _ownerRestartPoints = new ArrayList<>();
    protected final List<Location> _otherRestartPoints = new ArrayList<>();
    protected final List<Location> _chaosRestartPoints = new ArrayList<>();
    protected Clan _owner;
    protected Zone _zone;
    protected SiegeEvent<?, ?> _siegeEvent;
    protected ScheduledFuture<?> _cycleTask;
    protected JdbcEntityState _jdbcEntityState = JdbcEntityState.CREATED;
    private int _cycle;
    private int _rewardCount;
    private int _paidCycle;

    public Residence(final StatsSet set) {
        _id = set.getInteger("id");
        _name = set.getString("name");
    }

    public abstract ResidenceType getType();

    public void init() {
        initZone();
        initEvent();
        loadData();
        loadFunctions();
        rewardSkills();
        startCycleTask();
    }

    protected void initZone() {
        (_zone = ReflectionUtils.getZone("residence_" + _id)).setParam("residence", this);
    }

    protected void initEvent() {
        _siegeEvent = EventHolder.getInstance().getEvent(EventType.SIEGE_EVENT, _id);
    }

    @SuppressWarnings("unchecked")
    public <E extends SiegeEvent> E getSiegeEvent() {
        return (E) _siegeEvent;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public int getOwnerId() {
        return (_owner == null) ? 0 : _owner.getClanId();
    }

    public Clan getOwner() {
        return _owner;
    }

    public Zone getZone() {
        return _zone;
    }

    protected abstract void loadData();

    public abstract void changeOwner(final Clan p0);

    public Calendar getOwnDate() {
        return _ownDate;
    }

    public Calendar getSiegeDate() {
        return _siegeDate;
    }

    public Calendar getLastSiegeDate() {
        return _lastSiegeDate;
    }

    public void addSkill(final Skill skill) {
        _skills.add(skill);
    }

    public void addFunction(final ResidenceFunction function) {
        _functions.add(function);
    }

    public boolean checkIfInZone(final Location loc, final Reflection ref) {
        return checkIfInZone(loc.x, loc.y, loc.z, ref);
    }

    public boolean checkIfInZone(final int x, final int y, final int z, final Reflection ref) {
        return getZone() != null && getZone().checkIfInZone(x, y, z, ref);
    }

    public void banishForeigner() {
        _zone.getInsidePlayers().stream().filter(player -> player.getClanId() != getOwnerId()).forEach(player -> player.teleToLocation(getBanishPoint()));
    }

    public void rewardSkills() {
        final Clan owner = getOwner();
        if (owner != null) {
            _skills.forEach(skill -> {
                owner.addSkill(skill, false);
                owner.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));
            });
        }
    }

    public void removeSkills() {
        final Clan owner = getOwner();
        if (owner != null) {
            _skills.stream().mapToInt(Skill::getId).forEach(owner::removeSkill);
        }
    }

    protected void loadFunctions() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM residence_functions WHERE id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();
            while (rs.next()) {
                final ResidenceFunction function = getFunction(rs.getInt("type"));
                function.setLvl(rs.getInt("lvl"));
                function.setEndTimeInMillis(rs.getInt("endTime") * 1000L);
                function.setInDebt(rs.getBoolean("inDebt"));
                function.setActive(true);
                startAutoTaskForFunction(function);
            }
        } catch (Exception e) {
            LOGGER.warn("Residence: loadFunctions(): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    public boolean isFunctionActive(final int type) {
        final ResidenceFunction function = getFunction(type);
        return function != null && function.isActive() && function.getLevel() > 0;
    }

    public ResidenceFunction getFunction(final int type) {
        return _functions.stream().filter(_function -> _function.getType() == type).findFirst().orElse(null);
    }

    public boolean updateFunctions(final int type, final int level) {
        final Clan clan = getOwner();
        if (clan == null) {
            return false;
        }
        final long count = clan.getAdenaCount();
        final ResidenceFunction function = getFunction(type);
        if (function == null) {
            return false;
        }
        if (function.isActive() && function.getLevel() == level) {
            return true;
        }
        final int lease = (level == 0) ? 0 : getFunction(type).getLease(level);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            if (!function.isActive()) {
                if (count < lease) {
                    return false;
                }
                clan.getWarehouse().destroyItemByItemId(57, lease);
                final long time = Calendar.getInstance().getTimeInMillis() + 86400000L;
                statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?, endTime=?");
                statement.setInt(1, getId());
                statement.setInt(2, type);
                statement.setInt(3, level);
                statement.setInt(4, (int) (time / 1000L));
                statement.execute();
                function.setLvl(level);
                function.setEndTimeInMillis(time);
                function.setActive(true);
                startAutoTaskForFunction(function);
            } else {
                if (count < lease - getFunction(type).getLease()) {
                    return false;
                }
                if (lease > getFunction(type).getLease()) {
                    clan.getWarehouse().destroyItemByItemId(57, lease - getFunction(type).getLease());
                }
                statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?");
                statement.setInt(1, getId());
                statement.setInt(2, type);
                statement.setInt(3, level);
                statement.execute();
                function.setLvl(level);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception: SiegeUnit.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return true;
    }

    public void removeFunction(final int type) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM residence_functions WHERE id=? AND type=?");
            statement.setInt(1, getId());
            statement.setInt(2, type);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("Exception: removeFunctions(int type): " + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void startAutoTaskForFunction(final ResidenceFunction function) {
        if (getOwnerId() == 0) {
            return;
        }
        final Clan clan = getOwner();
        if (clan == null) {
            return;
        }
        if (function.getEndTimeInMillis() > System.currentTimeMillis()) {
            ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
        } else if (function.isInDebt() && clan.getAdenaCount() >= function.getLease()) {
            clan.getWarehouse().destroyItemByItemId(57, function.getLease());
            function.updateRentTime(false);
            ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
        } else if (!function.isInDebt()) {
            function.setInDebt(true);
            function.updateRentTime(true);
            ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
        } else {
            function.setLvl(0);
            function.setActive(false);
            removeFunction(function.getType());
        }
    }

    @Override
    public JdbcEntityState getJdbcState() {
        return _jdbcEntityState;
    }

    @Override
    public void setJdbcState(final JdbcEntityState state) {
        _jdbcEntityState = state;
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    public void cancelCycleTask() {
        _cycle = 0;
        _paidCycle = 0;
        _rewardCount = 0;
        if (_cycleTask != null) {
            _cycleTask.cancel(false);
            _cycleTask = null;
        }
        setJdbcState(JdbcEntityState.UPDATED);
    }

    public void startCycleTask() {
        if (_owner == null) {
            return;
        }
        final long ownedTime = getOwnDate().getTimeInMillis();
        if (ownedTime == 0L) {
            return;
        }
        long diff;
        for (diff = System.currentTimeMillis() - ownedTime; diff >= CYCLE_TIME; diff -= CYCLE_TIME) {
        }
        _cycleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ResidenceCycleTask(), diff, CYCLE_TIME);
    }

    public void chanceCycle() {
        setCycle(getCycle() + 1);
        setJdbcState(JdbcEntityState.UPDATED);
    }

    public List<Skill> getSkills() {
        return _skills;
    }

    public void addBanishPoint(final Location loc) {
        _banishPoints.add(loc);
    }

    public void addOwnerRestartPoint(final Location loc) {
        _ownerRestartPoints.add(loc);
    }

    public void addOtherRestartPoint(final Location loc) {
        _otherRestartPoints.add(loc);
    }

    public void addChaosRestartPoint(final Location loc) {
        _chaosRestartPoints.add(loc);
    }

    public Location getBanishPoint() {
        if (_banishPoints.isEmpty()) {
            return null;
        }
        return _banishPoints.get(Rnd.get(_banishPoints.size()));
    }

    public Location getOwnerRestartPoint() {
        if (_ownerRestartPoints.isEmpty()) {
            return null;
        }
        return _ownerRestartPoints.get(Rnd.get(_ownerRestartPoints.size()));
    }

    public Location getOtherRestartPoint() {
        if (_otherRestartPoints.isEmpty()) {
            return null;
        }
        return _otherRestartPoints.get(Rnd.get(_otherRestartPoints.size()));
    }

    public Location getChaosRestartPoint() {
        if (_chaosRestartPoints.isEmpty()) {
            return null;
        }
        return _chaosRestartPoints.get(Rnd.get(_chaosRestartPoints.size()));
    }

    public Location getNotOwnerRestartPoint(final Player player) {
        return (player.getKarma() > 0) ? getChaosRestartPoint() : getOtherRestartPoint();
    }

    public int getCycle() {
        return _cycle;
    }

    public void setCycle(final int cycle) {
        _cycle = cycle;
    }

    public long getCycleDelay() {
        if (_cycleTask == null) {
            return 0L;
        }
        return _cycleTask.getDelay(TimeUnit.SECONDS);
    }

    public int getPaidCycle() {
        return _paidCycle;
    }

    public void setPaidCycle(final int paidCycle) {
        _paidCycle = paidCycle;
    }

    public int getRewardCount() {
        return _rewardCount;
    }

    public void setRewardCount(final int rewardCount) {
        _rewardCount = rewardCount;
    }

    public class ResidenceCycleTask extends RunnableImpl {
        @Override
        public void runImpl() {
            chanceCycle();
            update();
        }
    }

    private class AutoTaskForFunctions extends RunnableImpl {
        final ResidenceFunction _function;

        public AutoTaskForFunctions(final ResidenceFunction function) {
            _function = function;
        }

        @Override
        public void runImpl() {
            startAutoTaskForFunction(_function);
        }
    }
}
