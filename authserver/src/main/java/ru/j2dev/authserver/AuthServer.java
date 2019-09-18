package ru.j2dev.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.database.DatabaseFactory;
import ru.j2dev.authserver.network.gamecomm.GameServerCommunication;
import ru.j2dev.authserver.network.l2.L2LoginClient;
import ru.j2dev.authserver.network.l2.L2LoginPacketHandler;
import ru.j2dev.authserver.network.l2.SelectorHelper;
import ru.j2dev.commons.dbutils.SqlTableOptimizer;
import ru.j2dev.commons.lang.StatsUtils;
import ru.j2dev.commons.net.nio.impl.SelectorConfig;
import ru.j2dev.commons.net.nio.impl.SelectorThread;
import ru.j2dev.commons.versioning.Version;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

public class AuthServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServer.class);
    private static AuthServer authServer;

    private final GameServerCommunication _gameServerListener;

    public AuthServer() throws Throwable {
        copyLog("stdout");
        copyLog("java");

        Version version = new Version(AuthServer.class);
        LOGGER.info("|=====================COPYRIGHT=========================|");
        LOGGER.info("Team: .................... {}", version.getTeamName());
        LOGGER.info("Site: .................... {}", version.getTeamSite());
        LOGGER.info("Jar Signature: ............ {}", version.getJarSignature());
        LOGGER.info("|=====================COPYRIGHT=========================|");
        LOGGER.info("|======================VERSION==========================|");
        LOGGER.info("Revision: ................ {}", version.getRevisionNumber());
        LOGGER.info("Version: ................. {}", version.getVersionNumber());
        LOGGER.info("Build date: .............. {}", version.getBuildDate());
        LOGGER.info("Compiler version: ........ {}", version.getBuildJdk());
        LOGGER.info("|======================VERSION==========================|");
        LOGGER.info("|==================SYSTEM INFORMATION===================|");
        StatsUtils.printOSInfo();
        StatsUtils.printCpuInfo();
        StatsUtils.printPid();
        LOGGER.info("| {}", Version.getJavaInfo());
        LOGGER.info("|==================SYSTEM INFORMATION===================|");
        Config.initCrypt();
        GameServerManager.getInstance();
        final L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
        final SelectorHelper sh = new SelectorHelper();
        final SelectorConfig sc = new SelectorConfig();
        SelectorThread<L2LoginClient> _selectorThread = new SelectorThread<>(sc, loginPacketHandler, sh, sh, sh);
        (_gameServerListener = GameServerCommunication.getInstance()).openServerSocket("*".equals(Config.GAME_SERVER_LOGIN_HOST) ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST), Config.GAME_SERVER_LOGIN_PORT);
        _gameServerListener.start();
        LOGGER.info("Listening for gameservers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
        _selectorThread.openServerSocket("*".equals(Config.LOGIN_HOST) ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
        _selectorThread.start();
        LOGGER.info("Listening for clients on " + Config.LOGIN_HOST + ":" + Config.PORT_LOGIN);
        LOGGER.info("=================================================");
        System.gc();
        System.runFinalization();
        StatsUtils.printMemoryInfo();
        LOGGER.info("=================================================");
        Toolkit.getDefaultToolkit().beep();
    }

    public static AuthServer getInstance() {
        return authServer;
    }


    public static void checkFreePorts() throws Throwable {
        Optional<ServerSocket> ss = Optional.empty();

        try {
            if (Config.LOGIN_HOST.equalsIgnoreCase("*")) {
                ss = Optional.of(new ServerSocket(Config.PORT_LOGIN));
            } else {
                ss = Optional.of(new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGIN_HOST)));
            }
        } finally {
            ss.ifPresent(serverSocket -> {
                try {
                    serverSocket.close();
                } catch (Exception ignored) {
                }
            });
        }
    }

    public static void main(final String[] args) throws Throwable {
        new File("./log/").mkdir();
        Config.load();
        checkFreePorts();
        DatabaseFactory.getInstance().initPool("AuthServer");
        LOGGER.info("Checking database: ........");
        //SqlInstaller.checkDatabase(DatabaseFactory.getInstance().getConnection(), SQL_DIR);
        SqlTableOptimizer.repairTables(DatabaseFactory.getInstance().getConnection());
        SqlTableOptimizer.optimizeTables(DatabaseFactory.getInstance().getConnection());
        authServer = new AuthServer();
    }

    private void copyLog(final String name) {
        final File copyLog = new File("log/" + name + ".log");
        if (copyLog.exists()) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(copyLog.lastModified());
            final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy_HH-mm-ss");
            copyLog.renameTo(new File(String.format("log/%s/%s-%s.log", name, name, df.format(calendar.getTime()))));
        }
    }

    public GameServerCommunication getGameServerListener() {
        return _gameServerListener;
    }
}
