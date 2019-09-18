package ru.j2dev.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class L2TopRuManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(L2TopRuManager.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static L2TopRuManager _instance;
    private static String USERAGENT = "Mozilla/5.0 (SunOS; 5.10; amd64; U) Java HotSpot(TM) 64-Bit Server VM/16.2-b04";
    private static Map<Integer, Long> _voteDateCache = new ConcurrentHashMap<>();

    private Pattern _webPattern;
    private Pattern _smsPattern;

    private L2TopRuManager() {
        if (Config.L2TOPRU_DELAY < 1L) {
            return;
        }
        LOGGER.info("L2TopRuManager: Initializing.");
        _webPattern = Pattern.compile("^([\\d-]+\\s[\\d:]+)\\s+(?:" + Config.L2TOPRU_PREFIX + "-)*([^\\s]+)$", Pattern.MULTILINE);
        _smsPattern = Pattern.compile("^([\\d-]+\\s[\\d:]+)\\s+(?:" + Config.L2TOPRU_PREFIX + "-)*([^\\s]+)\\s+x(\\d{1,2})$", Pattern.MULTILINE);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new L2TopRuTask(), Config.L2TOPRU_DELAY, Config.L2TOPRU_DELAY);
    }

    public static L2TopRuManager getInstance() {
        if (_instance == null) {
            _instance = new L2TopRuManager();
        }
        return _instance;
    }

    protected ArrayList<L2TopRuVote> filterVotes(final ArrayList<L2TopRuVote> votes) {
        final ArrayList<L2TopRuVote> result = new ArrayList<>();
        final HashMap<String, Integer> chars = new HashMap<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT `obj_Id`,`char_name` FROM `characters`");
            while (rset.next()) {
                chars.put(rset.getString("char_name"), rset.getInt("obj_Id"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, stmt, rset);
        }
        int charObjId;
        for (final L2TopRuVote vote : votes) {
            if (chars.containsKey(vote.charname)) {
                charObjId = chars.get(vote.charname);
                if (!isRewardReq(charObjId, vote.datetime)) {
                    continue;
                }
                vote.char_obj_id = charObjId;
                result.add(vote);
            }
        }
        return result;
    }

    private boolean isRewardReq(final int charObjId, final long date) {
        long lastDate;
        Connection con = null;
        PreparedStatement pstmt;
        ResultSet rset;
        if (_voteDateCache.containsKey(charObjId)) {
            lastDate = _voteDateCache.get(charObjId);
            if (date > lastDate) {
                _voteDateCache.put(charObjId, date);
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
                    pstmt.setInt(1, charObjId);
                    pstmt.setLong(2, date);
                    pstmt.executeUpdate();
                    DbUtils.closeQuietly(con, pstmt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        } else {
            try {
                con = DatabaseFactory.getInstance().getConnection();
                pstmt = con.prepareStatement("SELECT `obj_Id`,`last_vote` FROM `l2topru_votes` WHERE `obj_Id` = ?");
                pstmt.setInt(1, charObjId);
                rset = pstmt.executeQuery();
                if (!rset.next()) {
                    DbUtils.closeQuietly(pstmt, rset);
                    _voteDateCache.put(charObjId, date);
                    try {
                        pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
                        pstmt.setInt(1, charObjId);
                        pstmt.setLong(2, date);
                        pstmt.executeUpdate();
                        DbUtils.closeQuietly(con, pstmt);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
                lastDate = rset.getLong("last_vote");
                DbUtils.closeQuietly(pstmt, rset);
                if (date > lastDate) {
                    _voteDateCache.put(charObjId, date);
                    try {
                        pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
                        pstmt.setInt(1, charObjId);
                        pstmt.setLong(2, date);
                        pstmt.executeUpdate();
                        DbUtils.closeQuietly(con, pstmt);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
                _voteDateCache.put(charObjId, lastDate);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                DbUtils.closeQuietly(con);
            }
        }
        return false;
    }

    private void giveItem(final int charObjId, final int itemId, final int itemCount) {
        if (charObjId < 1) {
            return;
        }
        final Player player = GameObjectsStorage.getPlayer(charObjId);
        if (player != null) {
            player.sendMessage(new CustomMessage("ru.j2dev.gameserver.taskmanager.L2TopRuManager", player).addItemName(player.getInventory().addItem(itemId, itemCount)));
        } else {
            final ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
            newItem.setCount(itemCount);
            newItem.setOwnerId(charObjId);
            newItem.setLocation(ItemLocation.INVENTORY);
            newItem.save();
        }
    }

    private void rewardVotes(final ArrayList<L2TopRuVote> votes) {
        for (final L2TopRuVote vote : votes) {
            switch (vote.type) {
                case WEB: {
                    LOGGER.info("L2TopRuManager: Rewarding " + vote);
                    giveItem(vote.char_obj_id, Config.L2TOPRU_WEB_REWARD_ITEMID, Config.L2TOPRU_WEB_REWARD_ITEMCOUNT);
                    continue;
                }
                case SMS: {
                    LOGGER.info("L2TopRuManager: Rewarding " + vote);
                    if (Config.L2TOPRU_SMS_REWARD_VOTE_MULTI) {
                        giveItem(vote.char_obj_id, Config.L2TOPRU_SMS_REWARD_ITEMID, Config.L2TOPRU_SMS_REWARD_VOTE_MULTI ? (Config.L2TOPRU_SMS_REWARD_ITEMCOUNT * vote.count) : Config.L2TOPRU_SMS_REWARD_ITEMCOUNT);
                        continue;
                    }
                    continue;
                }
            }
        }
    }

    private ArrayList<L2TopRuVote> getAllVotes() {
        final ArrayList<L2TopRuVote> result = new ArrayList<>();
        try {
            Matcher m = _webPattern.matcher(getPage(Config.L2TOPRU_WEB_VOTE_URL));
            while (m.find()) {
                final String dateTimeStr = m.group(1);
                final String nameStr = m.group(2);
                if (Util.isMatchingRegexp(nameStr, Config.CNAME_TEMPLATE)) {
                    final L2TopRuVote vote = new L2TopRuVote(dateTimeStr, nameStr);
                    result.add(vote);
                }
            }
            m = _smsPattern.matcher(getPage(Config.L2TOPRU_SMS_VOTE_URL));
            while (m.find()) {
                final String dateTimeStr = m.group(1);
                final String nameStr = m.group(2);
                final String mulStr = m.group(3);
                if (Util.isMatchingRegexp(nameStr, Config.CNAME_TEMPLATE)) {
                    final L2TopRuVote vote = new L2TopRuVote(dateTimeStr, nameStr, mulStr);
                    result.add(vote);
                }
            }
            result.sort(new L2TopRuVoteComparator<>());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void tick() {
        rewardVotes(filterVotes(getAllVotes()));
    }

    private String getPage(final String uri) {
        try {
            final URL url = new URL(uri);
            final URLConnection conn = url.openConnection();
            conn.addRequestProperty("Host", url.getHost());
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Connection", "close");
            conn.addRequestProperty("User-Agent", USERAGENT);
            conn.setConnectTimeout(30000);
            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "cp1251"));
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private enum L2TopRuVoteType {
        WEB,
        SMS
    }

    private class L2TopRuTask implements Runnable {
        @Override
        public void run() {
            getInstance().tick();
        }
    }

    private class L2TopRuVote {
        public long datetime;
        public String charname;
        public int count;
        public int char_obj_id;
        public L2TopRuVoteType type;

        public L2TopRuVote(final String date, final String charName, final String itemcount) throws Exception {
            char_obj_id = -1;
            datetime = DATE_FORMAT.parse(date).getTime() / 1000L;
            count = Byte.parseByte(itemcount);
            charname = charName;
            type = L2TopRuVoteType.SMS;
        }

        public L2TopRuVote(final String date, final String charName) throws Exception {
            char_obj_id = -1;
            datetime = DATE_FORMAT.parse(date).getTime() / 1000L;
            charname = charName;
            count = 1;
            type = L2TopRuVoteType.WEB;
        }

        @Override
        public String toString() {
            return charname + "-" + count + "[" + char_obj_id + "(" + datetime + "|" + type.name() + ")]";
        }
    }

    private final class L2TopRuVoteComparator<T> implements Comparator<L2TopRuVote> {
        @Override
        public int compare(final L2TopRuVote o, final L2TopRuVote o1) {
            if (o.datetime == o1.datetime) {
                return 0;
            }
            if (o.datetime < o1.datetime) {
                return Integer.MIN_VALUE;
            }
            if (o.datetime > o1.datetime) {
                return Integer.MAX_VALUE;
            }
            return -1;
        }
    }
}
