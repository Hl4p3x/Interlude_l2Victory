package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowSellCropList extends L2GameServerPacket {
    private final Map<Integer, ItemInstance> _cropsItems;
    private final Map<Integer, CropProcure> _castleCrops;
    private int _manorId;

    public ExShowSellCropList(final Player player, final int manorId, final List<CropProcure> crops) {
        _manorId = 1;
        _manorId = manorId;
        _castleCrops = new TreeMap<>();
        _cropsItems = new TreeMap<>();
        final List<Integer> allCrops = Manor.getInstance().getAllCrops();
        allCrops.forEach(cropId -> {
            final ItemInstance item = player.getInventory().getItemByItemId(cropId);
            if (item != null) {
                _cropsItems.put(cropId, item);
            }
        });
        crops.stream().filter(crop -> _cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0L).forEach(crop -> _castleCrops.put(crop.getId(), crop));
    }

    @Override
    public void writeImpl() {
        writeEx(0x21);
        writeD(_manorId);
        writeD(_cropsItems.size());
        _cropsItems.values().forEach(item -> {
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(Manor.getInstance().getSeedLevelByCrop(item.getItemId()));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(item.getItemId(), 1));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(item.getItemId(), 2));
            if (_castleCrops.containsKey(item.getItemId())) {
                final CropProcure crop = _castleCrops.get(item.getItemId());
                writeD(_manorId);
                writeD((int) crop.getAmount());
                writeD((int) crop.getPrice());
                writeC(crop.getReward());
            } else {
                writeD(-1);
                writeD(0);
                writeD(0);
                writeC(0);
            }
            writeD((int) item.getCount());
        });
    }
}
