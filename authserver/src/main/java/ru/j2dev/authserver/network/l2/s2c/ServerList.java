package ru.j2dev.authserver.network.l2.s2c;

import ru.j2dev.authserver.GameServerManager;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.network.gamecomm.GameServer;
import ru.j2dev.authserver.network.gamecomm.ProxyServer;
import ru.j2dev.commons.net.utils.NetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ServerList extends L2LoginServerPacket {
    private static final Comparator<ServerData> SERVER_DATA_COMPARATOR = Comparator.comparingInt(o -> o.serverId);
    private List<ServerData> _servers;
    private int _lastServer;

    public ServerList(final Account account) {
        _servers = new ArrayList<>();
        _lastServer = account.getLastServer();
        for (final GameServer gs : GameServerManager.getInstance().getGameServers()) {
                InetAddress ip;
                try {
                    ip = (NetUtils.isInternalIP(account.getLastIP()) ? gs.getInternalHost() : gs.getExternalHost());
                } catch (UnknownHostException e) {
                    break;
                }
                _servers.add(new ServerData(gs.getId(), ip, gs.getPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), gs.getAgeLimit()));
                final List<ProxyServer> proxyServers = GameServerManager.getInstance().getProxyServersList(gs.getId());
            proxyServers.forEach(ps -> _servers.add(new ServerData(ps.getProxyServerId(), ps.getProxyAddr(), ps.getProxyPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), gs.getAgeLimit())));
        }
        _servers.sort(ServerList.SERVER_DATA_COMPARATOR);
    }

    @Override
    protected void writeImpl() {
        writeC(0x4);
        writeC(_servers.size());
        writeC(_lastServer);
        _servers.forEach(server -> {
            writeC(server.serverId);
            final InetAddress i4 = server.ip;
            final byte[] raw = i4.getAddress();
            writeC(raw[0] & 0xFF);
            writeC(raw[1] & 0xFF);
            writeC(raw[2] & 0xFF);
            writeC(raw[3] & 0xFF);
            writeD(server.port);
            writeC(server.ageLimit);
            writeC(server.pvp ? 1 : 0);
            writeH(server.online);
            writeH(server.maxPlayers);
            writeC(server.status ? 1 : 0);
            writeD(server.type);
            writeC(server.brackets ? 1 : 0);
        });
    }

    private static class ServerData {
        int serverId;
        InetAddress ip;
        int port;
        int online;
        int maxPlayers;
        boolean status;
        boolean pvp;
        boolean brackets;
        int type;
        int ageLimit;

        ServerData(final int serverId, final InetAddress ip, final int port, final boolean pvp, final boolean brackets, final int type, final int online, final int maxPlayers, final boolean status, final int ageLimit) {
            this.serverId = serverId;
            this.ip = ip;
            this.port = port;
            this.pvp = pvp;
            this.brackets = brackets;
            this.type = type;
            this.online = online;
            this.maxPlayers = maxPlayers;
            this.status = status;
            this.ageLimit = ageLimit;
        }
    }
}
