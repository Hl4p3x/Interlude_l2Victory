package ru.j2dev.gameserver.network.telnet;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import ru.j2dev.gameserver.Config;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class TelnetServer {
    public TelnetServer() {
        final ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1), 1));
        final TelnetServerHandler handler = new TelnetServerHandler();
        bootstrap.setPipelineFactory(new TelnetPipelineFactory(handler));
        bootstrap.bind(new InetSocketAddress("*".equals(Config.TELNET_HOSTNAME) ? null : Config.TELNET_HOSTNAME, Config.TELNET_PORT));
    }
}
