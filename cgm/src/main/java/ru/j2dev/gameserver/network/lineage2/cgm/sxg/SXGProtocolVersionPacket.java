package ru.j2dev.gameserver.network.lineage2.cgm.sxg;

import org.strixplatform.StrixPlatform;
import org.strixplatform.managers.ClientGameSessionManager;
import org.strixplatform.managers.ClientProtocolDataManager;
import org.strixplatform.utils.StrixClientData;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.KeyPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendStatus;

public class SXGProtocolVersionPacket extends L2GameClientPacket {

    private int _version;
    private byte[] _data;
    private int dataChecksum;

    @Override
    protected void readImpl() {
        _version = readD();
        if (StrixPlatform.getInstance().isPlatformEnabled()) {
            try {
                if (_buf.remaining() >= StrixPlatform.getInstance().getProtocolVersionDataSize()) {
                    readB(_data = new byte[StrixPlatform.getInstance().getClientDataSize()]);
                    dataChecksum = readD();
                }
            } catch (Exception e) {
                _data = null;
            }
        }
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (_version == -2) {
            client.closeNow(false);
            return;
        }
        if (_version == -3) {
            LOGGER.info("Status request from IP : " + getClient().getIpAddr());
            client.close(new SendStatus());
            return;
        }
        if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION) {
            LOGGER.warn("Unknown protocol revision : " + _version + ", client : " + client);
            client.close(SXGKeyPacket.NULL_KEY_PACKET);
            return;
        }
        if (StrixPlatform.getInstance().isPlatformEnabled() && _data == null) {
            client.close(new KeyPacket(null));
            LOGGER.info("Used un-protected system patch. Patch version: " + _version);
            return;
        }
        if (!StrixPlatform.getInstance().isPlatformEnabled()) {
            client.setRevision(_version);
            sendPacket(new KeyPacket(_client.enableCrypt()));
            return;
        }
        try {
            final StrixClientData clientData = ClientProtocolDataManager.getInstance().getDecodedData(_data, dataChecksum);
            if (clientData != null) {
                if (!ClientGameSessionManager.getInstance().checkServerResponse(clientData)) {
                    getClient().close(SXGKeyPacket.NULL_KEY_PACKET);
                    return;
                }
                client.setRevision(_version);
                client.setHwid(clientData.getClientHWID());
                sendPacket(new SXGKeyPacket(getClient().enableCrypt(), clientData));
                return;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        LOGGER.error("Decode client data failed. See Strix-Platform log file. Disconected client " + getClient().getIpAddr());
        client.close(SXGKeyPacket.NULL_KEY_PACKET);
    }
}
