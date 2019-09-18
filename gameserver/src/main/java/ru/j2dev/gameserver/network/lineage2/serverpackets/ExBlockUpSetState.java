package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExBlockUpSetState extends L2GameServerPacket {
    private final int BlockUpStateType;

    public ExBlockUpSetState() {
        BlockUpStateType = 0;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x98);
        writeD(BlockUpStateType);
        switch (BlockUpStateType) {
            case 0: {
            }
        }
    }
}
