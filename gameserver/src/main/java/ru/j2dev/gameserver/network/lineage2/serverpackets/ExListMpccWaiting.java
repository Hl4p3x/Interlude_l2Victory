package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExListMpccWaiting extends L2GameServerPacket {
    private static final int PAGE_SIZE = 10;
    private final int _fullSize;
    private final List<MatchingRoom> _list;

    public ExListMpccWaiting(final Player player, final int page, final int location, final boolean allLevels) {
        final int first = (page - 1) * 10;
        final int firstNot = page * 10;
        int i = 0;
        final Collection<MatchingRoom> all = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
        _fullSize = all.size();
        _list = new ArrayList<>(10);
        for (final MatchingRoom c : all) {
            if (i >= first) {
                if (i >= firstNot) {
                    continue;
                }
                _list.add(c);
                ++i;
            }
        }
    }

    @Override
    public void writeImpl() {
        writeEx(0x9c);
        writeD(_fullSize);
        writeD(_list.size());
        _list.forEach(room -> {
            writeD(room.getId());
            writeS(room.getTopic());
            writeD(room.getPlayers().size());
            writeD(room.getMinLevel());
            writeD(room.getMaxLevel());
            writeD(1);
            writeD(room.getMaxMembersSize());
            final Player leader = room.getLeader();
            writeS((leader == null) ? "" : leader.getName());
        });
    }
}
