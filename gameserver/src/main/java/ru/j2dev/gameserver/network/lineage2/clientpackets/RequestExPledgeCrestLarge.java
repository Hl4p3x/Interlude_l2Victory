package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.CrestCache;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPledgeCrestLarge;

public class RequestExPledgeCrestLarge extends L2GameClientPacket {
    private int _crestId;

    @Override
    protected void readImpl() {
        _crestId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_crestId == 0) {
            return;
        }
        final byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
        if (data != null) {
            final ExPledgeCrestLarge pcl = new ExPledgeCrestLarge(_crestId, data);
            sendPacket(pcl);
        }
    }
}
