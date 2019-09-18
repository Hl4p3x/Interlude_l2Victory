package ru.j2dev.authserver.network.l2;

import ru.j2dev.authserver.IpBanManager;
import ru.j2dev.authserver.ThreadPoolManager;
import ru.j2dev.authserver.network.l2.s2c.Init;
import ru.j2dev.commons.net.nio.impl.IAcceptFilter;
import ru.j2dev.commons.net.nio.impl.IClientFactory;
import ru.j2dev.commons.net.nio.impl.IMMOExecutor;
import ru.j2dev.commons.net.nio.impl.MMOConnection;
import ru.j2dev.commons.threading.RunnableImpl;

import java.nio.channels.SocketChannel;

public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter {
    @Override
    public void execute(final Runnable r) {
        ThreadPoolManager.getInstance().execute(r);
    }

    @Override
    public L2LoginClient create(final MMOConnection<L2LoginClient> con) {
        final L2LoginClient client = new L2LoginClient(con);
        client.sendPacket(new Init(client));
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                client.closeNow(false);
            }
        }, 60000L);
        return client;
    }

    @Override
    public boolean accept(final SocketChannel sc) {
        return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
    }
}
