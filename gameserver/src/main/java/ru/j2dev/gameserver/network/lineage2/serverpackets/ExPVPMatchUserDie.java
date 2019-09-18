package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPVPMatchUserDie extends L2GameServerPacket {
    private final int _blueKills;
    private final int _redKills;

    public ExPVPMatchUserDie(final int blue, final int red) {
        _blueKills = blue;
        _redKills = red;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x7f);
        writeD(_blueKills);
        writeD(_redKills);
    }
}
