package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.List;

public class ExShowCropInfo extends L2GameServerPacket {
    private final List<CropProcure> _crops;
    private final int _manorId;

    public ExShowCropInfo(final int manorId, final List<CropProcure> crops) {
        _manorId = manorId;
        _crops = crops;
    }

    @Override
    protected void writeImpl() {
        writeEx(29);
        writeC(0);
        writeD(_manorId);
        writeD(0);
        writeD(_crops.size());
        _crops.forEach(crop -> {
            writeD(crop.getId());
            writeD((int) crop.getAmount());
            writeD((int) crop.getStartAmount());
            writeD((int) crop.getPrice());
            writeC(crop.getReward());
            writeD(Manor.getInstance().getSeedLevelByCrop(crop.getId()));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(crop.getId(), 1));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(crop.getId(), 2));
        });
    }
}
