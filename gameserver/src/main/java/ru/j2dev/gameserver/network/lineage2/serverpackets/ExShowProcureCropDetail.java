package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowProcureCropDetail extends L2GameServerPacket {
    private final int _cropId;
    private final Map<Integer, CropProcure> _castleCrops;

    public ExShowProcureCropDetail(final int cropId) {
        _cropId = cropId;
        _castleCrops = new TreeMap<>();
        final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        for (final Castle c : castleList) {
            final CropProcure cropItem = c.getCrop(_cropId, 0);
            if (cropItem != null && cropItem.getAmount() > 0L) {
                _castleCrops.put(c.getId(), cropItem);
            }
        }
    }

    @Override
    public void writeImpl() {
        writeEx(0x22);
        writeD(_cropId);
        writeD(_castleCrops.size());
        _castleCrops.forEach((manorId, crop) -> {
            writeD(manorId);
            writeD((int) crop.getAmount());
            writeD((int) crop.getPrice());
            writeC(crop.getReward());
        });
    }
}
