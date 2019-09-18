package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestPCCafeCouponUse extends L2GameClientPacket {
    private String _unknown;

    @Override
    protected void readImpl() {
        _unknown = readS();
    }

    @Override
    protected void runImpl() {
    }
}
