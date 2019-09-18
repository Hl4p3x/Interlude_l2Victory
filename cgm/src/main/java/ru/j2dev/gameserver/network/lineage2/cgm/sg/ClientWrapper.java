package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.manager.session.HWID;
import ru.akumu.smartguard.core.wrappers.ISmartClient;
import ru.akumu.smartguard.core.wrappers.ISmartPlayer;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ServerClose;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.nio.ByteBuffer;


public final class ClientWrapper extends ISmartClient {
    private GameClient _client;
    private PlayerWrapper _playerWrapper;

    public ClientWrapper(final GameClient client) {
        _client = client;
    }

    private static L2GameServerPacket makePacketWithPayload(final ByteBuffer payloadBuff) {
        final byte[] payload = new byte[payloadBuff.position() + 1];
        payloadBuff.position(0);
        payloadBuff.get(payload, 0, payload.length);
        return new L2GameServerPacket() {
            @Override
            protected void writeImpl() {
                writeB(payload);
            }
        };
    }

    private GameClient getClient() {
        if (_client != null && !_client.isConnected()) {
            _client = null;
            _playerWrapper = null;
        }
        return _client;
    }

    @Override
    public ISmartClient.NetworkStatus getConnectionStatus() {
        if (getClient() != null) {
            switch (getClient().getState()) {
                case IN_GAME: {
                    return ISmartClient.NetworkStatus.IN_GAME;
                }
                case CONNECTED:
                case AUTHED: {
                    return ISmartClient.NetworkStatus.CONNECTED;
                }
                case DISCONNECTED: {
                    return ISmartClient.NetworkStatus.DISCONNECTED;
                }
            }
        }
        return ISmartClient.NetworkStatus.DISCONNECTED;
    }

    @Override
    public void setHWID(final HWID hwid) {
        if (getClient() != null && hwid != null) {
            getClient().setHwid(hwid.toString());
        }
    }

    @Override
    public void closeConnection(final boolean b) {
        if (getClient() != null) {
            if (b) {
                getClient().close(ServerClose.STATIC);
            } else {
                getClient().closeLater();
            }
        }
    }

    @Override
    public void sendRawPacket(final ByteBuffer payloadBuff) {
        if (getClient() != null) {
            getClient().sendPacket(makePacketWithPayload(payloadBuff));
        }
    }

    @Override
    public void closeWithRawPacket(final ByteBuffer payloadBuff) {
        if (getClient() != null) {
            getClient().close(makePacketWithPayload(payloadBuff));
        }
    }

    @Override
    public void sendHtml(final String html) {
        if (getClient() != null && html != null) {
            final NpcHtmlMessage htmlPkt = new NpcHtmlMessage(5);
            htmlPkt.setHtml(html);
            getClient().sendPacket(htmlPkt);
        }
    }

    @Override
    public void sendMessage(final String msgText) {
        if (getClient() != null && msgText != null) {
            getClient().sendPacket(new SystemMessage(msgText));
        }
    }

    @Override
    public String getAccountName() {
        return (getClient() != null) ? getClient().getLogin() : null;
    }

    @Override
    public String getIpAddr() {
        return (getClient() != null) ? getClient().getIpAddr() : null;
    }

    @Override
    public ISmartPlayer getPlayer() {
        if (getClient() != null) {
            final Player player = getClient().getActiveChar();
            if (player != null) {
                if (_playerWrapper == null || _playerWrapper.getPlayer() != player) {
                    return _playerWrapper = new PlayerWrapper(player, this);
                }
                return _playerWrapper;
            } else {
                _playerWrapper = null;
            }
        }
        return null;
    }
}