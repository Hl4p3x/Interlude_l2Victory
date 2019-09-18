package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.matching.MatchingRoom;

public class PartyRoomInfo extends L2GameServerPacket {
    private final int _id;
    private final int _minLevel;
    private final int _maxLevel;
    private final int _lootDist;
    private final int _maxMembers;
    private final int _location;
    private final String _title;

    public PartyRoomInfo(final MatchingRoom room) {
        _id = room.getId();
        _minLevel = room.getMinLevel();
        _maxLevel = room.getMaxLevel();
        _lootDist = room.getLootType();
        _maxMembers = room.getMaxMembersSize();
        _location = room.getLocationId();
        _title = room.getTopic();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x97);
        writeD(_id);
        writeD(_maxMembers);
        writeD(_minLevel);
        writeD(_maxLevel);
        writeD(_lootDist);
        writeD(_location);
        writeS(_title);
    }
}
