package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExAskJoinPartyRoom extends L2GameServerPacket {
    private final String _charName;
    private final String _roomName;

    public ExAskJoinPartyRoom(final String charName, final String roomName) {
        _charName = charName;
        _roomName = roomName;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x34);
        writeS(_charName);
    }
}
