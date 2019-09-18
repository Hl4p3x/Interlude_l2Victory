package services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TopPvPPKService extends Functions {
    private static final Logger LOG = LoggerFactory.getLogger(TopPvPPKService.class);
    private static final String SERVICE_HTML_BASE = "scripts/services/";

    private static void sendTopRecordHtml(final TopRecordHolder topRecordHolder, final Player player, final NpcInstance npc, final String htmlFile) {
        final NpcHtmlMessage msg = new NpcHtmlMessage(player, npc).setFile(SERVICE_HTML_BASE + htmlFile);
        final StringBuilder contentHtmlBuilder = new StringBuilder();
        final Collection<TopRecord> topRecords = topRecordHolder.getTopRecords();
        for (final TopRecord topRecord : topRecords) {
            contentHtmlBuilder.append(topRecord.formatHtml(player));
        }
        msg.replace("%content%", contentHtmlBuilder.toString());
        player.sendPacket(msg);
    }

    public void topPvP() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_PVP_PK_STATISTIC) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        sendTopRecordHtml(TopPvPRecordHolder.getInstance(), player, getNpc(), "top_pvp.htm");
    }

    public void topPK() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_PVP_PK_STATISTIC) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        sendTopRecordHtml(TopPKRecordHolder.getInstance(), player, getNpc(), "top_pk.htm");
    }

    private static class TopRecord implements Comparable<TopRecord> {
        private final int _playerObjectId;
        private final int _value;
        protected String _playerName;

        private TopRecord(final int playerObjectId, final String playerName, final int value) {
            _playerObjectId = playerObjectId;
            _playerName = playerName;
            _value = value;
        }

        protected int getPlayerObjectId() {
            return _playerObjectId;
        }

        protected Player getPlayer() {
            return GameObjectsStorage.getPlayer(_playerObjectId);
        }

        public String getPlayerName() {
            return _playerName;
        }

        public boolean isOnline() {
            final Player player = getPlayer();
            return player != null && player.isOnline();
        }

        public int getTopValue() {
            return _value;
        }

        public String formatHtml(final Player forPlayer) {
            String html = StringHolder.getInstance().getNotNull(forPlayer, isOnline() ? "services.TopPvPPKService.TopRecord.RecordHtmlPlayerOnline" : "services.TopPvPPKService.TopRecord.RecordHtmlPlayerOffline");
            html = html.replace("%name%", getPlayerName());
            html = html.replace("%val%", String.valueOf(getTopValue()));
            return html;
        }

        @Override
        public int compareTo(final TopRecord o) {
            if (getPlayerObjectId() == o.getPlayerObjectId()) {
                return 0;
            }
            return o.getTopValue() - getTopValue();
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || (o != null && o instanceof TopRecord && getPlayerObjectId() == ((TopRecord) o).getPlayerObjectId());
        }
    }

    private abstract static class TopRecordHolder {
        private final AtomicReference<Pair<Long, Collection<TopRecord>>> _lastUpdateAndRecords;
        private final int _limit;
        private final long _updateInterval;

        protected TopRecordHolder(final int limit, final long updateInterval) {
            _updateInterval = updateInterval;
            _lastUpdateAndRecords = new AtomicReference<>(Pair.of(0L, Collections.emptyList()));
            _limit = limit;
        }

        protected int getLimit() {
            return _limit;
        }

        protected abstract Collection<TopRecord> fetchTopOnlineRecords();

        protected abstract Collection<TopRecord> fetchTopDbRecords();

        protected Collection<TopRecord> fetchTopRecords() {
            final Collection<TopRecord> onlineRecords = fetchTopOnlineRecords();
            final Collection<TopRecord> dbRecords = fetchTopDbRecords();
            final List<TopRecord> result = new ArrayList<>(onlineRecords.size() + dbRecords.size());
            result.addAll(onlineRecords);
            for (final TopRecord topRecord : dbRecords) {
                if (result.contains(topRecord)) {
                    continue;
                }
                result.add(topRecord);
            }
            Collections.sort(result);
            return new ArrayList<>(result.subList(0, Math.min(_limit, result.size())));
        }

        public Collection<TopRecord> getTopRecords() {
            Pair<Long, Collection<TopRecord>> lastUpdateAndRecords;
            while ((lastUpdateAndRecords = _lastUpdateAndRecords.get()).getLeft() + _updateInterval < System.currentTimeMillis()) {
                final Collection<TopRecord> newTopRecords = fetchTopRecords();
                final Pair<Long, Collection<TopRecord>> newLastUpdateAndRecords = Pair.of(System.currentTimeMillis(), newTopRecords);
                if (_lastUpdateAndRecords.compareAndSet(lastUpdateAndRecords, newLastUpdateAndRecords)) {
                    return Collections.unmodifiableCollection(newLastUpdateAndRecords.getRight());
                }
            }
            return Collections.unmodifiableCollection(lastUpdateAndRecords.getRight());
        }
    }

    private static class TopPvPRecordHolder extends TopRecordHolder {
        private static final TopPvPRecordHolder INSTANCE = new TopPvPRecordHolder();

        private TopPvPRecordHolder() {
            super(Config.PVP_PK_STAT_RECORD_LIMIT, Config.PVP_PK_STAT_CACHE_UPDATE_INTERVAL);
        }

        public static TopPvPRecordHolder getInstance() {
            return TopPvPRecordHolder.INSTANCE;
        }

        @Override
        protected Collection<TopRecord> fetchTopOnlineRecords() {
            return GameObjectsStorage.getPlayers().stream().map(player -> new TopRecord(player.getObjectId(), player.getName(), player.getPvpKills())).collect(Collectors.toCollection(LinkedList::new));
        }

        @Override
        protected Collection<TopRecord> fetchTopDbRecords() {
            final List<TopRecord> recordSet = new LinkedList<>();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rset = null;
            try {
                conn = DatabaseFactory.getInstance().getConnection();
                pstmt = conn.prepareStatement("SELECT  `characters`.`obj_Id` AS `playerObjectId`,  `characters`.`char_name` AS `playerName`,  `characters`.`pvpkills` AS `pvpKills` FROM  `characters` ORDER BY  `characters`.`pvpkills` DESC LIMIT ?");
                pstmt.setInt(1, getLimit());
                rset = pstmt.executeQuery();
                while (rset.next()) {
                    recordSet.add(new TopRecord(rset.getInt("playerObjectId"), rset.getString("playerName"), rset.getInt("pvpKills")));
                }
            } catch (SQLException se) {
                LOG.error("Can't fetch top PvP records.", se);
            } finally {
                DbUtils.closeQuietly(conn, pstmt, rset);
            }
            return recordSet;
        }
    }

    private static class TopPKRecordHolder extends TopRecordHolder {
        private static final TopPKRecordHolder INSTANCE = new TopPKRecordHolder();

        private TopPKRecordHolder() {
            super(Config.PVP_PK_STAT_RECORD_LIMIT, Config.PVP_PK_STAT_CACHE_UPDATE_INTERVAL);
        }

        public static TopPKRecordHolder getInstance() {
            return TopPKRecordHolder.INSTANCE;
        }

        @Override
        protected Collection<TopRecord> fetchTopOnlineRecords() {
            return GameObjectsStorage.getPlayers().stream().map(player -> new TopRecord(player.getObjectId(), player.getName(), player.getPkKills())).collect(Collectors.toCollection(LinkedList::new));
        }

        @Override
        protected Collection<TopRecord> fetchTopDbRecords() {
            final List<TopRecord> recordSet = new LinkedList<>();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rset = null;
            try {
                conn = DatabaseFactory.getInstance().getConnection();
                pstmt = conn.prepareStatement("SELECT  `characters`.`obj_Id` AS `playerObjectId`,  `characters`.`char_name` AS `playerName`,  `characters`.`pkkills` AS `pkKills` FROM  `characters` ORDER BY  `characters`.`pkkills` DESC LIMIT ?");
                pstmt.setInt(1, getLimit());
                rset = pstmt.executeQuery();
                while (rset.next()) {
                    recordSet.add(new TopRecord(rset.getInt("playerObjectId"), rset.getString("playerName"), rset.getInt("pkKills")));
                }
            } catch (SQLException se) {
                LOG.error("Can't fetch top PK records.", se);
            } finally {
                DbUtils.closeQuietly(conn, pstmt, rset);
            }
            return recordSet;
        }
    }
}
