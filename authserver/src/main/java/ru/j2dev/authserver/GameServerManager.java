package ru.j2dev.authserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.database.DatabaseFactory;
import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ProxyServer;
import ru.j2dev.commons.dbutils.DbUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServerManager {
    private static final Logger LOG = LoggerFactory.getLogger(GameServerManager.class);
    private static final GameServerManager INSTANCE = new GameServerManager();

    private final Map<Integer, GameServer> _gameServers;
    private final Map<Integer, List<ProxyServer>> _gameServerProxys;
    private final Map<Integer, ProxyServer> _proxyServers;
    private final Lock _readLock;
    private final Lock _writeLock;

    public GameServerManager() {
        _gameServers = new TreeMap<>();
        _gameServerProxys = new TreeMap<>();
        _proxyServers = new TreeMap<>();
        ReadWriteLock _lock = new ReentrantReadWriteLock();
        _readLock = _lock.readLock();
        _writeLock = _lock.writeLock();
        loadGameServers();
        LOG.info("Loaded " + _gameServers.size() + " registered GameServer(s).");
        loadProxyServers();
        LOG.info("Loaded " + _proxyServers.size() + " proxy server(s).");
    }

    public static GameServerManager getInstance() {
        return INSTANCE;
    }

    private void loadGameServers() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT server_id FROM gameservers");
            rset = statement.executeQuery();
            while (rset.next()) {
                final int id = rset.getInt("server_id");
                for (final Config.ProxyServerConfig psc : Config.PROXY_SERVERS_CONFIGS) {
                    if (psc.getProxyId() == id) {
                        LOG.warn("Server with id " + id + " collides with proxy server.");
                    }
                }
                final GameServer gs = new GameServer(id);
                _gameServers.put(id, gs);
            }
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    private void loadProxyServers() {
        for (final Config.ProxyServerConfig psc : Config.PROXY_SERVERS_CONFIGS) {
                if (_gameServers.containsKey(psc.getProxyId())) {
                    LOG.warn("Won't load collided proxy with id " + psc.getProxyId() + ".");
                } else {
                    final ProxyServer ps = new ProxyServer(psc.getOrigServerId(), psc.getProxyId());
                    try {
                        final InetAddress inetAddress = InetAddress.getByName(psc.getPorxyHost());
                        ps.setProxyAddr(inetAddress);
                    } catch (UnknownHostException uhe) {
                        LOG.error("Can't load proxy", uhe);
                        break;
                    }
                    ps.setProxyPort(psc.getProxyPort());
                    List<ProxyServer> proxyList = _gameServerProxys.computeIfAbsent(ps.getOrigServerId(), k -> new LinkedList<>());
                    proxyList.add(ps);
                    _proxyServers.put(ps.getProxyServerId(), ps);
                }
        }
    }

    public List<ProxyServer> getProxyServersList(final int gameServerId) {
        final List<ProxyServer> result = _gameServerProxys.get(gameServerId);
        return (result != null) ? result : Collections.emptyList();
    }

    public ProxyServer getProxyServerById(final int proxyServerId) {
        return _proxyServers.get(proxyServerId);
    }

    public GameServer[] getGameServers() {
        _readLock.lock();
        try {
            return _gameServers.values().toArray(new GameServer[0]);
        } finally {
            _readLock.unlock();
        }
    }

    public GameServer getGameServerById(final int id) {
        _readLock.lock();
        try {
            return _gameServers.get(id);
        } finally {
            _readLock.unlock();
        }
    }

    public boolean registerGameServer(final GameServer gs) {
        if (!Config.ACCEPT_NEW_GAMESERVER) {
            return false;
        }
        _writeLock.lock();
        try {
            int id = 1;
            while (id++ < 127) {
                final GameServer pgs = _gameServers.get(id);
                if (_proxyServers.containsKey(id) || pgs == null) {
                    _gameServers.put(id, gs);
                    gs.setId(id);
                    return true;
                }
            }
        } finally {
            _writeLock.unlock();
        }
        return false;
    }

    public boolean registerGameServer(final int id, final GameServer gs) {
        _writeLock.lock();
        try {
            final GameServer pgs = _gameServers.get(id);
            if (!Config.ACCEPT_NEW_GAMESERVER && pgs == null) {
                return false;
            }
            if (pgs == null || !pgs.isAuthed()) {
                _gameServers.put(id, gs);
                gs.setId(id);
                return true;
            }
        } finally {
            _writeLock.unlock();
        }
        return false;
    }
}
