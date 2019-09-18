package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class MoveWithDelta extends L2GameClientPacket {
    private int _dx;
    private int _dy;
    private int _dz;

    @Override
    protected void readImpl() {
        _dx = readD();
        _dy = readD();
        _dz = readD();
    }

    @Override
    protected void runImpl() {
    }
}
