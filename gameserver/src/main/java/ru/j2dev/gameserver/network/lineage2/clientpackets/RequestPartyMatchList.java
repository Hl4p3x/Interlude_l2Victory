package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.model.matching.PartyMatchingRoom;

public class RequestPartyMatchList extends L2GameClientPacket {
    private int _lootDist;
    private int _maxMembers;
    private int _minLevel;
    private int _maxLevel;
    private int _roomId;
    private String _roomTitle;

    @Override
    protected void readImpl() {
        _roomId = readD();
        _maxMembers = readD();
        _minLevel = readD();
        _maxLevel = readD();
        _lootDist = readD();
        _roomTitle = readS(64);
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final MatchingRoom room = player.getMatchingRoom();
        if (room == null) {
            new PartyMatchingRoom(player, _minLevel, _maxLevel, _maxMembers, _lootDist, _roomTitle);
        } else if (room.getId() == _roomId && room.getType() == MatchingRoom.PARTY_MATCHING && room.getLeader() == player) {
            room.setMinLevel(_minLevel);
            room.setMaxLevel(_maxLevel);
            room.setMaxMemberSize(_maxMembers);
            room.setTopic(_roomTitle);
            room.setLootType(_lootDist);
            room.broadCast(room.infoRoomPacket());
        }
    }
}
