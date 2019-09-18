package ru.j2dev.authserver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.crypt.PasswordHash;
import ru.j2dev.authserver.crypt.ScrambledKeyPair;
import ru.j2dev.commons.configuration.PropertiesParser;
import ru.j2dev.commons.util.Rnd;

import java.io.File;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.*;
import java.util.stream.IntStream;

public class Config {
    public static final String LOGIN_CONFIGURATION_FILE = "config/authserver.ini";
    public static final String SERVER_NAMES_FILE = "config/servername.xml";
    public static final String PROXY_SERVERS_FILE = "config/proxyservers.xml";
    public static final Map<Integer, String> SERVER_NAMES = new HashMap<>();
    public static final long LOGIN_TIMEOUT = 60000L;
    public static final Set<String> WHITE_IPS = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    public static String LOGIN_HOST;
    public static int PORT_LOGIN;
    public static String GAME_SERVER_LOGIN_HOST;
    public static int GAME_SERVER_LOGIN_PORT;
    public static long GAME_SERVER_PING_DELAY;
    public static int GAME_SERVER_PING_RETRY;
    public static String DEFAULT_PASSWORD_HASH;
    public static String LEGACY_PASSWORD_HASH;
    public static int LOGIN_BLOWFISH_KEYS;
    public static int LOGIN_RSA_KEYPAIRS;
    public static boolean ACCEPT_NEW_GAMESERVER;
    public static boolean AUTO_CREATE_ACCOUNTS;
    public static String ANAME_TEMPLATE;
    public static String APASSWD_TEMPLATE;
    public static int LOGIN_TRY_BEFORE_BAN;
    public static long LOGIN_TRY_TIMEOUT;
    public static long IP_BAN_TIME;
    public static PasswordHash DEFAULT_CRYPT;
    public static PasswordHash[] LEGACY_CRYPT;
    public static boolean LOGIN_LOG;
    public static ProxyServerConfig[] PROXY_SERVERS_CONFIGS;
    private static ScrambledKeyPair[] _keyPairs;
    private static byte[][] _blowfishKeys;



    public static void load() {
        loadConfiguration();
        loadServerNames();
        loadServerProxies();
    }

