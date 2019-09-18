package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.wrappers.IServerInterface;
import ru.akumu.smartguard.core.wrappers.IWorld;
import ru.akumu.smartguard.core.wrappers.db.IConnectionFactory;


public final class ServerInterface extends IServerInterface {
    private static final ServerInterface INSTANCE = new ServerInterface();

    public ServerInterface() {
        protocol = 4;
    }

    public static ServerInterface getInstance() {
        return ServerInterface.INSTANCE;
    }

    @Override
    public IConnectionFactory getConnectionFactory() {
        return DBConnectionFactory.getInstance();
    }

    @Override
    public IWorld getWorld() {
        return WorldWrapper.getInstance();
    }
}