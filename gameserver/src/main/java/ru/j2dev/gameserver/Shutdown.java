package ru.j2dev.gameserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcEntity;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.commons.time.cron.SchedulingPattern.InvalidPatternException;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.manager.CoupleManager;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.manager.games.FishingChampionShipManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import ru.j2dev.gameserver.model.entity.olympiad.*;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class Shutdown extends Thread {
    public static final int SHUTDOWN = 0;
    public static final int RESTART = 2;
    public static final int NONE = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);
    private static final Shutdown _instance = new Shutdown();

    private Timer counter;
    private int shutdownMode;
    private int shutdownCounter;

    private Shutdown() {
        setName(getClass().getSimpleName());
        setDaemon(true);
        shutdownMode = -1;
    }

    public static Shutdown getInstance() {
        return _instance;
    }

    public int getSeconds() {
        return (shutdownMode == -1) ? -1 : shutdownCounter;
    }

    public int getMode() {
        return shutdownMode;
    }

    public synchronized void schedule(final int seconds, final int shutdownMode) {
        if (seconds < 0) {
            return;
        }
        if (counter != null) {
            counter.cancel();
        }
        this.shutdownMode = shutdownMode;
        shutdownCounter = seconds;
        LOGGER.info("Scheduled server " + ((shutdownMode == 0) ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");
        (counter = new Timer("ShutdownCounter", true)).scheduleAtFixedRate(new ShutdownCounter(), 0L, 1000L);
    }

    public void schedule(final String time, final int shutdownMode) {
        SchedulingPattern cronTime;
        try {
            cronTime = new SchedulingPattern(time);
        } catch (InvalidPatternException e) {
            return;
        }
        final int seconds = (int) (cronTime.next(System.currentTimeMillis()) / 1000L - System.currentTimeMillis() / 1000L);
        schedule(seconds, shutdownMode);
    }

    public synchronized void cancel() {
        shutdownMode = -1;
        if (counter != null) {
            counter.cancel();
        }
        counter = null;
    }

    @Override
    public void run() {
        LOGGER.info("Shutting down LS/GS communication...");
        AuthServerCommunication.getInstance().shutdown();
        LOGGER.info("Disconnecting players...");
        disconnectAllPlayers();
        LOGGER.info("Saving data...");
        saveData();
        NoblessManager.getInstance().SaveNobleses();
        if (Config.OLY_ENABLED) {
            try {
                OlympiadPlayersManager.getInstance().FreePools();
                OlympiadStadiumManager.getInstance().FreeStadiums();
                OlympiadSystemManager.getInstance().shutdown();
                LOGGER.info("Olympiad System: Olympiad stoped and data saved!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            LOGGER.info("Shutting down thread pool...");
            ThreadPoolManager.getInstance().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("Shutting down selector...");
        if (GameServer.getInstance() != null) {
            GameServer.cancelCheckSelector();
            Arrays.stream(GameServer.getInstance().getSelectorThreads()).forEach(st -> {
                try {
                    st.shutdown();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            });
        }
        try {
            LOGGER.info("Shutting down database communication...");
            DatabaseFactory.getInstance().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("Shutdown finished.");
    }

    private void saveData() {
        try {
            if (!SevenSigns.getInstance().isSealValidationPeriod()) {
                SevenSignsFestival.getInstance().saveFestivalData(false);
                LOGGER.info("SevenSignsFestival: Data saved.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SevenSigns.getInstance().saveSevenSignsData(0, true);
            LOGGER.info("SevenSigns: Data saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Config.ALLOW_WEDDING) {
            try {
                CoupleManager.getInstance().store();
                LOGGER.info("CoupleManager: Data saved.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FishingChampionShipManager.getInstance().shutdown();
            LOGGER.info("FishingChampionShipManager: Data saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HeroManager.getInstance().saveHeroes();
            LOGGER.info("Hero: Data saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final Collection<Residence> residences = ResidenceHolder.getInstance().getResidences();
            residences.forEach(JdbcEntity::update);
            LOGGER.info("Residences: Data saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Config.ALLOW_CURSED_WEAPONS) {
            try {
                CursedWeaponsManager.getInstance().saveData();
                LOGGER.info("CursedWeaponsManager: Data saved,");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnectAllPlayers() {
        GameObjectsStorage.getAllPlayers().forEach(player -> {
            try {
                player.logout();
            } catch (Exception e) {
                LOGGER.info("Error while disconnecting: " + player + "!");
                e.printStackTrace();
            }
        });
    }

    private class ShutdownCounter extends TimerTask {
        @Override
        public void run() {
            switch (shutdownCounter) {
                case 1800:
                case 900:
                case 600:
                case 300:
                case 240:
                case 180:
                case 120:
                case 60: {
                    switch (shutdownMode) {
                        case SHUTDOWN: {
                            Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_MINUTES", new String[]{String.valueOf(shutdownCounter / 60)});
                            break;
                        }
                        case RESTART: {
                            Announcements.getInstance().announceByCustomMessage("THE_SERVER_WILL_BE_COMING_RESTARTED_IN_S1_MINUTES", new String[]{String.valueOf(shutdownCounter / 60)});
                            break;
                        }
                    }
                    break;
                }
                case 30:
                case 20:
                case 10:
                case 9:
                case 8:
                case 7:
                case 6:
                case 5:
                case 4:
                case 3:
                case 2:
                case 1: {
                    Announcements.getInstance().announceToAll(new SystemMessage(1).addNumber(shutdownCounter));
                    break;
                }
                case 0: {
                    switch (shutdownMode) {
                        case SHUTDOWN: {
                            GameServer.getInstance().getListeners().onShutdown();
                            Runtime.getRuntime().exit(SHUTDOWN);
                            break;
                        }
                        case RESTART: {
                            Runtime.getRuntime().exit(RESTART);
                            break;
                        }
                    }
                    cancel();
                    return;
                }
            }
            shutdownCounter--;
        }
    }
}
