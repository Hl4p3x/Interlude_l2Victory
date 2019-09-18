package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExBlockUpSetList extends L2GameServerPacket {
    private final int BlockUpType;

    public ExBlockUpSetList() {
        BlockUpType = 0;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(BlockUpType);
        switch (BlockUpType) {
            case 0: {
            }
            case 1: {
            }
            case 2: {
            }
            case 3: {
            }
            case 4: {
            }
        }
    }
}
