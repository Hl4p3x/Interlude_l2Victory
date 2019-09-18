package ru.j2dev.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.taskmanager.tasks.RecommendationUpdateTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public final class TaskManager {
    static final String[] SQL_STATEMENTS = {"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks", "UPDATE global_tasks SET last_activation=? WHERE id=?", "SELECT id FROM global_tasks WHERE task=?", "INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"};
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
    private static TaskManager _instance;

    final List<ExecutedTask> _currentTasks;
    private final Map<String, Task> _tasks;

    public TaskManager() {
        _tasks = new ConcurrentHashMap<>();
        _currentTasks = new ArrayList<>();
        init();
        startAllTasks();
    }

    public static TaskManager getInstance() {
        if (_instance == null) {
            _instance = new TaskManager();
        }
        return _instance;
    }

    public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3) {
        return addUniqueTask(task, type, param1, param2, param3, 0L);
    }

    public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SQL_STATEMENTS[2]);
            statement.setString(1, task);
            rset = statement.executeQuery();
            final boolean exists = rset.next();
            DbUtils.close(statement, rset);
            if (!exists) {
                statement = con.prepareStatement(SQL_STATEMENTS[3]);
                statement.setString(1, task);
                statement.setString(2, type.toString());
                statement.setLong(3, lastActivation / 1000L);
                statement.setString(4, param1);
                statement.setString(5, param2);
                statement.setString(6, param3);
                statement.execute();
            }
            return true;
        } catch (SQLException e) {
            LOGGER.warn("cannot add the unique task: " + e.getMessage());
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return false;
    }

    public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3) {
        return addTask(task, type, param1, param2, param3, 0L);
    }

    public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SQL_STATEMENTS[3]);
            statement.setString(1, task);
            statement.setString(2, type.toString());
            statement.setLong(3, lastActivation / 1000L);
            statement.setString(4, param1);
            statement.setString(5, param2);
            statement.setString(6, param3);
            statement.execute();
            return true;
        } catch (SQLException e) {
            LOGGER.warn("cannot add the task:\t" + e.getMessage());
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return false;
    }

    public void init() {
        registerTask(new RecommendationUpdateTask());
    }

    public void registerTask(final Task task) {
        final String name = task.getName();
        if (!_tasks.containsKey(name)) {
            _tasks.put(name, task);
            task.init();
        }
    }

    private void startAllTasks() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SQL_STATEMENTS[0]);
            rset = statement.executeQuery();
            while (rset.next()) {
                final Task task = _tasks.get(rset.getString("task"));
                if (task == null) {
                    continue;
                }
                final TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
                if (type == TaskTypes.TYPE_NONE) {
                    continue;
                }
                final ExecutedTask current = new ExecutedTask(task, type, rset);
                if (!launchTask(current)) {
                    continue;
                }
                _currentTasks.add(current);
            }
        } catch (Exception e) {
            LOGGER.error("error while loading Global Task table " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    private boolean launchTask(final ExecutedTask task) {
        final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
        final TaskTypes type = task.getType();
        if (type == TaskTypes.TYPE_STARTUP) {
            task.run();
            return false;
        }
        if (type == TaskTypes.TYPE_SHEDULED) {
            final long delay = Long.parseLong(task.getParams()[0]);
            task._scheduled = scheduler.schedule(task, delay);
            return true;
        }
        if (type == TaskTypes.TYPE_FIXED_SHEDULED) {
            final long delay = Long.parseLong(task.getParams()[0]);
            final long interval = Long.parseLong(task.getParams()[1]);
            task._scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);
            return true;
        }
        switch (type) {
            case TYPE_TIME:
                try {
                    final Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
                    final long diff = desired.getTime() - System.currentTimeMillis();
                    if (diff >= 0L) {
                        task._scheduled = scheduler.schedule(task, diff);
                        return true;
                    }
                    LOGGER.info("Task " + task.getId() + " is obsoleted.");
                } catch (Exception ignored) {
                }
                break;
            case TYPE_SPECIAL:
                final ScheduledFuture<?> result = task.getTask().launchSpecial(task);
                if (result != null) {
                    task._scheduled = result;
                    return true;
                }
                break;
            case TYPE_GLOBAL_TASK:
                final long interval2 = Long.parseLong(task.getParams()[0]) * 86400000L;
                final String[] hour = task.getParams()[1].split(":");
                if (hour.length != 3) {
                    LOGGER.warn("Task " + task.getId() + " has incorrect parameters");
                    return false;
                }
                final Calendar check = Calendar.getInstance();
                check.setTimeInMillis(task.getLastActivation() + interval2);
                final Calendar min = Calendar.getInstance();
                try {
                    min.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
                    min.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
                    min.set(Calendar.SECOND, Integer.parseInt(hour[2]));
                } catch (Exception e) {
                    LOGGER.warn("Bad parameter on task " + task.getId() + ": " + e.getMessage());
                    return false;
                }
                long delay2 = min.getTimeInMillis() - System.currentTimeMillis();
                if (check.after(min) || delay2 < 0L) {
                    delay2 += interval2;
                }
                task._scheduled = scheduler.scheduleAtFixedRate(task, delay2, interval2);
                return true;
        }
        return false;
    }

    public class ExecutedTask extends RunnableImpl {
        final Task _task;
        final TaskTypes _type;
        int _id;
        long _lastActivation;
        String[] _params;
        ScheduledFuture<?> _scheduled;

        public ExecutedTask(final Task task, final TaskTypes type, final ResultSet rset) throws SQLException {
            _task = task;
            _type = type;
            _id = rset.getInt("id");
            _lastActivation = rset.getLong("last_activation") * 1000L;
            _params = new String[]{rset.getString("param1"), rset.getString("param2"), rset.getString("param3")};
        }

        @Override
        public void runImpl() {
            _task.onTimeElapsed(this);
            _lastActivation = System.currentTimeMillis();
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement(SQL_STATEMENTS[1]);
                statement.setLong(1, _lastActivation / 1000L);
                statement.setInt(2, _id);
                statement.executeUpdate();
            } catch (SQLException e) {
                ExecutedTask.LOGGER.warn("cannot updated the Global Task " + _id + ": " + e.getMessage());
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
            if (_type == TaskTypes.TYPE_SHEDULED || _type == TaskTypes.TYPE_TIME) {
                stopTask();
            }
        }

        @Override
        public boolean equals(final Object object) {
            return _id == ((ExecutedTask) object)._id;
        }

        public Task getTask() {
            return _task;
        }

        public TaskTypes getType() {
            return _type;
        }

        public int getId() {
            return _id;
        }

        public String[] getParams() {
            return _params;
        }

        public long getLastActivation() {
            return _lastActivation;
        }

        public void stopTask() {
            _task.onDestroy();
            if (_scheduled != null) {
                _scheduled.cancel(false);
            }
            _currentTasks.remove(this);
        }
    }
}
