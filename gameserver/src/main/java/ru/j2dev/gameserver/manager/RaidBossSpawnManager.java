package ru.j2dev.gameserver.manager;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.dbutils.SqlBatch;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.instances.ReflectionBossInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.tables.GmListTable;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RaidBossSpawnManager {
    public static final Integer KEY_RANK = -1;
    public static final Integer KEY_TOTAL_POINTS = 0;
    protected static final Map<Integer, Spawner> _spawntable = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RaidBossSpawnManager.class);
    protected static Map<Integer, StatsSet> _storedInfo;
    protected static Map<Integer, Map<Integer, Integer>> _points;

    private final Lock pointsLock = new ReentrantLock();

    private RaidBossSpawnManager() {
        if (!Config.DONTLOADSPAWN) {
            reloadBosses();
        }
    }

    public static RaidBossSpawnManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static void addRespawnAnnounce(final int npcId, final long respawnDelay) {
        if (Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY > 0 && respawnDelay > 0L && ArrayUtils.contains(Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_IDS, npcId)) {
            final long now = System.currentTimeMillis() / 1000L;
            if (respawnDelay - Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY > now) {
                ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        final NpcTemplate npcTemplate = NpcTemplateHolder.getInstance().getTemplate(npcId);
                        if (npcTemplate != null) {
                            Announcements.getInstance().announceByCustomMessage("ru.j2dev.gameserver.instancemanager.RaidBossSpawnManager.AltAnnounceRaidbossSpawnSoon", new String[]{npcTemplate.getName()});
                        }
                    }
                }, (respawnDelay - Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY - now) * 1000L);
            }
        }
    }

    public void reloadBosses() {
        loadStatus();
        restorePointsTable();
        calculateRanking();
    }

    public void cleanUp() {
        updateAllStatusDb();
        updatePointsDb();
        _storedInfo.clear();
        _spawntable.clear();
        _points.clear();
    }

    private void loadStatus() {
        _storedInfo = new ConcurrentHashMap<>();
        Connection con = null;
        final Statement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
            while (rset.next()) {
                final int id = rset.getInt("id");
                final StatsSet info = new StatsSet();
                info.set("current_hp", rset.getDouble("current_hp"));
                info.set("current_mp", rset.getDouble("current_mp"));
                info.set("respawn_delay", rset.getInt("respawn_delay"));
                _storedInfo.put(id, info);
            }
        } catch (Exception e) {
            LOGGER.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        LOGGER.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
    }

    public void updateAllStatusDb() {
        _storedInfo.keySet().forEach(this::updateStatusDb);
    }

    private void updateStatusDb(final int id) {
        final Spawner spawner = _spawntable.get(id);
        if (spawner == null) {
            return;
        }
        StatsSet info = _storedInfo.computeIfAbsent(id, k -> new StatsSet());
        final NpcInstance raidboss = spawner.getFirstSpawned();
        if (raidboss instanceof ReflectionBossInstance) {
            return;
        }
        int respawnDelay = 0;
        if (raidboss != null && !raidboss.isDead()) {
            info.set("current_hp", raidboss.getCurrentHp());
            info.set("current_mp", raidboss.getCurrentMp());
            info.set("respawn_delay", 0);
        } else {
            respawnDelay = spawner.getRespawnTime();
            info.set("current_hp", 0);
            info.set("current_mp", 0);
            info.set("respawn_delay", respawnDelay);
            addRespawnAnnounce(id, respawnDelay);
        }
        //Log.add("updateStatusDb id=" + id + " current_hp=" + info.getDouble("current_hp") + " current_mp=" + info.getDouble("current_mp") + " respawn_delay=" + info.getInteger("respawn_delay", 0) + ((raidboss != null) ? (" respawnTime=" + raidboss.getSpawn().getRespawnTime()) : ""), "RaidBossSpawnManager");
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
            statement.setInt(1, id);
            statement.setInt(2, (int) info.getDouble("current_hp"));
            statement.setInt(3, (int) info.getDouble("current_mp"));
            statement.setInt(4, respawnDelay);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void addNewSpawn(final int npcId, final Spawner spawnDat) {
        if (_spawntable.containsKey(npcId)) {
            return;
        }
        _spawntable.put(npcId, spawnDat);
        final StatsSet info = _storedInfo.get(npcId);
        if (info != null) {
            final long respawnTime = info.getLong("respawn_delay", 0L);
            spawnDat.setRespawnTime((int) respawnTime);
            //Log.add("AddSpawn npc=" + npcId + " respawnDelay=" + spawnDat.getRespawnDelay() + " respawnDelayRandom=" + spawnDat.getRespawnDelayRandom() + " respawnCron=" + spawnDat.getRespawnCron() + " respawn_delay=" + respawnTime, "RaidBossSpawnManager");
            if (respawnTime > 0L) {
                addRespawnAnnounce(npcId, respawnTime);
            }
        }
    }

    public void onBossSpawned(final RaidBossInstance raidboss) {
        final int bossId = raidboss.getNpcId();
        if (!_spawntable.containsKey(bossId)) {
            return;
        }
        final StatsSet info = _storedInfo.get(bossId);
        if (info != null && info.getDouble("current_hp") > 1.0) {
            raidboss.setCurrentHp(info.getDouble("current_hp"), false);
            raidboss.setCurrentMp(info.getDouble("current_mp"));
        }
        //Log.add("onBossSpawned npc=" + bossId + " current_hp=" + raidboss.getCurrentHp() + " current_mp=" + raidboss.getCurrentMp(), "RaidBossSpawnManager");
        GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());
        if (ArrayUtils.contains(Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_IDS, raidboss.getNpcId())) {
            Announcements.getInstance().announceByCustomMessage("ru.j2dev.gameserver.instancemanager.RaidBossSpawnManager.AltAnnounceRaidbossSpawn", new String[]{raidboss.getName()});
        }
    }

    public void onBossDespawned(final RaidBossInstance raidboss) {
        updateStatusDb(raidboss.getNpcId());
    }

    public Status getRaidBossStatusId(final int bossId) {
        final Spawner spawner = _spawntable.get(bossId);
        if (spawner == null) {
            return Status.UNDEFINED;
        }
        final NpcInstance npc = spawner.getFirstSpawned();
        return (npc == null) ? Status.DEAD : Status.ALIVE;
    }

    public boolean isDefined(final int bossId) {
        return _spawntable.containsKey(bossId);
    }

    public Map<Integer, Spawner> getSpawnTable() {
        return _spawntable;
    }

    private void restorePointsTable() {
        pointsLock.lock();
        _points = new ConcurrentHashMap<>();
        Connection con = null;
        Statement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            rset = statement.executeQuery("SELECT owner_id, boss_id, points FROM `raidboss_points` ORDER BY owner_id ASC");
            int currentOwner = 0;
            Map<Integer, Integer> score = null;
            while (rset.next()) {
                if (currentOwner != rset.getInt("owner_id")) {
                    currentOwner = rset.getInt("owner_id");
                    score = new HashMap<>();
                    _points.put(currentOwner, score);
                }
                assert score != null;
                final int bossId = rset.getInt("boss_id");
                final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(bossId);
                if (bossId == KEY_RANK || bossId == KEY_TOTAL_POINTS || template == null || template.rewardRp <= 0) {
                    continue;
                }
                score.put(bossId, rset.getInt("points"));
            }
        } catch (Exception e) {
            LOGGER.warn("RaidBossSpawnManager: Couldnt load raidboss points");
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        pointsLock.unlock();
    }

    public void updatePointsDb() {
        pointsLock.lock();
        if (!mysql.set("TRUNCATE `raidboss_points`")) {
            LOGGER.warn("RaidBossSpawnManager: Couldnt empty raidboss_points table");
        }
        if (_points.isEmpty()) {
            pointsLock.unlock();
            return;
        }
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            final SqlBatch b = new SqlBatch("INSERT INTO `raidboss_points` (owner_id, boss_id, points) VALUES");
            _points.forEach((key, tmpPoint) -> {
                if (tmpPoint != null) {
                    if (tmpPoint.isEmpty()) {
                        return;
                    }
                    for (final Entry<Integer, Integer> pointListEntry : tmpPoint.entrySet()) {
                        if (!KEY_RANK.equals(pointListEntry.getKey()) && !KEY_TOTAL_POINTS.equals(pointListEntry.getKey()) && pointListEntry.getValue() != null) {
                            if (pointListEntry.getValue() == 0) {
                                return;
                            }
                            String sb = "(" + key + "," +
                                    pointListEntry.getKey() + "," +
                                    pointListEntry.getValue() + ")";
                            b.write(sb);
                        }
                    }
                }
            });
            if (!b.isEmpty()) {
                statement.executeUpdate(b.close());
            }
        } catch (SQLException e) {
            LOGGER.warn("RaidBossSpawnManager: Couldnt update raidboss_points table");
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        pointsLock.unlock();
    }

    public void deletePoints(final int ownerId) {
        if (ownerId <= 0) {
            return;
        }
        pointsLock.lock();
        try {
            _points.remove(ownerId);
        } finally {
            pointsLock.unlock();
        }
    }

    public void addPoints(final int ownerId, final int bossId, final int points) {
        if (points <= 0 || ownerId <= 0 || bossId <= 0) {
            return;
        }
        pointsLock.lock();
        Map<Integer, Integer> pointsTable = _points.computeIfAbsent(ownerId, k -> new HashMap<>());
        if (pointsTable.isEmpty()) {
            pointsTable.put(bossId, points);
        } else {
            final Integer currentPoins = pointsTable.get(bossId);
            pointsTable.put(bossId, (currentPoins == null) ? points : (currentPoins + points));
        }
        pointsLock.unlock();
    }

    public TreeMap<Integer, Integer> calculateRanking() {
        final TreeMap<Integer, Integer> tmpRanking = new TreeMap<>();
        pointsLock.lock();
        for (final Entry<Integer, Map<Integer, Integer>> point : _points.entrySet()) {
            final Map<Integer, Integer> tmpPoint = point.getValue();
            tmpPoint.remove(KEY_RANK);
            tmpPoint.remove(KEY_TOTAL_POINTS);
            int totalPoints = tmpPoint.values().stream().mapToInt(e -> e).sum();
            if (totalPoints != 0) {
                tmpPoint.put(KEY_TOTAL_POINTS, totalPoints);
                tmpRanking.put(totalPoints, point.getKey());
            }
        }
        int ranking = 1;
        for (final int entry : tmpRanking.descendingMap().values()) {
            final Map<Integer, Integer> tmpPoint2 = _points.get(entry);
            tmpPoint2.put(KEY_RANK, ranking++);
        }
        pointsLock.unlock();
        return tmpRanking;
    }

    public void distributeRewards() {
        pointsLock.lock();
        final TreeMap<Integer, Integer> ranking = calculateRanking();
        final Iterator<Integer> e = ranking.descendingMap().values().iterator();
        for (int counter = 1; e.hasNext() && counter <= 100; ++counter) {
            int reward = 0;
            final int playerId = e.next();
            if (counter == 1) {
                reward = 2500;
            } else if (counter == 2) {
                reward = 1800;
            } else if (counter == 3) {
                reward = 1400;
            } else if (counter == 4) {
                reward = 1200;
            } else if (counter == 5) {
                reward = 900;
            } else if (counter == 6) {
                reward = 700;
            } else if (counter == 7) {
                reward = 600;
            } else if (counter == 8) {
                reward = 400;
            } else if (counter == 9) {
                reward = 300;
            } else if (counter == 10) {
                reward = 200;
            } else if (counter <= 50) {
                reward = 50;
            } else if (counter <= 100) {
                reward = 25;
            }
            final Player player = GameObjectsStorage.getPlayer(playerId);
            Clan clan;
            if (player != null) {
                clan = player.getClan();
            } else {
                clan = ClanTable.getInstance().getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + playerId));
            }
            if (clan != null) {
                clan.incReputation(reward, true, "RaidPoints");
            }
        }
        _points.clear();
        updatePointsDb();
        pointsLock.unlock();
    }

    public Map<Integer, Map<Integer, Integer>> getPoints() {
        return _points;
    }

    public Map<Integer, Integer> getPointsForOwnerId(final int ownerId) {
        return _points.get(ownerId);
    }

    public enum Status {
        ALIVE,
        DEAD,
        UNDEFINED
    }

    private static class LazyHolder {
        private static final RaidBossSpawnManager INSTANCE = new RaidBossSpawnManager();
    }
}
