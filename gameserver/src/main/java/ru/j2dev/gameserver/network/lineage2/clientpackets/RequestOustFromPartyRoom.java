package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

public class RequestOustFromPartyRoom extends L2GameClientPacket {
    private int _objectId;

    @Override
    protected void readImpl() {
        _objectId = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        final MatchingRoom room = player.getMatchingRoom();
        if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING) {
            return;
        }
        if (room.getLeader() != player) {
            return;
        }
        final Player member = GameObjectsStorage.getPlayer(_objectId);
        if (member == null) {
            return;
        }
        if (member == room.getLeader()) {
            return;
        }
        room.removeMember(member, true);
    }
}
