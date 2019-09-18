package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeEx(0x43);
    }
}