    public static void initCrypt() throws Throwable {
        DEFAULT_CRYPT = new PasswordHash(DEFAULT_PASSWORD_HASH);
        LEGACY_CRYPT = Arrays.stream(LEGACY_PASSWORD_HASH.split(";")).filter(method -> !method.equalsIgnoreCase(DEFAULT_PASSWORD_HASH)).map(PasswordHash::new).toArray(PasswordHash[]::new);
        LOGGER.info("Loaded {} as default crypt.", DEFAULT_PASSWORD_HASH);
        _keyPairs = new ScrambledKeyPair[LOGIN_RSA_KEYPAIRS];
        final KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);
        IntStream.range(0, _keyPairs.length).forEach(i -> _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair()));
        LOGGER.info("Cached {} KeyPairs for RSA communication", _keyPairs.length);
        _blowfishKeys = new byte[LOGIN_BLOWFISH_KEYS][16];
        for (int i = 0; i < _blowfishKeys.length; ++i) {
            for (int j = 0; j < _blowfishKeys[i].length; ++j) {
                _blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
            }
        }
        LOGGER.info("Stored {} keys for Blowfish communication", _blowfishKeys.length);
    }

    public static void loadServerNames() {
        SERVER_NAMES.clear();
        try {
            final SAXBuilder reader = new SAXBuilder();
            final Document document = reader.build(new File("config/servername.xml"));
            final Element root = document.getRootElement();
            root.getChildren().stream().filter(node -> "server".equalsIgnoreCase(node.getName())).forEach(node -> {
                final Integer id = Integer.valueOf(node.getAttributeValue("id"));
                final String name = node.getAttributeValue("name");
                SERVER_NAMES.put(id, name);
            });
            LOGGER.info("Loaded {} server names", SERVER_NAMES.size());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static void loadServerProxies() {
        final List<ProxyServerConfig> proxyServersConfigs = new ArrayList<>();
        try {
            final SAXBuilder reader = new SAXBuilder();
            final Document document = reader.build(new File("config/proxyservers.xml"));
            final Element root = document.getRootElement();
            root.getChildren().stream().filter(node -> "proxyServer".equalsIgnoreCase(node.getName())).forEach(node -> {
                final int origSrvId = Integer.parseInt(node.getAttributeValue("origId"));
                final int proxySrvId = Integer.parseInt(node.getAttributeValue("proxyId"));
                final String proxyHost = node.getAttributeValue("proxyHost");
                final int proxyPort = Integer.parseInt(node.getAttributeValue("proxyPort"));
                final ProxyServerConfig psc = new ProxyServerConfig(origSrvId, proxySrvId, proxyHost, proxyPort);
                proxyServersConfigs.add(psc);
            });
        } catch (Exception ex) {
            LOGGER.error("Can't load proxy server's config", ex);
        }
        PROXY_SERVERS_CONFIGS = proxyServersConfigs.toArray(new ProxyServerConfig[0]);
    }

    public static void loadConfiguration() {
        final PropertiesParser serverSettings = load(LOGIN_CONFIGURATION_FILE);
        LOGIN_HOST = serverSettings.getProperty("LoginserverHostname", "127.0.0.1");
        PORT_LOGIN = serverSettings.getProperty("LoginserverPort", 2106);
        GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
        GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9014);
        LOGIN_BLOWFISH_KEYS = serverSettings.getProperty("BlowFishKeys", 20);
        LOGIN_RSA_KEYPAIRS = serverSettings.getProperty("RSAKeyPairs", 10);
        ACCEPT_NEW_GAMESERVER = serverSettings.getProperty("AcceptNewGameServer", true);
        DEFAULT_PASSWORD_HASH = serverSettings.getProperty("PasswordHash", "sha1");
        LEGACY_PASSWORD_HASH = serverSettings.getProperty("LegacyPasswordHash", "whirlpool2");
        AUTO_CREATE_ACCOUNTS = serverSettings.getProperty("AutoCreateAccounts", true);
        ANAME_TEMPLATE = serverSettings.getProperty("AccountTemplate", "[A-Za-z0-9]{4,14}");
        APASSWD_TEMPLATE = serverSettings.getProperty("PasswordTemplate", "[A-Za-z0-9]{4,16}");
        LOGIN_TRY_BEFORE_BAN = serverSettings.getProperty("LoginTryBeforeBan", 10);
        LOGIN_TRY_TIMEOUT = serverSettings.getProperty("LoginTryTimeout", 5) * 1000L;
        IP_BAN_TIME = serverSettings.getProperty("IpBanTime", 300) * 1000L;
        WHITE_IPS.addAll(Arrays.asList(serverSettings.getProperty("WhiteIpList", new String[]{"127.0.0.1"})));
        GAME_SERVER_PING_DELAY = serverSettings.getProperty("GameServerPingDelay", 30) * 1000L;
        GAME_SERVER_PING_RETRY = serverSettings.getProperty("GameServerPingRetry", 4);
        LOGIN_LOG = serverSettings.getProperty("LoginLog", true);
    }

    public static PropertiesParser load(final String filename) {
        return load(new File(filename));
    }

    public static PropertiesParser load(final File file) {
        final PropertiesParser result = new PropertiesParser();
        try {
            result.load(file);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return result;
    }

    public static ScrambledKeyPair getScrambledRSAKeyPair() {
        return _keyPairs[Rnd.get(_keyPairs.length)];
    }

    public static byte[] getBlowfishKey() {
        return _blowfishKeys[Rnd.get(_blowfishKeys.length)];
    }

    public static class ProxyServerConfig {
        private final int _origServerId;
        private final int _proxyServerId;
        private final String _porxyHost;
        private final int _proxyPort;

        public ProxyServerConfig(final int origServerId, final int proxyServerId, final String porxyHost, final int proxyPort) {
            _origServerId = origServerId;
            _proxyServerId = proxyServerId;
            _porxyHost = porxyHost;
            _proxyPort = proxyPort;
        }

        public int getOrigServerId() {
            return _origServerId;
        }

        public int getProxyId() {
            return _proxyServerId;
        }

        public String getPorxyHost() {
            return _porxyHost;
        }

        public int getProxyPort() {
            return _proxyPort;
        }
    }
}
