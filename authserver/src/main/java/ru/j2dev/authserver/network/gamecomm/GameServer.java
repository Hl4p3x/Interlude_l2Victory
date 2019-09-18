package ru.j2dev.authserver.network.gamecomm;

import org.apache.log4j.Logger;
import ru.j2dev.authserver.Config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {
    private static final Logger LOGGER = Logger.getLogger(GameServer.class);
    private final AtomicInteger _port;
    private int _id;
    private String _internalHost;
    private String _externalHost;
    private InetAddress _internalAddr;
    private InetAddress _externalAddr;
    private volatile int[] _ports;
    private int _serverType;
    private int _ageLimit;
    private boolean _isOnline;
    private boolean _isPvp;
    private boolean _isShowingBrackets;
    private boolean _isGmOnly;
    private int _maxPlayers;
    private GameServerConnection _conn;
    private boolean _isAuthed;
    private volatile int _playersIngame;

    public GameServer(final GameServerConnection conn) {
        _ports = new int[]{7777};
        _port = new AtomicInteger(0);
        _conn = conn;
    }

    public GameServer(final int id) {
        _ports = new int[]{7777};
        _port = new AtomicInteger(0);
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void setId(final int id) {
        _id = id;
    }

    public boolean isAuthed() {
        return _isAuthed;
    }

    public void setAuthed(final boolean isAuthed) {
        _isAuthed = isAuthed;
    }

    public GameServerConnection getConnection() {
        return _conn;
    }

    public void setConnection(final GameServerConnection conn) {
        _conn = conn;
    }

    public InetAddress getInternalHost() throws UnknownHostException {
        if (_internalAddr != null) {
            return _internalAddr;
        }
        return _internalAddr = InetAddress.getByName(_internalHost);
    }

    public void setInternalHost(String internalHost) {
        if ("*".equals(internalHost)) {
            internalHost = getConnection().getIpAddress();
        }
        _internalHost = internalHost;
        _internalAddr = null;
    }

    public InetAddress getExternalHost() throws UnknownHostException {
        if (_externalAddr != null) {
            return _externalAddr;
        }
        return _externalAddr = InetAddress.getByName(_externalHost);
    }

    public void setExternalHost(String externalHost) {
        if ("*".equals(externalHost)) {
            externalHost = getConnection().getIpAddress();
        }
        _externalHost = externalHost;
        _externalAddr = null;
    }

    public int getPort() {
        final int[] ports = _ports;
        return ports[(_port.incrementAndGet() & Integer.MAX_VALUE) % ports.length];
    }

    public void setPorts(final int[] ports) {
        _ports = ports;
    }

    public int getMaxPlayers() {
        return _maxPlayers;
    }

    public void setMaxPlayers(final int maxPlayers) {
        _maxPlayers = maxPlayers;
    }

    public int getOnline() {
        return _playersIngame;
    }

    public void addAccount(final String account) {
        _playersIngame++;
    }

    public void removeAccount(final String account) {
        _playersIngame--;
    }

    public void setDown() {
        setAuthed(false);
        setConnection(null);
        setOnline(false);
    }

    public String getName() {
        return Config.SERVER_NAMES.get(getId());
    }

    public void sendPacket(final SendablePacket packet) {
        final GameServerConnection conn = getConnection();
        if (conn != null) {
            conn.sendPacket(packet);
        }
    }

    public int getServerType() {
        return _serverType;
    }

    public void setServerType(final int serverType) {
        _serverType = serverType;
    }

    public boolean isOnline() {
        return _isOnline;
    }

    public void setOnline(final boolean online) {
        _isOnline = online;
    }

    public boolean isPvp() {
        return _isPvp;
    }

    public void setPvp(final boolean pvp) {
        _isPvp = pvp;
    }

    public boolean isShowingBrackets() {
        return _isShowingBrackets;
    }

    public void setShowingBrackets(final boolean showingBrackets) {
        _isShowingBrackets = showingBrackets;
    }

    public boolean isGmOnly() {
        return _isGmOnly;
    }

    public void setGmOnly(final boolean gmOnly) {
        _isGmOnly = gmOnly;
    }

    public int getAgeLimit() {
        return _ageLimit;
    }

    public void setAgeLimit(final int ageLimit) {
        _ageLimit = ageLimit;
    }

    public void setProtocol(final int protocol) {
        int _protocol = protocol;
    }
}
