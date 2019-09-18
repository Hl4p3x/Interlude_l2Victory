package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Manor;

import java.util.List;

public class ExShowManorDefaultInfo extends L2GameServerPacket {
    private List<Integer> _crops;

    public ExShowManorDefaultInfo() {
        _crops = null;
        _crops = Manor.getInstance().getAllCrops();
    }

    @Override
    protected void writeImpl() {
        writeEx(0x1e);
        writeC(0);
        writeD(_crops.size());
        _crops.forEach(cropId -> {
            writeD(cropId);
            writeD(Manor.getInstance().getSeedLevelByCrop(cropId));
            writeD(Manor.getInstance().getSeedBasicPriceByCrop(cropId));
            writeD(Manor.getInstance().getCropBasicPrice(cropId));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(cropId, 1));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(cropId, 2));
        });
    }
}
