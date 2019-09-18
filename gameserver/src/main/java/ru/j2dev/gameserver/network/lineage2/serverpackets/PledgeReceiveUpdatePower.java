package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PledgeReceiveUpdatePower extends L2GameServerPacket {
    private final int _privs;

    public PledgeReceiveUpdatePower(final int privs) {
        _privs = privs;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x42);
        writeD(_privs);
    }
}
