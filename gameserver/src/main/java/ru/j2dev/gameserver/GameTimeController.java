package ru.j2dev.gameserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.listener.GameListener;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.listener.game.OnStartListener;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ClientSetTime;

import java.util.Calendar;

public class GameTimeController {
    public static final int TICKS_PER_SECOND = 10;
    public static final int MILLIS_IN_TICK = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(GameTimeController.class);
    private static final GameTimeController _instance = new GameTimeController();

    private final long _gameStartTime;
    private final GameTimeListenerList listenerEngine;
    private final Runnable _dayChangeNotify;

    private GameTimeController() {
        listenerEngine = new GameTimeListenerList();
        _dayChangeNotify = new CheckSunState();
        _gameStartTime = getDayStartTime();
        GameServer.getInstance().addListener(new OnStartListenerImpl());
        final StringBuilder msg = new StringBuilder();
        msg.append("GameTimeController: initialized.").append(" ");
        msg.append("Current time is ");
        msg.append(getGameHour()).append(":");
        if (getGameMin() < 10) {
            msg.append("0");
        }
        msg.append(getGameMin());
        msg.append(" in the ");
        if (isNowNight()) {
            msg.append("night");
        } else {
            msg.append("day");
        }
        msg.append(".");
        LOGGER.info(msg.toString());
        long nightStart = 0L;
        long dayStart = 3600000L;
        while (_gameStartTime + nightStart < System.currentTimeMillis()) {
            nightStart += 14400000L;
        }
        while (_gameStartTime + dayStart < System.currentTimeMillis()) {
            dayStart += 14400000L;
        }
        dayStart -= System.currentTimeMillis() - _gameStartTime;
        nightStart -= System.currentTimeMillis() - _gameStartTime;
        ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, nightStart, 14400000L);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, dayStart, 14400000L);
    }

    public static GameTimeController getInstance() {
        return _instance;
    }

    private long getDayStartTime() {
        final Calendar dayStart = Calendar.getInstance();
        final int HOUR_OF_DAY = dayStart.get(Calendar.HOUR_OF_DAY);
        dayStart.add(Calendar.HOUR_OF_DAY, -(HOUR_OF_DAY + 1) % 4);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        return dayStart.getTimeInMillis();
    }

    public boolean isNowNight() {
        return getGameHour() < 6;
    }

    public int getGameTime() {
        return getGameTicks() / 100;
    }

    public int getGameHour() {
        return getGameTime() / 60 % 24;
    }

    public int getGameMin() {
        return getGameTime() % 60;
    }

    public int getGameTicks() {
        return (int) ((System.currentTimeMillis() - _gameStartTime) / 100L);
    }

    public GameTimeListenerList getListenerEngine() {
        return listenerEngine;
    }

    public <T extends GameListener> boolean addListener(final T listener) {
        return listenerEngine.add(listener);
    }

    public <T extends GameListener> boolean removeListener(final T listener) {
        return listenerEngine.remove(listener);
    }

    private class OnStartListenerImpl implements OnStartListener {
        @Override
        public void onStart() {
            ThreadPoolManager.getInstance().execute(_dayChangeNotify);
        }
    }

    public class CheckSunState extends RunnableImpl {
        @Override
        public void runImpl() {
            if (isNowNight()) {
                getInstance().getListenerEngine().onNight();
            } else {
                getInstance().getListenerEngine().onDay();
            }
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    GameObjectsStorage.getPlayers().forEach(player -> {
                        player.checkDayNightMessages();
                        player.sendPacket(new ClientSetTime());
                    });
                }
            });
        }
    }

    protected class GameTimeListenerList extends ListenerList<GameServer> {
        public void onDay() {
            getListeners().forEach(listener -> {
                try {
                    if (!OnDayNightChangeListener.class.isInstance(listener)) {
                        return;
                    }
                    ((OnDayNightChangeListener) listener).onDay();
                } catch (Exception ex) {
                    LOGGER.warn("Exception during day change", ex);
                }
            });
        }

        public void onNight() {
            getListeners().forEach(listener -> {
                try {
                    if (!OnDayNightChangeListener.class.isInstance(listener)) {
                        return;
                    }
                    ((OnDayNightChangeListener) listener).onNight();
                } catch (Exception ex) {
                    LOGGER.warn("Exception during night change", ex);
                }
            });
        }
    }
}
