package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExListMpccWaiting;

public class RequestExListMpccWaiting extends L2GameClientPacket {
    private int _listId;
    private int _locationId;
    private boolean _allLevels;

    @Override
    protected void readImpl() {
        _listId = readD();
        _locationId = readD();
        _allLevels = (readD() == 1);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        player.sendPacket(new ExListMpccWaiting(player, _listId, _locationId, _allLevels));
    }
}
