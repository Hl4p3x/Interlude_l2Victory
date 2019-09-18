package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

public class RequestJoinPartyRoom extends L2GameClientPacket {
    private int _roomId;
    private int _locations;
    private int _level;

    @Override
    protected void readImpl() {
        _roomId = readD();
        _locations = readD();
        _level = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (player.getMatchingRoom() != null) {
            return;
        }
        if (_roomId > 0) {
            final MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.PARTY_MATCHING, _roomId);
            if (room == null) {
                return;
            }
            room.addMember(player);
        } else {
            for (final MatchingRoom room2 : MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, _locations, _level == 1, player)) {
                if (room2.addMember(player)) {
                    break;
                }
            }
        }
    }
}
