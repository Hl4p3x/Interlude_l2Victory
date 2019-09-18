package ru.j2dev.gameserver.manager.games;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class LotteryManager {
    public static final long SECOND = 1000L;
    public static final long MINUTE = 60000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LotteryManager.class);
    private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
    private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
    private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
    private static final String SELECT_LOTTERY_ITEM = "SELECT items.enchant AS `enchant`, items_options.damaged AS `damaged` FROM items, items_options WHERE items_options.blessed = ? AND items.item_id = items_options.item_id AND items.item_type = 4442";
    private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 AND idnr = ?";
    private static LotteryManager _instance;

    protected int _number;
    protected int _prize;
    protected boolean _isSellingTickets;
    protected boolean _isStarted;
    protected long _enddate;

    public LotteryManager() {
        _number = 1;
        _prize = Config.SERVICES_LOTTERY_PRIZE;
        _isSellingTickets = false;
        _isStarted = false;
        _enddate = System.currentTimeMillis();
        if (Config.SERVICES_ALLOW_LOTTERY) {
            new startLottery().run();
        }
    }

    public static LotteryManager getInstance() {
        if (_instance == null) {
            _instance = new LotteryManager();
        }
        return _instance;
    }

    public void increasePrize(final int count) {
        _prize += count;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?");
            statement.setInt(1, getPrize());
            statement.setInt(2, getPrize());
            statement.setInt(3, getId());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.warn("Lottery: Could not increase current lottery prize: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private boolean restoreLotteryData() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1");
            rset = statement.executeQuery();
            if (rset.next()) {
                _number = rset.getInt("idnr");
                if (rset.getInt("finished") == 1) {
                    ++_number;
                    _prize = rset.getInt("newprize");
                } else {
                    _prize = rset.getInt("prize");
                    _enddate = rset.getLong("enddate");
                    if (_enddate <= System.currentTimeMillis() + 120000L) {
                        new finishLottery().run();
                        return false;
                    }
                    if (_enddate > System.currentTimeMillis()) {
                        _isStarted = true;
                        ThreadPoolManager.getInstance().schedule(new finishLottery(), _enddate - System.currentTimeMillis());
                        if (_enddate > System.currentTimeMillis() + 720000L) {
                            _isSellingTickets = true;
                            ThreadPoolManager.getInstance().schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 600000L);
                        }
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Lottery: Could not restore lottery data: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return true;
    }

    private void announceLottery() {
        if (Config.SERVICES_ALLOW_LOTTERY) {
            LOGGER.info("Lottery: Starting ticket sell for lottery #" + getId() + ".");
        }
        _isSellingTickets = true;
        _isStarted = true;
        Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");
    }

    private void scheduleEndOfLottery() {
        final Calendar finishtime = Calendar.getInstance();
        finishtime.setTimeInMillis(_enddate);
        finishtime.set(Calendar.MINUTE, 0);
        finishtime.set(Calendar.SECOND, 0);
        if (finishtime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            finishtime.set(Calendar.HOUR_OF_DAY, 19);
            _enddate = finishtime.getTimeInMillis();
            _enddate += 604800000L;
        } else {
            finishtime.set(Calendar.DAY_OF_WEEK, 1);
            finishtime.set(Calendar.HOUR_OF_DAY, 19);
            _enddate = finishtime.getTimeInMillis();
        }
        ThreadPoolManager.getInstance().schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 600000L);
        ThreadPoolManager.getInstance().schedule(new finishLottery(), _enddate - System.currentTimeMillis());
    }

    private void createNewLottery() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)");
            statement.setInt(1, 1);
            statement.setInt(2, getId());
            statement.setLong(3, getEndDate());
            statement.setInt(4, getPrize());
            statement.setInt(5, getPrize());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.warn("Lottery: Could not store new lottery data: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public int[] decodeNumbers(int enchant, int type2) {
        final int[] res = new int[5];
        int id = 0;
        for (int nr = 1; enchant > 0; enchant /= 2, ++nr) {
            final int val = enchant / 2;
            if (val != enchant / 2.0) {
                res[id++] = nr;
            }
        }
        for (int nr = 17; type2 > 0; type2 /= 2, ++nr) {
            final int val = type2 / 2;
            if (val != type2 / 2.0) {
                res[id++] = nr;
            }
        }
        return res;
    }

    public int[] checkTicket(final int id, final int enchant, final int type2) {
        final int[] res = {0, 0};
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 AND idnr = ?");
            statement.setInt(1, id);
            rset = statement.executeQuery();
            if (rset.next()) {
                int curenchant = rset.getInt("number1") & enchant;
                int curtype2 = rset.getInt("number2") & type2;
                if (curenchant == 0 && curtype2 == 0) {
                    return res;
                }
                int count = 0;
                for (int i = 1; i <= 16; ++i) {
                    final int val = curenchant / 2;
                    if (val != curenchant / 2.0) {
                        ++count;
                    }
                    final int val2 = curtype2 / 2;
                    if (val2 != curtype2 / 2.0) {
                        ++count;
                    }
                    curenchant = val;
                    curtype2 = val2;
                }
                switch (count) {
                    case 0: {
                        break;
                    }
                    case 5: {
                        res[res[0] = 1] = rset.getInt("prize1");
                        break;
                    }
                    case 4: {
                        res[0] = 2;
                        res[1] = rset.getInt("prize2");
                        break;
                    }
                    case 3: {
                        res[0] = 3;
                        res[1] = rset.getInt("prize3");
                        break;
                    }
                    default: {
                        res[0] = 4;
                        res[1] = 200;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Lottery: Could not check lottery ticket #" + id + ": " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return res;
    }

    public int[] checkTicket(final ItemInstance item) {
        return checkTicket(item.getBlessed(), item.getEnchantLevel(), item.getDamaged());
    }

    public boolean isSellableTickets() {
        return _isSellingTickets;
    }

    public boolean isStarted() {
        return _isStarted;
    }

    public int getId() {
        return _number;
    }

    public int getPrize() {
        return _prize;
    }

    public long getEndDate() {
        return _enddate;
    }

    private class startLottery extends RunnableImpl {
        @Override
        public void runImpl() {
            if (restoreLotteryData()) {
                announceLottery();
                scheduleEndOfLottery();
                createNewLottery();
            }
        }
    }

    private class stopSellingTickets extends RunnableImpl {
        @Override
        public void runImpl() {
            if (Config.SERVICES_ALLOW_LOTTERY) {
                stopSellingTickets.LOGGER.info("Lottery: Stopping ticket sell for lottery #" + getId() + ".");
            }
            _isSellingTickets = false;
            Announcements.getInstance().announceToAll(Msg.LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED);
        }
    }

    private class finishLottery extends RunnableImpl {
        @Override
        public void runImpl() {
            if (Config.SERVICES_ALLOW_LOTTERY) {
                finishLottery.LOGGER.info("Lottery: Ending lottery #" + getId() + ".");
            }
            final int[] luckynums = new int[5];
            int luckynum = 0;
            for (int i = 0; i < 5; ++i) {
                boolean found = true;
                while (found) {
                    luckynum = Rnd.get(20) + 1;
                    found = false;
                    for (int j = 0; j < i; ++j) {
                        if (luckynums[j] == luckynum) {
                            found = true;
                        }
                    }
                }
                luckynums[i] = luckynum;
            }
            if (Config.SERVICES_ALLOW_LOTTERY) {
                finishLottery.LOGGER.info("Lottery: The lucky numbers are " + luckynums[0] + ", " + luckynums[1] + ", " + luckynums[2] + ", " + luckynums[3] + ", " + luckynums[4] + ".");
            }
            int enchant = 0;
            int type2 = 0;
            for (int k = 0; k < 5; ++k) {
                if (luckynums[k] < 17) {
                    enchant += (int) Math.pow(2.0, luckynums[k] - 1);
                } else {
                    type2 += (int) Math.pow(2.0, luckynums[k] - 17);
                }
            }
            if (Config.SERVICES_ALLOW_LOTTERY) {
                finishLottery.LOGGER.info("Lottery: Encoded lucky numbers are " + enchant + ", " + type2);
            }
            int count1 = 0;
            int count2 = 0;
            int count3 = 0;
            int count4 = 0;
            Connection con = null;
            PreparedStatement statement = null;
            ResultSet rset = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT items.enchant AS `enchant`, items_options.damaged AS `damaged` FROM items, items_options WHERE items_options.blessed = ? AND items.item_id = items_options.item_id AND items.item_type = 4442");
                statement.setInt(1, getId());
                rset = statement.executeQuery();
                while (rset.next()) {
                    int curenchant = rset.getInt("enchant") & enchant;
                    int curtype2 = rset.getInt("damaged") & type2;
                    if (curenchant == 0 && curtype2 == 0) {
                        continue;
                    }
                    int count5 = 0;
                    for (int l = 1; l <= 16; ++l) {
                        final int val = curenchant / 2;
                        if (val != curenchant / 2.0) {
                            ++count5;
                        }
                        final int val2 = curtype2 / 2;
                        if (val2 != curtype2 / 2.0) {
                            ++count5;
                        }
                        curenchant = val;
                        curtype2 = val2;
                    }
                    switch (count5) {
                        case 5:
                            ++count1;
                            break;
                        case 4:
                            ++count2;
                            break;
                        case 3:
                            ++count3;
                            break;
                        default:
                            if (count5 <= 0) {
                                continue;
                            }
                            ++count4;
                            break;
                    }
                }
            } catch (SQLException e) {
                finishLottery.LOGGER.warn("Lottery: Could restore lottery data: " + e);
            } finally {
                DbUtils.closeQuietly(con, statement, rset);
            }
            final int prize4 = count4 * Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
            int prize5 = 0;
            int prize6 = 0;
            int prize7 = 0;
            if (count1 > 0) {
                prize5 = (int) ((getPrize() - prize4) * Config.SERVICES_LOTTERY_5_NUMBER_RATE / count1);
            }
            if (count2 > 0) {
                prize6 = (int) ((getPrize() - prize4) * Config.SERVICES_LOTTERY_4_NUMBER_RATE / count2);
            }
            if (count3 > 0) {
                prize7 = (int) ((getPrize() - prize4) * Config.SERVICES_LOTTERY_3_NUMBER_RATE / count3);
            }
            int newprize;
            if (prize5 == 0 && prize6 == 0 && prize7 == 0) {
                newprize = getPrize();
            } else {
                newprize = getPrize() + prize5 + prize6 + prize7;
            }
            if (Config.SERVICES_ALLOW_LOTTERY) {
                finishLottery.LOGGER.info("Lottery: Jackpot for next lottery is " + newprize + ".");
            }
            if (count1 > 0) {
                final SystemMessage sm = new SystemMessage(1112);
                sm.addNumber(getId());
                sm.addNumber(getPrize());
                sm.addNumber(count1);
                Announcements.getInstance().announceToAll(sm);
            } else {
                final SystemMessage sm = new SystemMessage(1113);
                sm.addNumber(getId());
                sm.addNumber(getPrize());
                Announcements.getInstance().announceToAll(sm);
            }
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?");
                statement.setInt(1, getPrize());
                statement.setInt(2, newprize);
                statement.setInt(3, enchant);
                statement.setInt(4, type2);
                statement.setInt(5, prize5);
                statement.setInt(6, prize6);
                statement.setInt(7, prize7);
                statement.setInt(8, getId());
                statement.execute();
            } catch (SQLException e2) {
                finishLottery.LOGGER.warn("Lottery: Could not store finished lottery data: " + e2);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
            ThreadPoolManager.getInstance().schedule(new startLottery(), 60000L);
            final LotteryManager this$0 = LotteryManager.this;
            ++this$0._number;
            _isStarted = false;
        }
    }
}
