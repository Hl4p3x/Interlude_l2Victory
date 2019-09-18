package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.net.nio.impl.ReceivablePacket;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.utils.Log;

import java.nio.BufferUnderflowException;
import java.util.List;

public abstract class L2GameClientPacket extends ReceivablePacket<GameClient> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(L2GameClientPacket.class);

    @Override
    public final boolean read() {
        try {
            readImpl();
            if (Config.LOG_CLIENT_PACKETS) {
                Log.clientPacket("PacketOpcode : " + getType() + " to Client : " + _client.toString());
            }
            return true;
        } catch (BufferUnderflowException e) {
            _client.onPacketReadFail();
            LOGGER.error("Client: " + _client.toString() + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        } catch (Exception e2) {
            LOGGER.error("Client: " + _client.toString() + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e2);
        }
        return false;
    }

    protected abstract void readImpl();

    @Override
    public final void run() {
        final GameClient client = getClient();
        try {
            runImpl();
        } catch (Exception e) {
            LOGGER.error("Client: " + client + " - Failed running: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        }
    }

    protected abstract void runImpl() throws Exception;

    protected String readS(final int len) {
        final String ret = readS();
        return (ret.length() > len) ? ret.substring(0, len) : ret;
    }

    protected void sendPacket(final L2GameServerPacket packet) {
        getClient().sendPacket(packet);
    }

    protected void sendPacket(final L2GameServerPacket... packets) {
        getClient().sendPacket(packets);
    }

    protected void sendPackets(final List<L2GameServerPacket> packets) {
        getClient().sendPackets(packets);
    }

    public String getType() {
        return "[C] " + getClass().getSimpleName();
    }
}
