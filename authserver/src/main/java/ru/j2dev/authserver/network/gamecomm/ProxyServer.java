package ru.j2dev.authserver.network.gamecomm;

import ru.j2dev.authserver.GameServerManager;

import java.net.InetAddress;

public class ProxyServer {
    private final int _origServerId;
    private final int _proxyServerId;
    private InetAddress _proxyAddr;
    private int _proxyPort;

    public ProxyServer(final int origServerId, final int proxyServerId) {
        _origServerId = origServerId;
        _proxyServerId = proxyServerId;
    }

    public int getOrigServerId() {
        return _origServerId;
    }

    public int getProxyServerId() {
        return _proxyServerId;
    }

    public InetAddress getProxyAddr() {
        return _proxyAddr;
    }

    public void setProxyAddr(final InetAddress proxyAddr) {
        _proxyAddr = proxyAddr;
    }

    public int getProxyPort() {
        return _proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        _proxyPort = proxyPort;
    }

    public GameServer getGameServer() {
        return GameServerManager.getInstance().getGameServerById(getOrigServerId());
    }
}
