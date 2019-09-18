package ru.j2dev.gameserver.network.lineage2.cgm.sxg;

import org.strixplatform.StrixPlatform;
import org.strixplatform.utils.StrixClientData;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

public class SXGKeyPacket extends L2GameServerPacket {
    public static final SXGKeyPacket NULL_KEY_PACKET = new SXGKeyPacket(null);
    private final byte[] key;
    private final StrixClientData clientData;

    public SXGKeyPacket(final byte[] key) {
        this.key = key;
        clientData = null;
    }

    public SXGKeyPacket(final byte[] key, final StrixClientData clientData) {
        this.key = key;
        this.clientData = clientData;
    }

    @Override
    protected void writeImpl() {
        writeC(0);
        if (key == null || key.length == 0) {
            if (StrixPlatform.getInstance().isBackNotificationEnabled() && clientData != null) {
                writeC(clientData.getServerResponse().ordinal());
            }
            return;
        }
        writeC(1);
        writeB(key);
        writeD(1);
        writeD(0);
    }

}
