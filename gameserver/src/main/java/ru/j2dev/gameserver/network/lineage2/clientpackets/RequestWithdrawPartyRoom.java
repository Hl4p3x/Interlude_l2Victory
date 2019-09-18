package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

public class RequestWithdrawPartyRoom extends L2GameClientPacket {
    private int _roomId;

    @Override
    protected void readImpl() {
        _roomId = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final MatchingRoom room = player.getMatchingRoom();
        if (room.getId() != _roomId || room.getType() != MatchingRoom.PARTY_MATCHING) {
            return;
        }
        if (room.getLeader() == player) {
            return;
        }
        room.removeMember(player, false);
    }
}
