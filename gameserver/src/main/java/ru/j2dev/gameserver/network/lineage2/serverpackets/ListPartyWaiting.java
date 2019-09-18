package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPartyWaiting extends L2GameServerPacket {
    private final Collection<MatchingRoom> _rooms;
    private final int _fullSize;

    public ListPartyWaiting(final int region, final boolean allLevels, final int page, final Player activeChar) {
        final int first = (page - 1) * 64;
        final int firstNot = page * 64;
        _rooms = new ArrayList<>();
        int i = 0;
        final List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
        _fullSize = temp.size();
        for (final MatchingRoom room : temp) {
            if (i >= first) {
                if (i >= firstNot) {
                    continue;
                }
                _rooms.add(room);
                ++i;
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0x96);
        writeD(_fullSize);
        writeD(_rooms.size());
        _rooms.forEach(room -> {
            writeD(room.getId());
            writeS((room.getLeader() == null) ? "None" : room.getLeader().getName());
            writeD(room.getLocationId());
            writeD(room.getMinLevel());
            writeD(room.getMaxLevel());
            writeD(room.getMaxMembersSize());
            writeS(room.getTopic());
            final Collection<Player> players = room.getPlayers();
            writeD(players.size());
            players.forEach(player -> {
                writeD(player.getClassId().getId());
                writeS(player.getName());
            });
        });
    }
}
