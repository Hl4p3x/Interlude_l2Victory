package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.KeyPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendStatus;

public class SGProtocolVersionPacket extends L2GameClientPacket {
    private int _version;

    @Override
    protected void readImpl() {
        _version = readD();
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (_version == -2) {
            client.closeNow(false);
            return;
        }
        if (_version == -3) {
            LOGGER.info("Status request from IP : " + client.getIpAddr());
            client.close(new SendStatus());
            return;
        }
        if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION) {
            LOGGER.warn("Unknown protocol revision : " + _version + ", client : " + _client);
            client.close(new KeyPacket(null));
            return;
        }
        client.setRevision(_version);
        sendPacket(new SGKeyPacket(client.enableCrypt()));
    }
}