package ru.j2dev.gameserver.network.lineage2.cgm.lg;

import com.lameguard.LameGuard;
import com.lameguard.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.KeyPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendStatus;

public class LGProtocolVersionPacket extends L2GameClientPacket {
    private static final Logger LOG = LoggerFactory.getLogger(LGProtocolVersionPacket.class);

    private int _version;
    private byte[] _data;
    private byte[] _check;

    @Override
    protected void readImpl() {
        _version = readD();
        if (getAvaliableBytes() == 260) {
            _data = new byte[256];
            _check = new byte[4];
            readB(_data);
            readB(_check);
        }
    }

    @Override
    protected void runImpl() throws Exception {
        final GameClient client = getClient();
        if (_version == -2) {
            client.closeNow(false);
            return;
        }
        if (_version == -3) {
            LGProtocolVersionPacket.LOG.info("Status request from IP : " + client.getIpAddr());
            client.close(new SendStatus());
            return;
        }
        if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION) {
            LGProtocolVersionPacket.LOG.warn("Unknown protocol revision : " + _version + ", client : " + _client);
            client.close(new KeyPacket(null));
            return;
        }
        if (!LameGuard.getInstance().checkData(_data, _check)) {
            client.closeNow(true);
            return;
        }
        final ClientSession clientSession = LameGuard.getInstance().checkClient(client.getIpAddr(), _data);
        if (clientSession == null) {
            client.closeNow(true);
            return;
        }
        final byte[] key = client.enableCrypt();
        final byte[] keyData = LameGuard.getInstance().assembleAnswer(clientSession, key);
        if (keyData == null) {
            client.closeNow(true);
            return;
        }
        client.setHwid(clientSession.getHWID());
        getClient().setRevision(_version);
        sendPacket(new LGKeyPacket(keyData));
    }
}