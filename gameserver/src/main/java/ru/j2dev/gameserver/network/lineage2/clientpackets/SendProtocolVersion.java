package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.serverpackets.KeyPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendStatus;

public class SendProtocolVersion extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendProtocolVersion.class);

    private int _version;

    @Override
    protected void readImpl() {
        _version = readD();
    }

    @Override
    protected void runImpl() {
        if (_version == -2) {
            _client.closeNow(false);
            return;
        }
        if (_version == -3) {
            LOGGER.info("Status request from IP : " + getClient().getIpAddr());
            getClient().close(new SendStatus());
            return;
        }
        if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION) {
            LOGGER.warn("Unknown protocol revision : " + _version + ", client : " + _client);
            getClient().close(new KeyPacket(null));
            return;
        }
        getClient().setRevision(_version);
        sendPacket(new KeyPacket(_client.enableCrypt()));
    }
}
